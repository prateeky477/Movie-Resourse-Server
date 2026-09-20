package com.pratk.movie_RS.movie_RS.service.serviceimpl;

import com.pratk.movie_RS.movie_RS.entity.*;
import com.pratk.movie_RS.movie_RS.records.CreateBookingRequest;
import com.pratk.movie_RS.movie_RS.repository.BookingRepository;
import com.pratk.movie_RS.movie_RS.repository.ShowtimeRepository;
import com.pratk.movie_RS.movie_RS.repository.ShowtimeSeatRepository;
import com.pratk.movie_RS.movie_RS.service.BookingService;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class BookingServiceImpl implements BookingService {

    private static final String HOLD_KEY_PREFIX = "hold:seat:";

    private final BookingRepository bookingRepository;
    private final ShowtimeRepository showtimeRepository;
    private final ShowtimeSeatRepository showtimeSeatRepository;
    private final RedissonClient redissonClient; // NEW

    public List<Booking> getBookings(String userId) {
        return bookingRepository.findByUserId(userId);
    }

    public Booking getBooking(Long bookingId, String userId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new EntityNotFoundException("Booking not found: " + bookingId));
        validateOwnership(booking, userId);
        return booking;
    }

    @Override
    @Transactional
    public void cancelBooking(Long bookingId, String userId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new EntityNotFoundException("Booking not found: " + bookingId));
        validateOwnership(booking, userId);

        if (booking.getStatus() != Booking.BookingStatus.PENDING) {
            throw new IllegalStateException(
                    "Booking " + bookingId + " cannot be cancelled — current status is " + booking.getStatus());
        }

        booking.setStatus(Booking.BookingStatus.CANCELLED);
        booking.getBookingSeats().forEach(bs -> {
            ShowtimeSeat seat = bs.getShowtimeSeat();
            seat.setStatus(ShowtimeSeat.SeatStatus.AVAILABLE);
            seat.setLockedByUserId(null);
            seat.setLockExpiresAt(null);
            // Redis key was deleted when booking was created, nothing to clean up here
        });
        bookingRepository.save(booking);
    }

    public List<ShowtimeSeat> getBookingSeats(Long bookingId, String userId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new EntityNotFoundException("Booking not found: " + bookingId));
        validateOwnership(booking, userId);

        List<Long> showtimeSeatIds = booking.getBookingSeats().stream()
                .map(bs -> bs.getShowtimeSeat().getId())
                .toList();

        return showtimeSeatRepository.findByIdIn(showtimeSeatIds);
    }

    @Override
    @Transactional
    public Booking createBooking(String userId, CreateBookingRequest request) {
        Showtime showtime = showtimeRepository.findById(request.showtimeId())
                .orElseThrow(() -> new EntityNotFoundException("Showtime not found: " + request.showtimeId()));

        List<ShowtimeSeat> seats = showtimeSeatRepository.findByIdIn(request.showtimeSeatIds());
        if (seats.size() != request.showtimeSeatIds().size()) {
            throw new EntityNotFoundException("One or more seats not found");
        }

        // Check Redis ownership — if the key is missing, the hold expired before booking
        for (ShowtimeSeat seat : seats) {
            if (!seat.getShowtime().getId().equals(request.showtimeId())) {
                throw new IllegalArgumentException(
                        "Seat " + seat.getId() + " does not belong to showtime " + request.showtimeId());
            }
            RBucket<String> bucket = redissonClient.getBucket(HOLD_KEY_PREFIX + seat.getId());
            String heldBy = bucket.get();
            if (heldBy == null || !heldBy.equals(userId)) {
                throw new IllegalStateException(
                        "Seat " + seat.getId() + " is not held by you — hold it first or your hold expired");
            }
        }

        // Delete Redis keys — booking now owns these seats, TTL no longer needed
        seats.forEach(seat -> redissonClient.getBucket(HOLD_KEY_PREFIX + seat.getId()).delete());

        BigDecimal total = seats.stream()
                .map(ShowtimeSeat::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Booking booking = new Booking();
        booking.setUserId(userId);
        booking.setShowtime(showtime);
        booking.setStatus(Booking.BookingStatus.PENDING);
        booking.setTotalAmount(total);
        booking.setExpiresAt(LocalDateTime.now().plusMinutes(15));
        Booking saved = bookingRepository.save(booking);

        List<BookingSeat> bookingSeats = seats.stream()
                .map(seat -> {
                    BookingSeat bs = new BookingSeat();
                    bs.setBooking(saved);
                    bs.setShowtimeSeat(seat);
                    bs.setPriceAtBooking(seat.getPrice());
                    return bs;
                })
                .collect(Collectors.toList());

        saved.setBookingSeats(bookingSeats);
        return bookingRepository.save(saved);
    }

    private void validateOwnership(Booking booking, String userId) {
        if (!booking.getUserId().equals(userId)) {
            throw new EntityNotFoundException("Booking not found: " + booking.getId());
        }
    }
}
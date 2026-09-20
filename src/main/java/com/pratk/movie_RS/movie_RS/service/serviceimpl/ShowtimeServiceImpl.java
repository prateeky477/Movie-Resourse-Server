package com.pratk.movie_RS.movie_RS.service.serviceimpl;

import com.pratk.movie_RS.movie_RS.entity.Movie;
import com.pratk.movie_RS.movie_RS.entity.Screen;
import com.pratk.movie_RS.movie_RS.entity.Showtime;
import com.pratk.movie_RS.movie_RS.entity.ShowtimeSeat;
import com.pratk.movie_RS.movie_RS.repository.MovieRepository;
import com.pratk.movie_RS.movie_RS.repository.ScreenRepository;
import com.pratk.movie_RS.movie_RS.repository.ShowtimeRepository;
import com.pratk.movie_RS.movie_RS.repository.ShowtimeSeatRepository;
import com.pratk.movie_RS.movie_RS.service.ShowtimeService;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@AllArgsConstructor
public class ShowtimeServiceImpl implements ShowtimeService {

    private static final int HOLD_MINUTES = 10;
    private static final String HOLD_KEY_PREFIX = "hold:seat:";

    private final ShowtimeRepository showtimeRepository;
    private final ScreenRepository screenRepository;
    private final MovieRepository movieRepository;
    private final ShowtimeSeatRepository showtimeSeatRepository;
    private final RedissonClient redissonClient;

    @Override
    public List<Showtime> getShowtimes() {
        return showtimeRepository.findAll();
    }

    @Override
    public Showtime getShowtime(Long id) {
        return showtimeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Showtime not found: " + id));
    }

    @Override
    @Transactional
    public Showtime createShowtime(Showtime showtime) {
        Movie movie = movieRepository.findById(showtime.getMovie().getId())
                .orElseThrow(() -> new EntityNotFoundException("Movie not found: " + showtime.getMovie().getId()));
        Screen screen = screenRepository.findById(showtime.getScreen().getId())
                .orElseThrow(() -> new EntityNotFoundException("Screen not found: " + showtime.getScreen().getId()));

        showtime.setMovie(movie);
        showtime.setScreen(screen);
        Showtime saved = showtimeRepository.save(showtime);

        List<ShowtimeSeat> showtimeSeats = screen.getSeats().stream()
                .map(seat -> {
                    ShowtimeSeat ss = new ShowtimeSeat();
                    ss.setShowtime(saved);
                    ss.setSeat(seat);
                    ss.setStatus(ShowtimeSeat.SeatStatus.AVAILABLE);
                    ss.setPrice(saved.getBasePrice());
                    return ss;
                })
                .toList();
        showtimeSeatRepository.saveAll(showtimeSeats);
        saved.setShowtimeSeats(showtimeSeats);

        return saved;
    }

    @Override
    public Showtime updateShowtime(Long id, Showtime showtime) {
        Showtime existing = getShowtime(id);
        existing.setMovie(showtime.getMovie());
        existing.setScreen(showtime.getScreen());
        existing.setStartTime(showtime.getStartTime());
        existing.setEndTime(showtime.getEndTime());
        existing.setBasePrice(showtime.getBasePrice());
        return showtimeRepository.save(existing);
    }

    @Override
    public void deleteShowtime(Long id) {
        if (!showtimeRepository.existsById(id)) {
            throw new EntityNotFoundException("Showtime not found: " + id);
        }
        showtimeRepository.deleteById(id);
    }

    // Lazy self-healing — if Postgres says LOCKED but Redis hold key is gone,
    // the TTL expired and the seat should be AVAILABLE again. No sweeper needed.
    @Override
    @Transactional
    public List<ShowtimeSeat> getShowtimeSeats(Long id) {
        List<ShowtimeSeat> seats = getShowtime(id).getShowtimeSeats();

        List<ShowtimeSeat> healed = new ArrayList<>();
        for (ShowtimeSeat seat : seats) {
            if (seat.getStatus() == ShowtimeSeat.SeatStatus.LOCKED
                    && !redissonClient.getBucket(HOLD_KEY_PREFIX + seat.getId()).isExists()) {
                seat.setStatus(ShowtimeSeat.SeatStatus.AVAILABLE);
                seat.setLockedByUserId(null);
                seat.setLockExpiresAt(null);
                healed.add(seat);
            }
        }
        if (!healed.isEmpty()) {
            showtimeSeatRepository.saveAll(healed);
        }
        return seats;
    }

    @Override
    @Transactional
    public List<ShowtimeSeat> holdSeats(Long showtimeId, List<Long> showtimeSeatIds, String userId) {
        List<ShowtimeSeat> seats = showtimeSeatRepository.findByIdIn(showtimeSeatIds);

        if (seats.size() != showtimeSeatIds.size()) {
            throw new EntityNotFoundException("One or more seats not found");
        }
        for (ShowtimeSeat seat : seats) {
            if (!seat.getShowtime().getId().equals(showtimeId)) {
                throw new IllegalArgumentException(
                        "Seat " + seat.getId() + " does not belong to showtime " + showtimeId);
            }
            if (seat.getStatus() == ShowtimeSeat.SeatStatus.BOOKED) {
                throw new IllegalStateException("Seat " + seat.getId() + " is already booked");
            }
        }

        // trySet is atomic — only one thread wins per seat key, no @Version race possible
        List<Long> acquired = new ArrayList<>();
        try {
            for (ShowtimeSeat seat : seats) {
                System.out.println("Redis key = " + HOLD_KEY_PREFIX + seat.getId());
                RBucket<String> bucket = redissonClient.getBucket(HOLD_KEY_PREFIX + seat.getId());
                boolean gotIt = bucket.trySet(userId, HOLD_MINUTES, TimeUnit.MINUTES);
                System.out.println("gotIt = " + gotIt);
                System.out.println("value = " + bucket.get());
                System.out.println("ttl = " + bucket.remainTimeToLive());
                if (!gotIt) {
                    throw new IllegalStateException("Seat " + seat.getId() + " was just taken by another user");
                }
                acquired.add(seat.getId());
            }
        } catch (IllegalStateException ex) {
            // Roll back any keys we already wrote in this batch before failing
            acquired.forEach(acquiredId ->
                    redissonClient.getBucket(HOLD_KEY_PREFIX + acquiredId).delete());
            throw ex;
        }

        seats.forEach(seat -> {
            seat.setStatus(ShowtimeSeat.SeatStatus.LOCKED);
            seat.setLockedByUserId(userId);
        });
        return showtimeSeatRepository.saveAll(seats);
    }

    @Override
    @Transactional
    public void releaseSeats(Long showtimeId, List<Long> showtimeSeatIds, String userId) {
        List<ShowtimeSeat> seats = showtimeSeatRepository.findByIdIn(showtimeSeatIds);

        if (seats.size() != showtimeSeatIds.size()) {
            throw new EntityNotFoundException("One or more seats not found");
        }

        // Ownership check against Redis — if the key is gone, the hold already expired naturally
        for (ShowtimeSeat seat : seats) {
            if (!seat.getShowtime().getId().equals(showtimeId)) {
                throw new IllegalArgumentException(
                        "Seat " + seat.getId() + " does not belong to showtime " + showtimeId);
            }
            RBucket<String> bucket = redissonClient.getBucket(HOLD_KEY_PREFIX + seat.getId());
            String heldBy = bucket.get();
            if (heldBy == null || !heldBy.equals(userId)) {
                throw new IllegalStateException("Seat " + seat.getId() + " is not held by you");
            }
        }

        seats.forEach(seat -> {
            redissonClient.getBucket(HOLD_KEY_PREFIX + seat.getId()).delete();
            seat.setStatus(ShowtimeSeat.SeatStatus.AVAILABLE);
            seat.setLockedByUserId(null);
            seat.setLockExpiresAt(null);
        });
        showtimeSeatRepository.saveAll(seats);
    }
}
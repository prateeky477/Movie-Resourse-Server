package com.pratk.movie_RS.movie_RS.service.serviceimpl;

import com.pratk.movie_RS.movie_RS.entity.Screen;
import com.pratk.movie_RS.movie_RS.entity.Seat;
import com.pratk.movie_RS.movie_RS.records.SeatLayoutRequest;
import com.pratk.movie_RS.movie_RS.repository.ScreenRepository;
import com.pratk.movie_RS.movie_RS.repository.SeatRepository;
import com.pratk.movie_RS.movie_RS.service.SeatService;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
public class SeatServiceImpl implements SeatService {

    private final SeatRepository seatRepository;
    private final ScreenRepository screenRepository;

    @Override
    public List<Seat> generateSeatLayout(Long theaterId, Long screenId, SeatLayoutRequest request) {
        Screen screen = screenRepository.findById(screenId)
                .orElseThrow(() -> new EntityNotFoundException("Screen not found: " + screenId));
        validateBelongsToTheater(screen, theaterId);

        List<Seat> seats = new ArrayList<>();
        for (String row : request.rowLabels()) {
            for (int num = 1; num <= request.seatsPerRow(); num++) {
                Seat seat = new Seat();
                seat.setSeatRow(row);                 // was missing entirely
                seat.setSeatNumber(row + num);         // was String.valueOf(num) — collided across rows
                seat.setSeatType(request.seatType());
                seat.setScreen(screen);
                seats.add(seat);
            }
        }
        return seatRepository.saveAll(seats);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Seat> getSeatsForScreen(Long theaterId, Long screenId) {
        Screen screen = screenRepository.findById(screenId)
                .orElseThrow(() -> new EntityNotFoundException("Screen not found: " + screenId));
        validateBelongsToTheater(screen, theaterId);
        return seatRepository.findByScreenId(screenId);
    }

    private void validateBelongsToTheater(Screen screen, Long theaterId) {
        if (!screen.getTheater().getId().equals(theaterId)) {
            throw new EntityNotFoundException(
                    "Screen " + screen.getId() + " does not belong to theater " + theaterId);
        }
    }
}
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@Service
@AllArgsConstructor
public class ShowtimeServiceImpl implements ShowtimeService {

    private final ShowtimeRepository showtimeRepository;
    private final ScreenRepository screenRepository;
    private final MovieRepository movieRepository;
    private final ShowtimeSeatRepository showtimeSeatRepository; // add this


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
    @Transactional // add this — screen.getSeats() below is a lazy fetch
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

    @Override
    @Transactional(readOnly = true)
    public List<ShowtimeSeat> getShowtimeSeats(Long id) {
        return getShowtime(id).getShowtimeSeats();
    }
}
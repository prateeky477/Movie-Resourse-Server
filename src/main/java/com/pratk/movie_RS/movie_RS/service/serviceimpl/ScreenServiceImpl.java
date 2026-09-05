package com.pratk.movie_RS.movie_RS.service.serviceimpl;

import com.pratk.movie_RS.movie_RS.entity.Screen;
import com.pratk.movie_RS.movie_RS.entity.Theater;
import com.pratk.movie_RS.movie_RS.repository.ScreenRepository;
import com.pratk.movie_RS.movie_RS.repository.TheaterRepository;
import com.pratk.movie_RS.movie_RS.service.ScreenService;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import org.hibernate.Hibernate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
@Service
@AllArgsConstructor
public class ScreenServiceImpl implements ScreenService {

    private final ScreenRepository screenRepository;
    private final TheaterRepository theaterRepository;

    @Override
    public Screen createScreen(Long theaterId, Screen screen) {
        Theater theater = theaterRepository.findById(theaterId)
                .orElseThrow(() -> new EntityNotFoundException("Theater not found: " + theaterId));
        screen.setTheater(theater);
        return screenRepository.save(screen);
    }

    @Override
    public List<Screen> getScreensByTheater(Long theaterId) {
        if (!theaterRepository.existsById(theaterId)) {
            throw new EntityNotFoundException("Theater not found: " + theaterId);
        }
        return screenRepository.findByTheaterId(theaterId);
    }

    @Override
    @Transactional(readOnly = true)
    public Screen getScreen(Long theaterId, Long screenId) {
        Screen screen = screenRepository.findById(screenId)
                .orElseThrow(() -> new EntityNotFoundException("Screen not found: " + screenId));
        validateBelongsToTheater(screen, theaterId);
        Hibernate.initialize(screen.getSeats());
        return screen;
    }

    @Override
    public Screen updateScreen(Long theaterId, Long screenId, Screen screen) {
        Screen existing = screenRepository.findById(screenId)
                .orElseThrow(() -> new EntityNotFoundException("Screen not found: " + screenId));
        validateBelongsToTheater(existing, theaterId);
        existing.setScreenNumber(screen.getScreenNumber());
        return screenRepository.save(existing);
    }

    private void validateBelongsToTheater(Screen screen, Long theaterId) {
        if (!screen.getTheater().getId().equals(theaterId)) {
            throw new EntityNotFoundException(
                    "Screen " + screen.getId() + " does not belong to theater " + theaterId);
        }
    }
}
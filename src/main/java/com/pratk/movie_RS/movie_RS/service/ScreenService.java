package com.pratk.movie_RS.movie_RS.service;

import com.pratk.movie_RS.movie_RS.entity.Screen;
import com.pratk.movie_RS.movie_RS.entity.Theater;
import com.pratk.movie_RS.movie_RS.repository.ScreenRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

public interface ScreenService {
    Screen createScreen(Long theaterId, Screen screen);
    List<Screen> getScreensByTheater(Long theaterId);
    Screen  getScreen(Long theaterId, Long screenId);
    Screen updateScreen(Long theaterId, Long screenId, Screen screen);
}
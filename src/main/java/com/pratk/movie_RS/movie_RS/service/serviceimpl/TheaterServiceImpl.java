package com.pratk.movie_RS.movie_RS.service.serviceimpl;

import com.pratk.movie_RS.movie_RS.entity.Theater;
import com.pratk.movie_RS.movie_RS.repository.TheaterRepository;
import com.pratk.movie_RS.movie_RS.service.TheaterService;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class TheaterServiceImpl implements TheaterService {

    private final TheaterRepository theaterRepository;

    @Override
    public List<Theater> getTheaters() {
        return theaterRepository.findAll();
    }

    @Override
    public Theater getTheater(Long id) {
        return theaterRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Theater not found: " + id));
    }

    @Override
    public Theater createTheater(Theater theater) {
        return theaterRepository.save(theater);
    }

    @Override
    public Theater updateTheater(Long id, Theater theater) {
        Theater existing = getTheater(id);
        existing.setName(theater.getName());
        existing.setCity(theater.getCity());
        existing.setAddress(theater.getAddress());
        return theaterRepository.save(existing);
    }

    @Override
    public void deleteTheater(Long id) {
        if (!theaterRepository.existsById(id)) {
            throw new EntityNotFoundException("Theater not found: " + id);
        }
        theaterRepository.deleteById(id);
    }
}

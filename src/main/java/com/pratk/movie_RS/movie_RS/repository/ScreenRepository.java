package com.pratk.movie_RS.movie_RS.repository;

import com.pratk.movie_RS.movie_RS.entity.Screen;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ScreenRepository extends JpaRepository<Screen, Long> {
    List<Screen> findByTheaterId(Long theaterId);

}
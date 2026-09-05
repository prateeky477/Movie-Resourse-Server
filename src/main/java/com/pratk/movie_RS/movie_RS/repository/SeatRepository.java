package com.pratk.movie_RS.movie_RS.repository;

import com.pratk.movie_RS.movie_RS.entity.Seat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SeatRepository extends JpaRepository<Seat, Long> {
    List<Seat> findByScreenId(Long screenId);
}

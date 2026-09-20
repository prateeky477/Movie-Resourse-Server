package com.pratk.movie_RS.movie_RS.repository;

import com.pratk.movie_RS.movie_RS.entity.ShowtimeSeat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface ShowtimeSeatRepository extends JpaRepository<ShowtimeSeat, Long> {
    List<ShowtimeSeat> findByShowtimeId(Long showtimeId);
    List<ShowtimeSeat> findByIdIn(List<Long> ids);
    List<ShowtimeSeat> findByStatusAndLockExpiresAtBefore(ShowtimeSeat.SeatStatus status, LocalDateTime cutoff);

}
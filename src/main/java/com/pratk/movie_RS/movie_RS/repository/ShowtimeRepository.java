package com.pratk.movie_RS.movie_RS.repository;

import com.pratk.movie_RS.movie_RS.entity.Showtime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ShowtimeRepository extends JpaRepository<Showtime, Long> {



}

package com.pratk.movie_RS.movie_RS.repository;

import com.pratk.movie_RS.movie_RS.entity.Movie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MovieRepository extends JpaRepository<Movie, Long> {


}

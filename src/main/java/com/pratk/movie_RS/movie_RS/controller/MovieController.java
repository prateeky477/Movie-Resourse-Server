package com.pratk.movie_RS.movie_RS.controller;

import com.pratk.movie_RS.movie_RS.entity.Movie;
import com.pratk.movie_RS.movie_RS.service.MovieService;
import lombok.AllArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/movie")
@AllArgsConstructor
public class MovieController {


    private final MovieService movieService;

    @GetMapping("/movies")
    public List<Movie> getMovies() {
        return movieService.getAllMovies();
    }

    @PreAuthorize("hasAuthority('READ_MOVIE')")
    @GetMapping("/movies/{id}")
    public Movie getMovie(@PathVariable Long id) {
        return movieService.getMovieById(id);
    }

    @PreAuthorize("hasAuthority('CREATE_MOVIE')")
    @PostMapping("/movies")
    public Movie createMovie(@RequestBody Movie movie) {
        return movieService.createMovie(movie);
    }

    @PreAuthorize("hasAuthority('UPDATE_MOVIE')")
    @PutMapping("/movies/{id}")
    public Movie updateMovie(
            @PathVariable Long id,
            @RequestBody Movie movie) {

        return movieService.updateMovie(id, movie);
    }

    @PreAuthorize("hasAuthority('DELETE_MOVIE')")
    @DeleteMapping("/movies/{id}")
    public void deleteMovie(@PathVariable Long id) {
        movieService.deleteMovie(id);
    }
}
package com.pratk.movie_RS.movie_RS.service;
import com.pratk.movie_RS.movie_RS.entity.Movie;

import java.util.List;

public interface MovieService {

    public List<Movie> getAllMovies();

    public Movie getMovieById(Long id) ;

    public Movie createMovie(Movie movie) ;

    public Movie updateMovie(Long id, Movie updatedMovie);

    public void deleteMovie(Long id);
}
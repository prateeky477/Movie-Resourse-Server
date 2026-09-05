package com.pratk.movie_RS.movie_RS.service.serviceimpl;

import com.pratk.movie_RS.movie_RS.entity.Movie;
import com.pratk.movie_RS.movie_RS.repository.MovieRepository;
import com.pratk.movie_RS.movie_RS.service.MovieService;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
@AllArgsConstructor
public class MovieServiceImpl implements MovieService {


        private final MovieRepository movieRepository;


        public List<Movie> getAllMovies() {
            return movieRepository.findAll();
        }

    public Movie getMovieById(Long id) {
        return movieRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Movie not found: " + id));
    }

        public Movie createMovie(Movie movie) {
            return movieRepository.save(movie);
        }

        public Movie updateMovie(Long id, Movie updatedMovie) {

            Movie movie = getMovieById(id);

            movie.setTitle(updatedMovie.getTitle());
            movie.setGenre(updatedMovie.getGenre());
            movie.setLanguage(updatedMovie.getLanguage());
            movie.setReleaseDate(updatedMovie.getReleaseDate());
            movie.setDurationMinutes(updatedMovie.getDurationMinutes());
            movie.setRating(updatedMovie.getRating());

            return movieRepository.save(movie);
        }

        public void deleteMovie(Long id) {

            Movie movie = getMovieById(id);

            movieRepository.delete(movie);
        }
}

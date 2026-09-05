package com.pratk.movie_RS.movie_RS.service;

import com.pratk.movie_RS.movie_RS.entity.Theater;

import java.util.List;

public interface TheaterService {
    List<Theater> getTheaters();
    Theater getTheater(Long id);
    Theater createTheater(Theater theater);
    Theater updateTheater(Long id, Theater theater);
    void deleteTheater(Long id);
}

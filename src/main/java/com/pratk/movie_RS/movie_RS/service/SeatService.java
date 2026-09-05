package com.pratk.movie_RS.movie_RS.service;

import com.pratk.movie_RS.movie_RS.entity.Seat;
import com.pratk.movie_RS.movie_RS.records.SeatLayoutRequest;

import java.util.List;

public interface SeatService {
    List<Seat> generateSeatLayout(Long theaterId, Long screenId, SeatLayoutRequest request);
    List<Seat> getSeatsForScreen(Long theaterId, Long screenId);
}

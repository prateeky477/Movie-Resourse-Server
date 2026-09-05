package com.pratk.movie_RS.movie_RS.records;

import com.pratk.movie_RS.movie_RS.entity.Seat;

import java.util.List;

public record SeatLayoutRequest(
        List<String> rowLabels,      // e.g. ["A", "B", "C"]
        int seatsPerRow,
        Seat.SeatType seatType
) {}

package com.pratk.movie_RS.movie_RS.records;

import java.util.List;

public record CreateBookingRequest(Long showtimeId, List<Long> showtimeSeatIds) {}
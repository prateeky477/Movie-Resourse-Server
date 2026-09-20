package com.pratk.movie_RS.movie_RS.records;

import java.util.List;

// showtimeSeatIds — the ShowtimeSeat rows for THIS showing, not the raw Seat ids
public record HoldRequest(List<Long> showtimeSeatIds) {}
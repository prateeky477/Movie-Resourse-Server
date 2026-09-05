package com.pratk.movie_RS.movie_RS.service;
import com.pratk.movie_RS.movie_RS.entity.Showtime;
import com.pratk.movie_RS.movie_RS.entity.ShowtimeSeat;

import java.util.List;

public interface ShowtimeService {
    List<Showtime> getShowtimes();
    Showtime getShowtime(Long id);
    Showtime createShowtime(Showtime showtime);
    Showtime updateShowtime(Long id, Showtime showtime);
    void deleteShowtime(Long id);
    List<ShowtimeSeat> getShowtimeSeats(Long id);
}
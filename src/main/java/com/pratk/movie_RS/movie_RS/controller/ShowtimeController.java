package com.pratk.movie_RS.movie_RS.controller;

import com.pratk.movie_RS.movie_RS.entity.Movie;
import com.pratk.movie_RS.movie_RS.entity.Showtime;
import com.pratk.movie_RS.movie_RS.entity.ShowtimeSeat;
import com.pratk.movie_RS.movie_RS.records.HoldRequest;
import com.pratk.movie_RS.movie_RS.service.ShowtimeService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@RestController
@RequestMapping("/api/v1/showtime")
@AllArgsConstructor
public class ShowtimeController {

    private final ShowtimeService showtimeService;

    @PreAuthorize("hasAuthority('READ_SHOWTIME')")
    @GetMapping("/showtimes")
    public List<Showtime> getShowtimes() {
        return showtimeService.getShowtimes();
    }

    @PreAuthorize("hasAuthority('READ_SHOWTIME')")
    @GetMapping("/showtime/{id}")
    public Showtime getShowtime(@PathVariable Long id) {
        return showtimeService.getShowtime(id);
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @PostMapping("/showtimes")
    public Showtime createShowtime(@RequestBody Showtime showtime) {
        return showtimeService.createShowtime(showtime);
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @PutMapping("/showtime/{id}")
    public Showtime updateShowtime(@PathVariable Long id, @RequestBody Showtime showtime) {
        return showtimeService.updateShowtime(id, showtime);
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @DeleteMapping("/showtime/{id}")
    public ResponseEntity<Void> deleteShowtime(@PathVariable Long id) {
        showtimeService.deleteShowtime(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/showtime/{id}/seats")
    public List<ShowtimeSeat> getShowtimeSeats(@PathVariable Long id) {
        return showtimeService.getShowtimeSeats(id);
    }

    @PostMapping("/showtime/{id}/hold")
    public List<ShowtimeSeat> holdSeats(@PathVariable Long id,
                                        @RequestBody HoldRequest request,
                                        @AuthenticationPrincipal Jwt jwt) {
        return showtimeService.holdSeats(id, request.showtimeSeatIds(), jwt.getSubject());
    }

    @DeleteMapping("/showtime/{id}/release")
    public ResponseEntity<Void> releaseSeats(@PathVariable Long id,
                                             @RequestBody HoldRequest request,
                                             @AuthenticationPrincipal Jwt jwt) {
        showtimeService.releaseSeats(id, request.showtimeSeatIds(), jwt.getSubject());
        return ResponseEntity.noContent().build();
    }
}
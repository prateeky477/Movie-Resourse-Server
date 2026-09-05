package com.pratk.movie_RS.movie_RS.controller;

import com.pratk.movie_RS.movie_RS.entity.Seat;
import com.pratk.movie_RS.movie_RS.records.SeatLayoutRequest;
import com.pratk.movie_RS.movie_RS.service.SeatService;
import lombok.AllArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/theaters/{theaterId}/screens/{screenId}/seats")
@AllArgsConstructor
public class SeatController {

    private final SeatService seatService;

    @PreAuthorize("hasAuthority('ADMIN')")
    @PostMapping
    public List<Seat> generateSeats(@PathVariable Long theaterId,
                                    @PathVariable Long screenId,
                                    @RequestBody SeatLayoutRequest request) {
        return seatService.generateSeatLayout(theaterId, screenId, request);
    }

    @GetMapping
    public List<Seat> getSeats(@PathVariable Long theaterId, @PathVariable Long screenId) {
        return seatService.getSeatsForScreen(theaterId, screenId);
    }
}

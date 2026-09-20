package com.pratk.movie_RS.movie_RS.controller;

import com.pratk.movie_RS.movie_RS.entity.Booking;
import com.pratk.movie_RS.movie_RS.entity.ShowtimeSeat;
import com.pratk.movie_RS.movie_RS.records.CreateBookingRequest;
import com.pratk.movie_RS.movie_RS.service.BookingService;
import com.pratk.movie_RS.movie_RS.service.serviceimpl.BookingServiceImpl;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("api/v1/bookings")
public class BookingController {

    private final BookingService bookingService;


    @GetMapping
    public List<Booking> getBookings(@AuthenticationPrincipal Jwt jwt){
        return bookingService.getBookings(jwt.getSubject());
    }

    @GetMapping("{id}")
    public Booking getBooking(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt){
        return bookingService.getBooking(id, jwt.getSubject());
    }

    @PostMapping
    public Booking createBooking(@RequestBody CreateBookingRequest bookingRequest, @AuthenticationPrincipal Jwt jwt){
        return bookingService.createBooking(jwt.getSubject(), bookingRequest);
    }

    @DeleteMapping("{id}")
    public ResponseEntity<Void> cancelBooking(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt ){
        bookingService.cancelBooking(id, jwt.getSubject());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("{id}/seats")
    public List<ShowtimeSeat> getBookingSeats(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt){
        return bookingService.getBookingSeats(id, jwt.getSubject());
    }



}

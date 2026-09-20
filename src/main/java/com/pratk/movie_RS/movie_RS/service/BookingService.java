package com.pratk.movie_RS.movie_RS.service;

import com.pratk.movie_RS.movie_RS.entity.Booking;
import com.pratk.movie_RS.movie_RS.entity.ShowtimeSeat;
import com.pratk.movie_RS.movie_RS.records.CreateBookingRequest;

import java.util.List;

public interface BookingService {
        Booking createBooking(String userId, CreateBookingRequest request);
        Booking getBooking(Long id, String userId);
        List<Booking> getBookings(String userId);
        void cancelBooking(Long id, String userId);
        List<ShowtimeSeat> getBookingSeats(Long id, String userId);
    }

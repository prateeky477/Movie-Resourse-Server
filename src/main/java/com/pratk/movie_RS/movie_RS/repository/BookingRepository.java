package com.pratk.movie_RS.movie_RS.repository;

import com.pratk.movie_RS.movie_RS.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {
        List<Booking> findByUserId(String userId);
    }


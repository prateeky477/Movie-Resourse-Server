package com.pratk.movie_RS.movie_RS.repository;


import com.pratk.movie_RS.movie_RS.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByBookingId(Long bookingId);

    Optional<Payment> findByProviderOrderId(String providerOrderId);
}

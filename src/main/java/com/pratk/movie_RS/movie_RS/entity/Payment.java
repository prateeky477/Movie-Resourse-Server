package com.pratk.movie_RS.movie_RS.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Getter
@Setter
public class Payment {

    public enum PaymentStatus { PENDING, SUCCESS, FAILED, REFUNDED }


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    private PaymentStatus status; // PENDING, SUCCESS, FAILED, REFUNDED

    private String provider; // "razorpay", "stripe", "mock"
    private String providerSignature;
    private String transactionId;

    private String providerOrderId;

    @CreationTimestamp
    private LocalDateTime createdAt;
}

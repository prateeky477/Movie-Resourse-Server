package com.pratk.movie_RS.movie_RS.entity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@Entity
@Table(name = "bookings")
@Getter
@Setter
public class Booking {

    public enum BookingStatus { PENDING, CONFIRMED, CANCELLED, EXPIRED }


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String userId; // from JWT 'user_id' claim — no FK, since User lives in auth server

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "showtime_id", nullable = false)
    private Showtime showtime;

    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL)
    private List<BookingSeat> bookingSeats = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    private BookingStatus status; // PENDING, CONFIRMED, CANCELLED, EXPIRED

    private BigDecimal totalAmount;

    @CreationTimestamp
    private LocalDateTime createdAt;

    private LocalDateTime expiresAt; // hold expiry before payment
}
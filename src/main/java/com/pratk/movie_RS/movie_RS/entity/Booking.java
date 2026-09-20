package com.pratk.movie_RS.movie_RS.entity;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

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

    @JsonBackReference("showtimes-bookings")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "showtime_id", nullable = false)
    private Showtime showtime;

    @JsonManagedReference("bookings-booking_seats")
    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL)
    private List<BookingSeat> bookingSeats = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private BookingStatus status; // PENDING, CONFIRMED, CANCELLED, EXPIRED

    private BigDecimal totalAmount;

    @CreationTimestamp
    private LocalDateTime createdAt;

    private LocalDateTime expiresAt; // hold expiry before payment
}
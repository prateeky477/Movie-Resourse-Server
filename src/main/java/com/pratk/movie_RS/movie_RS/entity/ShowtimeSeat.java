package com.pratk.movie_RS.movie_RS.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "showtime_seats",
        uniqueConstraints = @UniqueConstraint(columnNames = {"showtime_id", "seat_id"}))
@Getter
@Setter
public class ShowtimeSeat {

    public enum SeatStatus { AVAILABLE, LOCKED, BOOKED }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ShowtimeSeat.java
    @JsonBackReference("showtime-showtimeseats")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "showtime_id", nullable = false)
    private Showtime showtime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seat_id", nullable = false)
    private Seat seat;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private SeatStatus status; // AVAILABLE, LOCKED, BOOKED

    private String lockedByUserId;
    private LocalDateTime lockExpiresAt;

    private BigDecimal price; // allows per-showtime dynamic pricing

    @Version
    private Long version; // optimistic lock target — this is the field from our earlier discussion
}


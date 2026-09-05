package com.pratk.movie_RS.movie_RS.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;


@Entity
@Table(name = "seats",
        uniqueConstraints = @UniqueConstraint(columnNames = {"screen_id", "seat_number"}))
@Getter
@Setter
public class Seat {

    public enum SeatType { REGULAR, PREMIUM, RECLINER }


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String seatNumber; // e.g. "A1"
    private String seatRow;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private SeatType seatType; // REGULAR, PREMIUM, RECLINER

    // Seat.java
    @JsonBackReference("screen-seats")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "screen_id", nullable = false)
    private Screen screen;

    // IMPORTANT: version lives here only if you model seat status
    // per-screen (fixed seat layout). If status is per-SHOWTIME
    // (a seat can be free for the 3pm show but booked for 6pm),
    // status/version belongs on ShowtimeSeat instead — see below.
}
package com.pratk.movie_RS.movie_RS.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@Entity
@Table(name = "showtimes")
@Getter
@Setter
public class Showtime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private BigDecimal basePrice;

    // Showtime.java
    @JsonBackReference("screen-showtimes")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "screen_id", nullable = false)
    private Screen screen;

    @JsonBackReference("movie-showtimes")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "movie_id", nullable = false)
    private Movie movie;

    @JsonManagedReference("showtime-showtimeseats")
    @OneToMany(mappedBy = "showtime", cascade = CascadeType.ALL)
    private List<ShowtimeSeat> showtimeSeats = new ArrayList<>();
}

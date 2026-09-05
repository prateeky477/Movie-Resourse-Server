package com.pratk.movie_RS.movie_RS.entity;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;


@Entity
@Table(name = "screens")
@Getter
@Setter
public class Screen {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer screenNumber;

    // Screen.java
    @JsonBackReference("theater-screens")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "theater_id", nullable = false)
    private Theater theater;

    @JsonManagedReference("screen-seats")
    @OneToMany(mappedBy = "screen", cascade = CascadeType.ALL)
    private List<Seat> seats = new ArrayList<>();

    @JsonManagedReference("screen-showtimes")
    @OneToMany(mappedBy = "screen")
    private List<Showtime> showtimes = new ArrayList<>();

}

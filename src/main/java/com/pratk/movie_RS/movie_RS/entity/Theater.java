package com.pratk.movie_RS.movie_RS.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;


@Entity
@Table(name = "theaters")
@Getter
@Setter
public class Theater {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String city;
    private String address;

    @JsonManagedReference("theater-screens")
    @OneToMany(mappedBy = "theater", cascade = CascadeType.ALL)
    private List<Screen> screens = new ArrayList<>();
}
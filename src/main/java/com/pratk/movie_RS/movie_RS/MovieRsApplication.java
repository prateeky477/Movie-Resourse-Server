package com.pratk.movie_RS.movie_RS;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MovieRsApplication {

	public static void main(String[] args) {
		SpringApplication.run(MovieRsApplication.class, args);
	}

}

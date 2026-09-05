package com.pratk.movie_RS.movie_RS.controller;

import com.pratk.movie_RS.movie_RS.entity.Screen;
import com.pratk.movie_RS.movie_RS.entity.Theater;
import com.pratk.movie_RS.movie_RS.service.ScreenService;
import com.pratk.movie_RS.movie_RS.service.TheaterService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@RestController
@RequestMapping("/api/v1/theaters")
@AllArgsConstructor
public class TheaterController {

    private final TheaterService theaterService;
    private final ScreenService screenService;

    @GetMapping
    public List<Theater> getTheaters() {
        return theaterService.getTheaters();
    }

    @GetMapping("/{id}")
    public Theater getTheater(@PathVariable Long id) {
        return theaterService.getTheater(id);
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @PostMapping
    public Theater createTheater(@RequestBody Theater theater) {
        return theaterService.createTheater(theater);
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @PutMapping("/{id}")
    public Theater updateTheater(@PathVariable Long id, @RequestBody Theater theater) {
        return theaterService.updateTheater(id, theater);
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTheater(@PathVariable Long id) {
        theaterService.deleteTheater(id);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @PostMapping("/{theaterId}/screens")
    public Screen addScreen(@PathVariable Long theaterId, @RequestBody Screen screen) {
        return screenService.createScreen(theaterId, screen);
    }

    @GetMapping("/{theaterId}/screens")
    public List<Screen> getScreensForTheater(@PathVariable Long theaterId) {
        return screenService.getScreensByTheater(theaterId);
    }

    @GetMapping("/{theaterId}/screens/{screenId}")
    public Screen getScreen(@PathVariable Long theaterId, @PathVariable Long screenId) {
        return screenService.getScreen(theaterId, screenId);
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @PutMapping("/{theaterId}/screens/{screenId}")
    public Screen updateScreen(@PathVariable Long theaterId, @PathVariable Long screenId,
                               @RequestBody Screen screen) {
        return screenService.updateScreen(theaterId, screenId, screen);
    }
}
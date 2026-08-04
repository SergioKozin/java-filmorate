package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.Collection;

@RestController
@RequestMapping("/films")
@Slf4j
public class FilmController {
    private final FilmService filmService;

    public FilmController(FilmService filmService) {
        this.filmService = filmService;
    }

    @GetMapping
    public Collection<Film> findAll() {
        return filmService.findAll();
    }

    @PostMapping
    public Film create(@RequestBody Film newFilm) {
        Film film = filmService.create(newFilm);
        log.info("Создан {} фильм.", film.getName());
        return film;
    }

    @PutMapping
    public Film update(@RequestBody Film newFilm) {
        Film oldFilm = filmService.update(newFilm);
        log.info("Изменены данные фильма {}.", oldFilm.getName());
        return oldFilm;
    }

    @GetMapping("/{id}")
    public Film findFilmById(@PathVariable long id) {
        return filmService.findFilmById(id);
    }

    @PutMapping("/{id}/like/{userId}")
    public Film addLike(@PathVariable Long id, @PathVariable Long userId) {
        return filmService.addLike(id, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public Film deleteLike(@PathVariable Long id, @PathVariable Long userId) {
        return filmService.deleteLike(id, userId);
    }

    @GetMapping("/popular")
    public Collection<Film> findPopularFilms(@RequestParam(defaultValue = "10") long count) {
        return filmService.findPopularFilms(count);
    }
}
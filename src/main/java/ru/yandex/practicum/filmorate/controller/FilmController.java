package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.FilmDto;
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
    public Collection<FilmDto> findAll() {
        return filmService.findAll();
    }

    @PostMapping
    public FilmDto create(@RequestBody FilmDto newFilm) {
        FilmDto film = filmService.create(newFilm);
        log.info("Создан {} фильм.", film.getName());
        return film;
    }

    @PutMapping
    public FilmDto update(@RequestBody FilmDto newFilm) {
        FilmDto updatedFilm = filmService.update(newFilm);
        log.info("Изменены данные фильма {}.", updatedFilm.getName());
        return updatedFilm;
    }

    @GetMapping("/{id}")
    public FilmDto findFilmById(@PathVariable long id) {
        return filmService.findFilmById(id);
    }

    @PutMapping("/{id}/like/{userId}")
    public FilmDto addLike(@PathVariable Long id, @PathVariable Long userId) {
        return filmService.addLike(id, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public FilmDto deleteLike(@PathVariable Long id, @PathVariable Long userId) {
        return filmService.deleteLike(id, userId);
    }

    @GetMapping("/popular")
    public Collection<FilmDto> findPopularFilms(@RequestParam(defaultValue = "10") long count) {
        return filmService.findPopularFilms(count);
    }
}

package ru.yandex.practicum.filmorate.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.Collection;

@Service
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

    public FilmService(FilmStorage filmStorage, UserStorage userStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
    }

    public Collection<Film> findAll() {
        return filmStorage.findAll();
    }

    public Film create(Film film) {
        return filmStorage.create(film);
    }

    public Film update(Film newFilm) {
        return filmStorage.update(newFilm);
    }

    public Film findFilmById(long id) {
        return filmStorage.findFilmById(id).orElseThrow();
    }

    public Film addLike(Long id, Long userId) {
        userStorage.findUserById(userId).orElseThrow();
        filmStorage.findFilmById(id).orElseThrow().getLikes().add(userId);
        return filmStorage.findFilmById(id).orElseThrow();
    }

    public Film deleteLike(Long id, Long userId) {
        userStorage.findUserById(userId).orElseThrow();
        filmStorage.findFilmById(id).orElseThrow().getLikes().remove(userId);
        return filmStorage.findFilmById(id).orElseThrow();
    }

    public Collection<Film> findPopularFilms(Long count) {
        return filmStorage.findAll().stream()
                .sorted((a, b) -> Integer.compare(b.getLikes().size(), a.getLikes().size()))
                .limit(count)
                .toList();
    }
}
package ru.yandex.practicum.filmorate.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.Collection;
import java.util.Optional;

@Service
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

    public FilmService(FilmStorage filmStorage, UserStorage userStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
    }

    public Optional<Film> addLike(Long id, Long userId) {
        userStorage.findUserById(userId).orElseThrow();
        filmStorage.findFilmById(id).orElseThrow().getLikes().add(userId);
        return filmStorage.findFilmById(id);
    }

    public Optional<Film> deleteLike(Long id, Long userId) {
        userStorage.findUserById(userId).orElseThrow();
        filmStorage.findFilmById(id).orElseThrow().getLikes().remove(userId);
        return filmStorage.findFilmById(id);
    }

    public Collection<Film> findPopularFilms(Long count) {
        return filmStorage.findAll().stream()
                .sorted((a, b) -> Integer.compare(b.getLikes().size(), a.getLikes().size()))
                .limit(count)
                .toList();
    }
}
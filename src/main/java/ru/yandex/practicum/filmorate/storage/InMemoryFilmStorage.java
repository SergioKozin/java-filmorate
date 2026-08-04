package ru.yandex.practicum.filmorate.storage;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.*;

@Component
public class InMemoryFilmStorage implements FilmStorage {
    private final Map<Long, Film> films = new HashMap<>();

    public Collection<Film> findAll() {
        return films.values();
    }

    public Film create(Film film) {
        if (isValidFilm(film)) {
            film.setId(getNextId());
            film.setLikes(new HashSet<>());
            films.put(film.getId(), film);
        }
        return film;
    }

    private boolean isValidFilm(Film film) throws ValidationException {
        if (film.getName() == null || film.getName().isBlank()) {
            throw new ValidationException("Имя не может быть пустым.");
        }
        if (film.getDescription().length() > 200) {
            throw new ValidationException("Длина описания более 200 символов.");
        }
        if (film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
            throw new ValidationException("Дата релиза раньше 28 декабря 1895г.");
        }
        if (film.getDuration() <= 0) {
            throw new ValidationException("Продолжительность фильма - не положительное число.");
        }
        return true;
    }

    private long getNextId() {
        long currentMaxId = films.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }

    public Film update(Film newFilm) {

        if (newFilm.getId() == null) {
            throw new ValidationException("Id должен быть указан");
        }
        if (films.containsKey(newFilm.getId())) {
            Film oldFilm = films.get(newFilm.getId());
            oldFilm.setName(newFilm.getName());
            oldFilm.setDescription(newFilm.getDescription());
            oldFilm.setReleaseDate(newFilm.getReleaseDate());
            oldFilm.setDuration(newFilm.getDuration());
            return oldFilm;
        }
        throw new NotFoundException("Фильм с id = " + newFilm.getId() + " не найден");
    }


    public Optional<Film> findFilmById(Long filmId) {
        return Optional.ofNullable(films.get(filmId));
    }
}

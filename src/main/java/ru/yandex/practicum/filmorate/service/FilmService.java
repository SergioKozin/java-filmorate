package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.mappers.FilmMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.FilmDto;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.time.LocalDate;
import java.util.Collection;

@Service
public class FilmService {
    private final FilmStorage filmStorage;

    private final JdbcTemplate jdbcTemplate;
    private final FilmMapper filmMapper;

    private static final String INSERT_GENRES_QUERY = "INSERT INTO genres_of_film (film_id, genre_id) VALUES (?, ?)";
    private static final String INSERT_LIKE_QUERY = "INSERT INTO likes (film_id, user_id) VALUES (?, ?)";
    private static final String DELETE_LIKE_QUERY = "DELETE FROM likes WHERE film_id = ? AND user_id = ?";

    public FilmService(@Qualifier("FilmDbStorage") FilmStorage filmStorage,
                       JdbcTemplate jdbcTemplate, FilmMapper filmMapper) {
        this.filmStorage = filmStorage;
        this.jdbcTemplate = jdbcTemplate;
        this.filmMapper = filmMapper;
    }

    public Collection<FilmDto> findAll() {

        return filmStorage.findAll().stream()
                .map(filmMapper::mapToDto)
                .toList();
    }

    public FilmDto create(FilmDto filmDto) {
        if (isValidFilm(filmDto)) {
            filmDto.setId(filmStorage.create(filmMapper.mapToFilm(filmDto)).getId());
            if (filmDto.getGenres() != null) {
                filmDto.getGenres().forEach(
                        genre -> {
                            if (genre.getId() < 1 || genre.getId() > 6) {
                                throw new NotFoundException("Жанр фильма должен быть от 1 до 6.");
                            }
                            jdbcTemplate.update(
                                    INSERT_GENRES_QUERY,
                                    filmDto.getId(),
                                    genre.getId()
                            );
                        }
                );
            }
        }
        return filmDto;
    }

    private boolean isValidFilm(FilmDto film) throws ValidationException {
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
        if (film.getMpa().getId() < 1 || film.getMpa().getId() > 5) {
            throw new NotFoundException("Возврастной рейтинг должен быть от 1 до 5.");
        }

        return true;
    }

    public FilmDto update(FilmDto filmDto) {
        if (isValidFilm(filmDto)) {
            if (filmDto.getId() == null) {
                throw new ValidationException("Id должен быть указан");
            }
            if (filmStorage.findFilmById(filmDto.getId()).isPresent()) {
                Film film = filmMapper.mapToFilm(filmDto);
                film.setId(filmDto.getId());
                filmStorage.update(film);
            } else {
                throw new NotFoundException("Фильм с id = " + filmDto.getId() + " не найден.");
            }
        }
        return filmDto;
    }

    public FilmDto findFilmById(long id) {
        return filmMapper.mapToDto(filmStorage.findFilmById(id).orElseThrow());
    }

    public FilmDto addLike(Long filmId, Long userId) {
        jdbcTemplate.update(
                INSERT_LIKE_QUERY,
                filmId,
                userId
        );
        return findFilmById(filmId);
    }

    public FilmDto deleteLike(Long filmId, Long userId) {
        jdbcTemplate.update(
                DELETE_LIKE_QUERY,
                filmId,
                userId
        );
        return findFilmById(filmId);
    }

    public Collection<FilmDto> findPopularFilms(Long count) {
        return findAll().stream()
                .sorted((f1, f2) -> Integer.compare(
                        f2.getLikes().size(),
                        f1.getLikes().size()
                ))
                .limit(count)
                .toList();
    }
}

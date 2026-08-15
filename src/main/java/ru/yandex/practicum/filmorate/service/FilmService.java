package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.FilmDto;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Like;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.mappers.GenreRowMapper;
import ru.yandex.practicum.filmorate.storage.mappers.MpaRowMapper;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;

@Service
public class FilmService {
    private final FilmStorage filmStorage;
    private final GenreRowMapper genreRowMapper;
    private final MpaRowMapper mpaRowMapper;
    private final JdbcTemplate jdbcTemplate;

    private static final String FIND_ALL_LIKES_QUERY = "SELECT * FROM likes WHERE film_id = ?";
    private static final String FIND_ALL_GENRES_QUERY = "SELECT * FROM genres_of_film " +
            "WHERE film_id = ? ORDER BY genre_id ASC";
    private static final String INSERT_GENRES_QUERY = "INSERT INTO genres_of_film (film_id, genre_id) VALUES (?, ?)";
    private static final String INSERT_LIKE_QUERY = "INSERT INTO likes (film_id, user_id) VALUES (?, ?)";
    private static final String DELETE_LIKE_QUERY = "DELETE FROM likes WHERE film_id = ? AND user_id = ?";
    private static final String FIND_GENRE_QUERY = "SELECT * FROM genres WHERE genre_id = ?";
    private static final String FIND_MPA_QUERY = "SELECT * FROM mpa WHERE mpa_id = ?";

    public FilmService(@Qualifier("FilmDbStorage") FilmStorage filmStorage,
                       GenreRowMapper genreRowMapper, MpaRowMapper mpaRowMapper,
                       JdbcTemplate jdbcTemplate) {
        this.filmStorage = filmStorage;
        this.genreRowMapper = genreRowMapper;
        this.mpaRowMapper = mpaRowMapper;
        this.jdbcTemplate = jdbcTemplate;
    }

    public Collection<FilmDto> findAll() {

        return filmStorage.findAll().stream()
                .map(film -> {
                    FilmDto dto = new FilmDto();
                    dto.setId(film.getId());
                    dto.setName(film.getName());
                    dto.setDescription(film.getDescription());
                    dto.setReleaseDate(film.getReleaseDate());
                    dto.setDuration(film.getDuration());
                    dto.setLikes(
                            new HashSet<>(jdbcTemplate.query(FIND_ALL_LIKES_QUERY,
                                    (rs, rowNum) -> {
                                        Like like = new Like();
                                        like.setId(rs.getLong("like_id"));
                                        like.setFilm_id(rs.getLong("film_id"));
                                        like.setUser_id(rs.getLong("user_id"));
                                        return like;
                                    }, film.getId()))
                    );
                    dto.setGenres(
                            new HashSet<>(jdbcTemplate.query(FIND_ALL_GENRES_QUERY,
                                    (rs, rowNum) -> {
                                        Genre genre = new Genre();
                                        genre.setId(rs.getInt("genre_id"));
                                        genre.setName(Objects.requireNonNull(jdbcTemplate.queryForObject(
                                                FIND_GENRE_QUERY,
                                                genreRowMapper,
                                                genre.getId()
                                        )).getName());
                                        return genre;
                                    }, film.getId())));
                    dto.setMpa(Objects.requireNonNull(
                            jdbcTemplate.queryForObject(
                                    FIND_MPA_QUERY,
                                    mpaRowMapper,
                                    film.getMpa().getId())));
                    return dto;
                })
                .toList();
    }

    public FilmDto create(FilmDto filmDto) {
        if (isValidFilm(filmDto)) {
            Film film = new Film();
            film.setName(filmDto.getName());
            film.setDescription(filmDto.getDescription());
            film.setReleaseDate(filmDto.getReleaseDate());
            film.setDuration(filmDto.getDuration());
            film.setMpa(filmDto.getMpa());
            filmDto.setId(filmStorage.create(film).getId());
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
                Film film = new Film();
                film.setId(filmDto.getId());
                film.setName(filmDto.getName());
                film.setDescription(filmDto.getDescription());
                film.setReleaseDate(filmDto.getReleaseDate());
                film.setDuration(filmDto.getDuration());
                film.setMpa(filmDto.getMpa());
                filmStorage.update(film);
            } else {
                throw new NotFoundException("Фильм с id = " + filmDto.getId() + " не найден.");
            }
        }
        return filmDto;
    }

    public FilmDto findFilmById(long id) {
        Film film = filmStorage.findFilmById(id).orElseThrow();
        FilmDto filmDto = new FilmDto();
        filmDto.setId(film.getId());
        filmDto.setName(film.getName());
        filmDto.setDescription(film.getDescription());
        filmDto.setReleaseDate(film.getReleaseDate());
        filmDto.setDuration(film.getDuration());
        filmDto.setLikes(
                new HashSet<>(jdbcTemplate.query(FIND_ALL_LIKES_QUERY,
                        (rs, rowNum) -> {
                            Like like = new Like();
                            like.setId(rs.getLong("like_id"));
                            like.setFilm_id(rs.getLong("film_id"));
                            like.setUser_id(rs.getLong("user_id"));
                            return like;
                        }, film.getId()))
        );
        filmDto.setGenres(
                new HashSet<>(jdbcTemplate.query(FIND_ALL_GENRES_QUERY,
                        (rs, rowNum) -> {
                            Genre genre = new Genre();
                            genre.setId(rs.getInt("genre_id"));
                            genre.setName(Objects.requireNonNull(jdbcTemplate.queryForObject(
                                    FIND_GENRE_QUERY,
                                    genreRowMapper,
                                    genre.getId()
                            )).getName());
                            return genre;
                        },
                        film.getId())));

        filmDto.setMpa(Objects.requireNonNull(
                jdbcTemplate.queryForObject(
                        FIND_MPA_QUERY,
                        mpaRowMapper,
                        film.getMpa().getId())));
        return filmDto;
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

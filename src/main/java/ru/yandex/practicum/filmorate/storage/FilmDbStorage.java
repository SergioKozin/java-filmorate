package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.mappers.FilmRowMapper;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Collection;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
@Component("FilmDbStorage")
public class FilmDbStorage implements FilmStorage {
    private final JdbcTemplate jdbcTemplate;
    private final FilmRowMapper filmRowMapper;
    private static final String FIND_ALL_QUERY = "SELECT * FROM films";
    private static final String INSERT_FILM_QUERY = "INSERT INTO films(film_name, description, release_date, " +
            "duration, mpa) VALUES (?, ?, ?, ?, ?)";
    private static final String UPDATE_FILM_QUERY = "UPDATE films SET film_name = ?, description = ?, " +
            "release_date = ?, duration = ?, mpa = ? WHERE id = ?";
    private static final String FIND_FILM_BY_ID_QUERY = "SELECT * FROM films WHERE id = ?";

    @Override
    public Collection<Film> findAll() {
        try {
            return jdbcTemplate.query(FIND_ALL_QUERY, filmRowMapper);
        } catch (EmptyResultDataAccessException ignored) {
            throw new NoSuchElementException();
        }
    }

    @Override
    public Film create(Film film) {
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    INSERT_FILM_QUERY,
                    Statement.RETURN_GENERATED_KEYS
            );
            ps.setString(1, film.getName());
            ps.setString(2, film.getDescription());
            ps.setObject(3, film.getReleaseDate());
            ps.setLong(4, film.getDuration());
            ps.setInt(5, film.getMpa().getId());
            return ps;
        }, keyHolder);
        film.setId(Objects.requireNonNull(keyHolder.getKey()).longValue());

        return film;
    }

    @Override
    public Film update(Film film) {
        try {
            int rowsUpdated = jdbcTemplate.update(
                    UPDATE_FILM_QUERY,
                    film.getName(),
                    film.getDescription(),
                    film.getReleaseDate(),
                    film.getDuration(),
                    film.getMpa().getId(),
                    film.getId()
            );
            if (rowsUpdated == 0) {
                throw new ValidationException("Не удалось обновить данные");
            }
        } catch (DataAccessException e) {
            throw new ValidationException(e.getMessage());
        }
        return film;
    }

    @Override
    public Optional<Film> findFilmById(Long id) {
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(FIND_FILM_BY_ID_QUERY, filmRowMapper, id));
        } catch (EmptyResultDataAccessException ignored) {
            return Optional.empty();
        }
    }
}

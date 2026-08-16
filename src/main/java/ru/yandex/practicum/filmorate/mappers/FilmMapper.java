package ru.yandex.practicum.filmorate.mappers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.FilmDto;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Like;
import ru.yandex.practicum.filmorate.storage.mappers.GenreRowMapper;
import ru.yandex.practicum.filmorate.storage.mappers.MpaRowMapper;

import java.util.HashSet;
import java.util.Objects;


@Component
public class FilmMapper {
    private final JdbcTemplate jdbcTemplate;
    private final GenreRowMapper genreRowMapper;
    private final MpaRowMapper mpaRowMapper;

    private static final String FIND_ALL_LIKES_QUERY = "SELECT * FROM likes WHERE film_id = ?";
    private static final String FIND_ALL_GENRES_QUERY = "SELECT * FROM genres_of_film " +
            "WHERE film_id = ? ORDER BY genre_id ASC";
    private static final String FIND_GENRE_QUERY = "SELECT * FROM genres WHERE id = ?";
    private static final String FIND_MPA_QUERY = "SELECT * FROM mpa WHERE id = ?";
@Autowired
    public FilmMapper(JdbcTemplate jdbcTemplate, GenreRowMapper genreRowMapper, MpaRowMapper mpaRowMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.genreRowMapper = genreRowMapper;
        this.mpaRowMapper = mpaRowMapper;
    }

    public FilmDto mapToDto(Film film) {
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
                            like.setId(rs.getLong("id"));
                            like.setFilmId(rs.getLong("film_id"));
                            like.setUserId(rs.getLong("user_id"));
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
    }

    public Film mapToFilm(FilmDto filmDto) {
        Film film = new Film();
        film.setName(filmDto.getName());
        film.setDescription(filmDto.getDescription());
        film.setReleaseDate(filmDto.getReleaseDate());
        film.setDuration(filmDto.getDuration());
        film.setMpa(filmDto.getMpa());

        return film;
    }
}

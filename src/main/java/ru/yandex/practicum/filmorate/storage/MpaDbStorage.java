package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.mappers.MpaRowMapper;

import java.util.Collection;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
@Component
public class MpaDbStorage {
    private final JdbcTemplate jdbcTemplate;
    private final MpaRowMapper mpaRowMapper;

    private static final String FIND_ALL_MPA = "SELECT * FROM mpa";
    private static final String FIND_MPA_BY_ID = "SELECT * FROM mpa WHERE id = ?";

    public Collection<Mpa> findAll() {
        return jdbcTemplate.query(FIND_ALL_MPA, mpaRowMapper);
    }

    public Optional<Mpa> findMpaById(Long id) {
        try {
            Mpa result = jdbcTemplate.queryForObject(FIND_MPA_BY_ID, mpaRowMapper, id);
            return Optional.ofNullable(result);
        } catch (EmptyResultDataAccessException ignored) {
            return Optional.empty();
        }
    }
}

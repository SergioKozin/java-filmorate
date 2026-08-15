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

    public Collection<Mpa> findAll() {
        String query = "SELECT * FROM mpa";
        return jdbcTemplate.query(query, mpaRowMapper);
    }

    public Optional<Mpa> findMpaById(Long id) {
        String query = "SELECT * FROM mpa WHERE mpa_id = ?";
        try {
            Mpa result = jdbcTemplate.queryForObject(query, mpaRowMapper, id);
            return Optional.ofNullable(result);
        } catch (EmptyResultDataAccessException ignored) {
            return Optional.empty();
        }
    }
}

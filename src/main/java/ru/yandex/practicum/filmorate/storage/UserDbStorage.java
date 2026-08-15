package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.DuplicatedDataException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.mappers.UserRowMapper;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.*;

@Component("UserDbStorage")
@Repository
@RequiredArgsConstructor
public class UserDbStorage implements UserStorage {
    private final JdbcTemplate jdbcTemplate;
    private final UserRowMapper userRowMapper;
    private static final String FIND_ALL_QUERY = "SELECT * FROM users";
    private static final String FIND_BY_ID_QUERY = "SELECT * FROM users WHERE user_id = ?";
    private static final String FIND_ALL_FRIENDS_QUERY = "SELECT friend_id FROM friendship WHERE user_id = ?";
    private static final String INSERT_USER_QUERY = "INSERT INTO users(email, login, user_name, birthday)" +
            "VALUES (?, ?, ?, ?)";
    private static final String FIND_LOGIN = "SELECT count(*) FROM users WHERE login = ?";
    private static final String UPDATE_QUERY = "UPDATE users SET user_name = ?, email = ?, " +
            "login = ?, birthday = ? WHERE user_id = ?";

    @Override
    public Collection<User> findAll() {
        try {
            List<User> result = jdbcTemplate.query(FIND_ALL_QUERY, userRowMapper);
            result.forEach(u -> u.setFriends(
                            new HashSet<>(jdbcTemplate.queryForList(FIND_ALL_FRIENDS_QUERY, Long.class, u.getId()))
                    )
            );
            return result;
        } catch (EmptyResultDataAccessException ignored) {
            throw new NoSuchElementException();
        }
    }

    @Override
    public User create(User user) {
        if (isValidUser(user)) {
            try {
                KeyHolder keyHolder = new GeneratedKeyHolder();

                jdbcTemplate.update(connection -> {
                    PreparedStatement ps = connection.prepareStatement(
                            INSERT_USER_QUERY,
                            Statement.RETURN_GENERATED_KEYS
                    );

                    ps.setString(1, user.getEmail());
                    ps.setString(2, user.getLogin());
                    ps.setString(3, user.getName());
                    ps.setObject(4, user.getBirthday());

                    return ps;
                }, keyHolder);
                user.setId(Objects.requireNonNull(keyHolder.getKey()).longValue());


            } catch (DuplicateKeyException e) {
                throw new DuplicatedDataException(
                        "Пользователь с таким email или login уже существует"
                );
            }

        }
        return user;
    }

    private boolean isValidUser(User user) throws ValidationException {
        if (user.getLogin() == null || user.getLogin().isBlank() || user.getLogin().contains(" ")) {
            throw new ValidationException("Логин должен быть указан и не содержать пробелов.");
        }
        Integer countLogins;
        try {
            countLogins = jdbcTemplate.queryForObject(FIND_LOGIN, Integer.class, user.getLogin());
        } catch (DataAccessException e) {
            throw new ValidationException(e.getMessage());
        }
        if (countLogins != null && countLogins > 0) {
            throw new ValidationException("Этот логин уже используется");
        }
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        if (user.getBirthday().isAfter(LocalDate.now())) {
            throw new ValidationException("Дата рождения не может быть в будущем.");
        }
        return true;
    }

    @Override
    public User update(User newUser) {
        if (newUser.getId() == null) {
            throw new ValidationException("Id должен быть указан");
        }
        if (findUserById(newUser.getId()).isPresent()) {
            try {
                jdbcTemplate.update(
                        UPDATE_QUERY,
                        newUser.getName(),
                        newUser.getEmail(),
                        newUser.getLogin(),
                        newUser.getBirthday(),
                        newUser.getId()
                );
            } catch (DataAccessException e) {
                throw new ValidationException(e.getMessage());
            }
        } else {
            throw new NotFoundException("Пользователь с id = " + newUser.getId() + " не найден");
        }
        return newUser;
    }

    @Override
    public Optional<User> findUserById(Long id) {
        try {
            User result = jdbcTemplate.queryForObject(FIND_BY_ID_QUERY, userRowMapper, id);
            if (result != null) {
                result.setFriends(new HashSet<>(jdbcTemplate.queryForList(FIND_ALL_FRIENDS_QUERY, Long.class, id)));
            }
            return Optional.ofNullable(result);
        } catch (EmptyResultDataAccessException ignored) {
            return Optional.empty();
        }
    }
}

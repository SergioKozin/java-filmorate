package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.Collection;
import java.util.HashSet;

@Service
public class UserService {
    private final UserStorage userStorage;
    private final JdbcTemplate jdbcTemplate;
    private static final String INSERT_FRIEND_QUERY = "INSERT INTO friendship (user_id, friend_id) VALUES (?, ?)";
    private static final String DELETE_FRIEND_QUERY = "DELETE FROM friendship WHERE user_id = ? AND friend_id = ?";
    private static final String FIND_ALL_FRIENDS_QUERY = "SELECT * FROM friendship WHERE user_id = ?";
    private static final String FIND_COMMON_FRIENDS_QUERY = "SELECT friend_id FROM friendship " +
            "WHERE user_id = ? OR user_id = ? GROUP BY friend_id HAVING COUNT(*) > 1";

    public UserService(@Qualifier("UserDbStorage") UserStorage userStorage, JdbcTemplate jdbcTemplate) {
        this.userStorage = userStorage;
        this.jdbcTemplate = jdbcTemplate;
    }

    public Collection<User> findAll() {
        return userStorage.findAll();
    }

    public User create(User user) {
        return userStorage.create(user);
    }

    public User update(User newUser) {
        return userStorage.update(newUser);
    }

    public User findUserById(long id) {
        return userStorage.findUserById(id).orElseThrow();
    }

    public User addFriend(Long userId, Long friendId) {
        if (isUserPresent(userId) && isUserPresent(friendId)) {
            try {
                jdbcTemplate.update(
                        INSERT_FRIEND_QUERY,
                        userId,
                        friendId
                );
            } catch (DataAccessException e) {
                throw new ValidationException(e.getMessage());
            }
        }
        return findUserById(userId);
    }

    public User deleteFriend(Long userId, Long friendId) {
        if (isUserPresent(userId) && isUserPresent(friendId)) {
            try {
                jdbcTemplate.update(
                        DELETE_FRIEND_QUERY,
                        userId,
                        friendId
                );
            } catch (DataAccessException e) {
                throw new ValidationException(e.getMessage());
            }
        }

        return findUserById(userId);
    }

    public Boolean isUserPresent(Long userId) {
        userStorage.findUserById(userId)
                .orElseThrow(() ->
                        new NotFoundException("Пользователь с id = " + userId + " не найден"));
        return true;
    }

    public Collection<User> findAllFriends(Long userId) {
        if (isUserPresent(userId)) {
            try {
                return new HashSet<>(jdbcTemplate.query(FIND_ALL_FRIENDS_QUERY,
                        (rs, rowNum) -> findUserById(rs.getLong("friend_id")), userId));
            } catch (DataAccessException e) {
                throw new ValidationException(e.getMessage());
            }
        } else {
            throw new NotFoundException("Пользователь с id = " + userId + " не найден");
        }
    }

    public Collection<User> findCommonFriends(Long userId, Long otherId) {
        return new HashSet<>(jdbcTemplate.query(FIND_COMMON_FRIENDS_QUERY,
                (rs, rowNum) -> findUserById(rs.getLong("friend_id")), userId, otherId));
    }
}

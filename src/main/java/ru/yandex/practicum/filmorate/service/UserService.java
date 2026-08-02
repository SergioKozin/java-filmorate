package ru.yandex.practicum.filmorate.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.Collection;
import java.util.Optional;

@Service
public class UserService {
    private final UserStorage userStorage;

    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public Optional<User> addFriend(Long id, Long friendId) {
        userStorage.findUserById(id).orElseThrow().getFriends().add(friendId);
        return userStorage.findUserById(id);
    }

    public Optional<User> deleteFriend(Long id, Long friendId) {
        userStorage.findUserById(id).orElseThrow().getFriends().remove(friendId);
        userStorage.findUserById(friendId).orElseThrow().getFriends().remove(id);
        return userStorage.findUserById(id);
    }

    public Collection<User> findAllFriends(Long id) {
        return userStorage.findUserById(id).orElseThrow().getFriends()
                .stream()
                .map(userStorage::findUserById)
                .map(Optional::orElseThrow)
                .toList();
    }

    public Collection<User> findCommonFriends(Long id, Long otherId) {
        return userStorage.findUserById(id).orElseThrow().getFriends()
                .stream()
                .filter(userStorage.findUserById(otherId).orElseThrow().getFriends()::contains)
                .map(userStorage::findUserById)
                .map(Optional::orElseThrow)
                .toList();
    }
}

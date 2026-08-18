package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.GenreDbStorage;
import ru.yandex.practicum.filmorate.storage.MpaDbStorage;
import ru.yandex.practicum.filmorate.storage.UserDbStorage;
import ru.yandex.practicum.filmorate.storage.mappers.GenreRowMapper;
import ru.yandex.practicum.filmorate.storage.mappers.MpaRowMapper;
import ru.yandex.practicum.filmorate.storage.mappers.UserRowMapper;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@JdbcTest
@AutoConfigureTestDatabase
@Import({
        UserDbStorage.class,
        UserRowMapper.class,
        GenreDbStorage.class,
        MpaDbStorage.class,
        GenreRowMapper.class,
        MpaRowMapper.class
})
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class FilmorateApplicationTests {
    private final UserDbStorage userDbStorage;

    @Test
    @Order(1)
    public void testFindUserById() {
        User newUser = new User();
        newUser.setLogin("Sergio");
        newUser.setName("Sergei");
        newUser.setEmail("sergio@mail.ru");
        newUser.setBirthday(LocalDate.parse("1900-10-27"));
        userDbStorage.create(newUser);

        Optional<User> userOptional = userDbStorage.findUserById(1L);
        assertThat(userOptional)
                .isPresent()
                .hasValueSatisfying(user ->
                        assertThat(user).hasFieldOrPropertyWithValue("id", 1L)
                );
    }

    @Test
    @Order(2)
    public void testFindAll() {

        User newUser2 = new User();
        newUser2.setLogin("Sergio2");
        newUser2.setName("Sergei2");
        newUser2.setEmail("sergio2@mail.ru");
        newUser2.setBirthday(LocalDate.parse("1900-10-27"));
        userDbStorage.create(newUser2);

        User newUser3 = new User();
        newUser3.setLogin("Sergio3");
        newUser3.setName("Sergei3");
        newUser3.setEmail("sergio3@mail.ru");
        newUser3.setBirthday(LocalDate.parse("1900-10-28"));
        userDbStorage.create(newUser3);

        Collection<User> users = userDbStorage.findAll();

        assertEquals(2, users.size());
    }
}

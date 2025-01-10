package ru.yandex.practicum.filmorate.dal;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.mapper.UserRowMapper;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Collection;

@Slf4j
@Primary
@Repository
@RequiredArgsConstructor
public class UserDbStorage implements UserStorage {

    private final JdbcTemplate jdbcTemplate;
    private final UserRowMapper userRowMapper;

    @Override
    public Collection<User> getUsers() {
        String query = "SELECT * FROM users";
        return jdbcTemplate.query(query, userRowMapper);
    }

    @Override
    public User addUser(User user) {
        String query = "INSERT INTO PUBLIC.USERS(username, email, login, birthday) VALUES (?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(con -> createPreparedStatement(con, query, user), keyHolder);

        return handleGeneratedKey(user, keyHolder);
    }

    private PreparedStatement createPreparedStatement(Connection con, String query, User user) throws SQLException {
        PreparedStatement ps = con.prepareStatement(query, new String[]{"id"});
        ps.setString(1, user.getName());
        ps.setString(2, user.getEmail());
        ps.setString(3, user.getLogin());
        ps.setTimestamp(4, user.getBirthday() != null ? Timestamp.valueOf(user.getBirthday().atStartOfDay()) : null);
        return ps;
    }

    private User handleGeneratedKey(User user, KeyHolder keyHolder) {
        Number key = keyHolder.getKey();
        if (key != null) {
            user.setId(key.longValue());
            return user;
        } else {
            throw new RuntimeException("Не удалось получить сгенерированный ID для нового пользователя.");
        }
    }

    @Override
    public User updateUser(User newUser) {
        User existingUser = getUserById(newUser.getId());
        updateUserDetails(newUser);
        return newUser;
    }

    private void updateUserDetails(User user) {
        String query = "UPDATE PUBLIC.USERS SET email = ?, login = ?, username = ?, birthday = ? WHERE id = ?";
        jdbcTemplate.update(query,
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                user.getBirthday(),
                user.getId());
    }

    @Override
    public User getUserById(Long id) {
        String query = "SELECT * FROM PUBLIC.USERS WHERE id = ?";
        try {
            return jdbcTemplate.queryForObject(query, userRowMapper, id);
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException(String.format("Пользователь с ID: %d не найден", id));

        }
    }

    @Override
    public void deleteUserById(Long id) {
        getUserById(id);
        String query = "DELETE FROM users WHERE id = ?";
        jdbcTemplate.update(query, id);
    }
}
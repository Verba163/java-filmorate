package ru.yandex.practicum.filmorate.dal;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.mapper.UserRowMapper;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.FriendshipStorage;

import java.sql.PreparedStatement;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Repository
@Primary
@RequiredArgsConstructor
public class FriendshipDbStorage implements FriendshipStorage {
    private final JdbcTemplate jdbcTemplate;
    private final UserRowMapper mapper;
    private final UserDbStorage userDbStorage;

    @Override
    public User addFriend(Long user1Id, Long user2Id) {
        User user = findUserById(user1Id);
        User friend = findUserById(user2Id);

        initializeFriendsSet(user);
        initializeFriendsSet(friend);

        if (areAlreadyFriends(user1Id, user2Id)) {
            log.warn("Пользователь {} и {} уже являются друзьями.", user1Id, user2Id);
            return user;
        }

        addFriendshipToDatabase(user1Id, user2Id);
        updateFriendshipInMemory(user, friend, user2Id);

        log.info("Пользователь {} добавил {} в друзья.", user1Id, user2Id);
        return user;
    }

    private User findUserById(Long userId) {
        return userDbStorage.getUsers().stream()
                .filter(u -> u.getId().equals(userId))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Пользователь с ID " + userId + " не найден"));
    }

    private void initializeFriendsSet(User user) {
        if (user.getFriendsId() == null) {
            user.setFriendsId(new HashSet<>());
        }
    }

    private boolean areAlreadyFriends(Long user1Id, Long user2Id) {
        String queryUser2 = "SELECT user2_id FROM friendship WHERE user1_id = ?";
        List<Long> user2Ids = jdbcTemplate.queryForList(queryUser2, Long.class, user1Id);
        return user2Ids.contains(user2Id);
    }

    private void addFriendshipToDatabase(Long user1Id, Long user2Id) {
        String queryAddFriend = "INSERT INTO friendship(user1_id, user2_id) VALUES (?, ?)";
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(queryAddFriend);
            ps.setLong(1, user1Id);
            ps.setLong(2, user2Id);
            return ps;
        });
    }

    private void updateFriendshipInMemory(User user, User friend, Long user2Id) {
        user.getFriendsId().add(user2Id);
        friend.getFriendsId().add(user.getId());
    }

    @Override
    public User removeFriend(Long userId, Long friendId) {
        User user = findUserById(userId);
        User friend = findUserById(friendId);

        boolean wasRemoved = Optional.ofNullable(user.getFriendsId()).orElse(new HashSet<>()).remove(friendId);
        Optional.ofNullable(friend.getFriendsId()).orElse(new HashSet<>()).remove(userId);

        int deletedRows = deleteFriendshipFromDatabase(userId, friendId);

        if (!wasRemoved) {
            log.warn("Пользователь {} и {} не были друзьями.", userId, friendId);
        } else {
            log.info("Удалено {} строк", deletedRows);
            log.info("Пользователь с id = {} удалил из друзей пользователя с id = {}", userId, friendId);
        }

        return user;
    }

    private int deleteFriendshipFromDatabase(Long userId, Long friendId) {
        String query = "DELETE FROM friendship WHERE user1_id = ? AND user2_id = ?";
        return jdbcTemplate.update(query, userId, friendId);
    }

    @Override
    public Collection<User> getFriends(Long userId) {
        String sqlQueryUser2 = "SELECT user2_id FROM friendship WHERE user1_id = ?";
        List<Long> friendsId = jdbcTemplate.queryForList(sqlQueryUser2, Long.class, userId);

        return userDbStorage.getUsers().stream()
                .filter(user -> friendsId.contains(user.getId()))
                .collect(Collectors.toList());
    }

    @Override
    public Collection<User> getCommonFriends(Long user1Id, Long user2Id) {
        validateUsersExist(user1Id, user2Id);

        String sqlCommonFriends = """
                SELECT u.id, u.email, u.login, u.username, u.birthday\s
                FROM users u\s
                JOIN friendship f ON u.id = f.user2_id\s
                WHERE f.user1_id = ? AND u.id IN (
                  SELECT user2_id FROM friendship WHERE user1_id = ?)
               \s""";

        return new HashSet<>(jdbcTemplate.query(sqlCommonFriends, mapper::mapRow, user1Id, user2Id));
    }

    private void validateUsersExist(Long user1Id, Long user2Id) {
        if (userDbStorage.getUserById(user1Id) == null) {
            log.error("Пользователь с id = {} не найден", user1Id);
            throw new NotFoundException("Пользователь с id = " + user1Id + " не найден");
        }

        if (userDbStorage.getUserById(user2Id) == null) {
            log.error("Пользователь с id = {} не найден", user2Id);
            throw new NotFoundException("Пользователь с id = " + user2Id + " не найден");
        }
    }
}
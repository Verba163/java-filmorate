package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Service
public class UserService {
    private final UserStorage userStorage;

    @Autowired
    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public Collection<User> getUsers() {
        return userStorage.getUsers();
    }

    public User addUser(User user) {
        user.setFriendsId(new HashSet<>());
        return userStorage.addUser(user);
    }

    public User updateUser(User user) {
        if (user.getId() == null) {
            log.warn("Id должен быть указан");
            throw new ConditionsNotMetException("Id должен быть указан");
        }
        if (!getUsers().stream().anyMatch(u -> u.getId().equals(user.getId()))) {
            log.error("Пользователь с ID {} не существует", user.getId());
            throw new NotFoundException("Пользователь с ID" + user.getId() + " не найден");
        }
        return userStorage.updateUser(user);
    }

    public User addFriend(Long userId, Long friendId) {
        User user = userStorage.getUsers().stream()
                .filter(u -> u.getId().equals(userId))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Пользователь с ID" + userId + " не найден"));
        User friend = userStorage.getUsers().stream()
                .filter(u -> u.getId().equals(friendId))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Друг с ID " + friendId + " не найден"));

        if (user.getFriendsId() == null) {
            user.setFriendsId(new HashSet<>());
        }
        if (friend.getFriendsId() == null) {
            friend.setFriendsId(new HashSet<>());
        }
        if (!user.getFriendsId().contains(friendId)) {
            user.getFriendsId().add(friendId);
            friend.getFriendsId().add(userId);
            log.info("Пользователь {} добавил {} в друзья.", userId, friendId);

        } else {
            log.warn("Пользователь {} и {} уже являются друзьями.", userId, friendId);
        }
        return user;
    }

    public ResponseEntity<String> removeFriend(Long userId1, Long userId2) {
        User user = findUserById(userId1);
        User user2 = findUserById(userId2);

        boolean wasRemoved = Optional.ofNullable(user.getFriendsId()).orElse(new HashSet<>()).remove(userId2);
        Optional.ofNullable(user2.getFriendsId()).orElse(new HashSet<>()).remove(userId1);

        if (!wasRemoved) {
            log.warn("Пользователь {} и {} не были друзьями.", userId1, userId2);
            return ResponseEntity.ok("Не друзья");
        }
        return ResponseEntity.ok("Друг успешно удален");
    }

    public Set<Long> getCommonFriends(Long userId1, Long userId2) {
        User user1 = findUserById(userId1);

        User user2 = findUserById(userId2);
        if (!userExists(userId1)) {
            throw new NotFoundException("Пользователь с ID " + userId1 + " не найден");
        }

        if (!userExists(userId2)) {
            throw new NotFoundException("Пользователь с ID " + userId2 + " не найден");
        }
        Set<Long> commonFriends = new HashSet<>(Optional.ofNullable(user1.getFriendsId()).orElse(new HashSet<>()));
        commonFriends.retainAll(Optional.ofNullable(user2.getFriendsId()).orElse(new HashSet<>()));
        return commonFriends;
    }

    public Set<Long> getFriends(long userId) {
        User user = findUserById(userId);
        return Optional.ofNullable(user.getFriendsId()).orElse(new HashSet<>());
    }

    public boolean userExists(Long id) {
        return userStorage.getUsers().stream().anyMatch(user -> user.getId().equals(id));
    }

    private User findUserById(Long userId) {
        return userStorage.getUsers().stream()
                .filter(u -> u.getId().equals(userId))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Пользователь с ID " + userId + " не найден"));
    }
}

package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

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
        return userStorage.addUser(user);
    }
    public User updateUser(User user) {
        if (!userExists(user.getId())) {
            throw new NotFoundException("Пользователь не найден для ID: " + user.getId());
        }
        return userStorage.updateUser(user);
    }

    public void addFriend(Long userId, Long friendId) {
        User user = userStorage.getUsers().stream()
                .filter(u -> u.getId().equals(userId))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));
        User friend = userStorage.getUsers().stream()
                .filter(u -> u.getId().equals(friendId))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Друг не найден"));

        if (user.getFriends() == null) {
            user.setFriends(new HashSet<>());
        }
        if (friend.getFriends() == null) {
            friend.setFriends(new HashSet<>());
        }
        if (!user.getFriends().contains(friend)) {
            user.getFriends().add(friend);
            friend.getFriends().add(user);
            log.info("Пользователь {} добавил {} в друзья.", userId, friendId);
        } else {
            log.warn("Пользователь {} и {} уже являются друзьями.", userId, friendId);
        }
    }

    public ResponseEntity<String> removeFriend(Long userId1, Long userId2) {
        User user = userStorage.getUsers().stream()
                .filter(u -> u.getId().equals(userId1))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        User user2 = userStorage.getUsers().stream()
                .filter(u -> u.getId().equals(userId2))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Друг не найден"));

        if (user.getFriends() == null) {
            user.setFriends(new HashSet<>());
        }
        if (user2.getFriends() == null) {
            user2.setFriends(new HashSet<>());
        }

        boolean wasRemoved = user.getFriends().remove(user2);
        user2.getFriends().remove(user);

        if (!wasRemoved) {
            log.warn("Пользователь {} и {} не были друзьями.", userId1, userId2);
            return ResponseEntity.ok("Не друзья");
        }
        return ResponseEntity.ok("Друг успешно удален");
    }

    public Set<User> getCommonFriends(Long userId1, Long userId2) {
        User user1 = userStorage.getUsers().stream()
                .filter(u -> u.getId().equals(userId1))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Пользователь 1 не найден"));

        User user2 = userStorage.getUsers().stream()
                .filter(u -> u.getId().equals(userId2))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Пользователь 2 не найден"));

        return user1.getFriends().stream()
                .filter(user2.getFriends()::contains)
                .collect(Collectors.toSet());
    }

    public Set<User> getFriends(Long userId) {
        User user = userStorage.getUsers().stream()
                .filter(u -> u.getId().equals(userId))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        return Optional.ofNullable(user.getFriends()).orElse(new HashSet<>());
    }

    public boolean userExists(Long id) {
        return userStorage.getUsers().stream().anyMatch(user -> user.getId().equals(id));
    }
}

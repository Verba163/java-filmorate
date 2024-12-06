package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.*;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<Collection<User>> getUsers() {
        log.info("Получение всех пользователей. Всего пользователей {}", userService.getUsers().size());
        Collection<User> users = userService.getUsers();
        return ResponseEntity.ok(users);
    }

    @PostMapping
    public User create(@Valid @RequestBody User user) {
        log.warn("Создание нового пользователя: {}", user);
        return userService.addUser(user);
    }

    @PutMapping
    public User update(@Valid @RequestBody User newUser) {
        if (newUser.getId() == null) {
            log.warn("Id должен быть указан");
            throw new ConditionsNotMetException("Id должен быть указан");
        }

        if (!userService.getUsers().stream().anyMatch(u -> u.getId().equals(newUser.getId()))) {
            log.error("Пользователь с ID {} не существует", newUser.getId());
            throw new NotFoundException("Пользователь не найден для ID: " + newUser.getId());
        }

        return userService.updateUser(newUser);
    }

    @PutMapping("/{id}/friends/{friendId}")
    public ResponseEntity<Map<String, String>> addFriend(@PathVariable Long id, @PathVariable Long friendId) {
        if (!userService.userExists(id)) {
            throw new NotFoundException("Пользователь с ID " + id + " не найден");
        }

        if (!userService.userExists(friendId)) {
            throw new NotFoundException("Пользователь с ID " + friendId + " не найден");
        }
        userService.addFriend(id, friendId);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Друг успешно добавлен");
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}/friends/{friendId}")
    public ResponseEntity<String> removeFriend(@PathVariable Long id, @PathVariable Long friendId) {

        if (!userService.userExists(id)) {
            throw new NotFoundException("Пользователь с ID " + id + " не найден");
        }

        if (!userService.userExists(friendId)) {
            throw new NotFoundException("Пользователь с ID " + friendId + " не найден");
        }
        ResponseEntity<String> response = userService.removeFriend(id, friendId);
        return ResponseEntity.status(response.getStatusCode()).body(response.getBody());

    }

    @GetMapping("/{id}/friends")
    public ResponseEntity<Set<User>> getFriends(@PathVariable Long id) {
        Set<User> friends = userService.getFriends(id);
        return ResponseEntity.ok(friends);

    }

    @GetMapping("/{id}/friends/common/{otherId}")
    public ResponseEntity<Set<User>> getCommonFriends(@PathVariable Long id, @PathVariable Long otherId) {
        if (!userService.userExists(id)) {
            throw new NotFoundException("Пользователь с ID " + id + " не найден");
        }

        if (!userService.userExists(otherId)) {
            throw new NotFoundException("Пользователь с ID " + otherId + " не найден");
        }
        Set<User> commonFriends = userService.getCommonFriends(id, otherId);
        return ResponseEntity.ok(commonFriends);
    }
}

package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;

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
        log.info("Создание нового пользователя: {}", user);
        return userService.addUser(user);
    }

    @PutMapping
    public User update(@Valid @RequestBody User newUser) {
        log.info("Пользователь обновлен {}", newUser);
        return userService.updateUser(newUser);
    }

    @PutMapping("/{id}/friends/{friend-id}")
    public User addFriend(@PathVariable("id") Long id, @PathVariable("friend-id") Long friendId) {
        return userService.addFriend(id, friendId);
    }


    @DeleteMapping("/{id}/friends/{friend-id}")
    public ResponseEntity<String> removeFriend(@PathVariable("id") Long id, @PathVariable("friend-id") Long friendId) {
        ResponseEntity<String> response = userService.removeFriend(id, friendId);
        return ResponseEntity.status(response.getStatusCode()).body(response.getBody());

    }

    @GetMapping("/{id}/friends")
    public ResponseEntity<Set<Long>> getFriends(@PathVariable Long id) {
        Set<Long> friends = userService.getFriends(id);
        return ResponseEntity.ok(friends);

    }

    @GetMapping("/{id}/friends/common/{other-id}")
    public ResponseEntity<Set<Long>> getCommonFriends(@PathVariable("id") Long id, @PathVariable("other-id") Long otherId) {
        Set<Long> commonFriends = userService.getCommonFriends(id, otherId);
        return ResponseEntity.ok(commonFriends);
    }
}

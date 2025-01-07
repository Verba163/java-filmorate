package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.dto.UserDto;
import ru.yandex.practicum.filmorate.service.UserService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;
    private static final String FRIENDS_PATH = "/{id}/friends/{friendId}";

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public List<UserDto> getUsers() {
        List<UserDto> users = userService.getUsers();
        log.info("Получение всех пользователей. Всего пользователей: {}", users.size());
        return users;
    }

    @PostMapping
    public UserDto create(@Valid @RequestBody UserDto userDto) {
        log.info("Создание нового пользователя: {}", userDto);
        return userService.addUser(userDto);
    }

    @PutMapping
    public UserDto updateUser(@Valid @RequestBody UserDto userDto) {
        log.info("Обновление пользователя: {}", userDto);
        return userService.updateUser(userDto);
    }

    @PutMapping(FRIENDS_PATH)
    public UserDto addFriend(@PathVariable("id") Long id, @PathVariable("friendId") Long friendId) {
        log.info("Добавление друга с ID {} к пользователю с ID {}", friendId, id);
        return userService.addFriend(id, friendId);
    }

    @DeleteMapping(FRIENDS_PATH)
    public UserDto removeFriend(@PathVariable("id") Long id, @PathVariable("friendId") Long friendId) {
        log.info("Удаление друга с ID {} у пользователя с ID {}", friendId, id);
        return userService.removeFriend(id, friendId);
    }

    @GetMapping("/{id}/friends")
    public List<UserDto> getFriends(@PathVariable Long id) {
        log.info("Получение списка друзей пользователя с ID {}", id);
        Collection<UserDto> friends = userService.getFriends(id);
        return new ArrayList<>(friends);
    }

    @GetMapping("/{id}/friends/common/{otherId}")
    public List<UserDto> getCommonFriends(@PathVariable("id") Long id, @PathVariable("otherId") Long otherId) {
        log.info("Получение общих друзей между пользователями с ID {} и ID {}", id, otherId);
        Collection<UserDto> commonFriends = userService.getCommonFriends(id, otherId);
        return new ArrayList<>(commonFriends);
    }
}
package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dal.FriendshipDbStorage;
import ru.yandex.practicum.filmorate.dal.UserDbStorage;
import ru.yandex.practicum.filmorate.dto.UserDto;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.mapper.UserMapper;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UserService {
    private final UserDbStorage userDbStorage;
    private final FriendshipDbStorage friendshipDbStorage;

    @Autowired
    public UserService(UserDbStorage userDbStorage, FriendshipDbStorage friendshipDbStorage) {
        this.userDbStorage = userDbStorage;
        this.friendshipDbStorage = friendshipDbStorage;
    }

    public List<UserDto> getUsers() {
        return userDbStorage.getUsers().stream()
                .map(UserMapper::mapToDto)
                .collect(Collectors.toList());
    }

    public UserDto addUser(UserDto userDto) {
        User newUser = UserMapper.mapToUser(userDto);
        User createdUser = userDbStorage.addUser(newUser);
        log.info("Пользователь {} добавлен", createdUser.getId());
        return UserMapper.mapToDto(createdUser);
    }

    public UserDto updateUser(UserDto userDto) {
        User existingUser = validateUserExists(userDto.getId());

        User updatedUser = UserMapper.mapToUser(userDto);
        updatedUser.setId(existingUser.getId());

        User resultUser = userDbStorage.updateUser(updatedUser);
        log.info("Пользователь {} обновлён", resultUser.getId());
        return UserMapper.mapToDto(resultUser);
    }

    public Collection<UserDto> getFriends(Long userId) {
        validateUserExists(userId);

        return friendshipDbStorage.getFriends(userId).stream()
                .map(UserMapper::mapToDto)
                .collect(Collectors.toList());
    }

    public Collection<UserDto> getCommonFriends(Long user1Id, Long user2Id) {
        return friendshipDbStorage.getCommonFriends(user1Id, user2Id).stream()
                .map(UserMapper::mapToDto)
                .collect(Collectors.toList());
    }

    public UserDto addFriend(Long user1Id, Long user2Id) {
        UserDto friend = UserMapper.mapToDto(friendshipDbStorage.addFriend(user1Id, user2Id));
        log.info("Пользователь {} стал другом пользователя {}", user1Id, user2Id);
        return friend;
    }

    public UserDto removeFriend(Long user1Id, Long user2Id) {
        UserDto removedFriend = UserMapper.mapToDto(friendshipDbStorage.removeFriend(user1Id, user2Id));
        log.info("Пользователь {} удалил из друзей пользователя {}", user1Id, user2Id);
        return removedFriend;
    }

    private User validateUserExists(Long userId) {
        return Optional.ofNullable(userDbStorage.getUserById(userId))
                .orElseThrow(() -> new NotFoundException(String.format("Пользователь с ID: %d не найден", userId)));
    }
}
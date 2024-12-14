package ru.yandex.practicum.filmorate;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.InMemoryUserStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

public class UserControllerServiceTest {
    @Mock
    private UserStorage userStorage;

    @InjectMocks
    private UserService userService;

    private User user;
    private User friend;
    private Validator validator;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    public void testAddUserWithEmptyNameShouldSetNameToLogin() {
        InMemoryUserStorage userstorage = new InMemoryUserStorage();
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin("testuser");
        user.setEmail("121113@yandex.ru");
        user.setBirthday(LocalDate.now().minusDays(1));

        User addedUser = userstorage.addUser(user);

        assertNotNull(addedUser);
        assertEquals("testuser", addedUser.getName());
        assertEquals(1L, addedUser.getId());
    }

    @Test
    public void personWithFutureBirthdayShouldFailValidation() {
        User user = new User();
        user.setBirthday(LocalDate.now().plusDays(1));
        user.setName("111");
        user.setLogin("oiou1wio");
        user.setEmail("121113@yandex.ru");

        Set<ConstraintViolation<User>> violations = validator.validate(user);

        assertThat(violations).isNotEmpty();
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Дата рождения должна быть в прошлом");
    }

    @Test
    public void personWithPastBirthdayShouldPassValidation() {
        User user = new User();
        user.setBirthday(LocalDate.of(2000, 1, 1));
        user.setName("111");
        user.setLogin("oiou1wio");
        user.setEmail("121113@yandex.ru");

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertThat(violations).isEmpty();
    }

    @Test
    public void personWithIncorrectEmailShouldFailValidation() {
        User user = new User();
        user.setBirthday(LocalDate.of(2000, 1, 1));
        user.setName("111");
        user.setLogin("oiou1wio");
        user.setEmail("Лапшкаru");
        Set<ConstraintViolation<User>> violations = validator.validate(user);

        assertThat(violations).isNotEmpty();
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Некорректный Email");
    }

    @Test
    public void personWithCorrectEmailShouldPassValidation() {
        User user = new User();
        user.setBirthday(LocalDate.of(2000, 1, 1));
        user.setName("111");
        user.setLogin("oiou1wio");
        user.setEmail("121113@yandex.ru");
        Set<ConstraintViolation<User>> violations = validator.validate(user);

        assertThat(violations).isEmpty();
    }

    @Test
    public void testAddFriendWhenAlreadyFriends() {
        user = new User();
        user.setId(1L);
        user.setFriendsId(new HashSet<>());

        friend = new User();
        friend.setId(2L);
        friend.setFriendsId(new HashSet<>());

        when(userStorage.getUsers()).thenReturn(List.of(user, friend));
        User updatedUser = userService.addFriend(1L, 2L);

        assertTrue(updatedUser.getFriendsId().contains(2L), "User should have friendId 2 in their friends list");
        assertTrue(friend.getFriendsId().contains(1L), "Friend should have userId 1 in their friends list");
    }

    @Test
    public void testGetCommonFriends() {
        user = new User();
        user.setId(1L);
        user.setFriendsId(new HashSet<>(List.of(2L, 3L)));

        friend = new User();
        friend.setId(2L);
        friend.setFriendsId(new HashSet<>(List.of(3L, 4L)));
        when(userStorage.getUsers()).thenReturn(List.of(user, friend));

        Set<Long> commonFriends = userService.getCommonFriends(1L, 2L);

        assertTrue(commonFriends.contains(3L), "Common friend should be 3");
    }

    @Test
    public void testGetCommonFriendsNoCommon() {
        user = new User();
        user.setId(1L);
        user.setFriendsId(new HashSet<>(List.of(2L, 3L)));

        friend = new User();
        friend.setId(2L);
        friend.setFriendsId(new HashSet<>(List.of(3L, 4L)));
        when(userStorage.getUsers()).thenReturn(List.of(user, friend));

        friend.setFriendsId(new HashSet<>(List.of(4L)));
        Set<Long> commonFriends = userService.getCommonFriends(1L, 2L);

        assertTrue(commonFriends.isEmpty(), "Should have no common friends");
    }

    @Test
    public void testGetCommonFriendsUserNotFound() {
        user = new User();
        user.setId(1L);
        user.setFriendsId(new HashSet<>(List.of(2L, 3L)));

        friend = new User();
        friend.setId(2L);
        friend.setFriendsId(new HashSet<>(List.of(3L, 4L)));

        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            userService.getCommonFriends(1L, 3L);
        });
        assertEquals("Пользователь с ID 1 не найден", exception.getMessage());
    }

    @Test
    public void testGetFriends() {
        user = new User();
        user.setId(1L);
        user.setFriendsId(new HashSet<>(List.of(2L, 3L)));

        friend = new User();
        friend.setId(2L);
        friend.setFriendsId(new HashSet<>(List.of(3L, 4L)));
        when(userStorage.getUsers()).thenReturn(List.of(user, friend));
        Set<Long> friends = userService.getFriends(1L);

        assertEquals(2, friends.size(), "User should have 2 friends");
        assertTrue(friends.containsAll(List.of(2L, 3L)), "User friends should include 2 and 3");
    }
}

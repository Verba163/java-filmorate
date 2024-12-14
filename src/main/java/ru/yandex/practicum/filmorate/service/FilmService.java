package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Likes;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    public final Set<Likes> likes = new HashSet<>();

    @Autowired
    public FilmService(FilmStorage filmStorage, UserStorage userStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
    }

    public Film addFilm(Film film) {
        return filmStorage.addFilm(film);
    }

    public Film update(Film film) {
        if (!filmExists(film.getId())) {
            throw new NotFoundException("Фильм не найден для ID: " + film.getId());
        }
        return filmStorage.updateFilm(film);
    }


    public void addLike(Long userId, Long filmId) {
        validateUser(userId);
        if (!filmExists(filmId)) {
            throw new NotFoundException("Фильм с ID " + filmId + " не найден");
        }

        Likes like = new Likes(userId, filmId);
        if (likes.add(like)) {
            Film film = filmStorage.findAll().stream()
                    .filter(f -> f.getId().equals(filmId))
                    .findFirst()
                    .orElseThrow(() -> new NotFoundException("Фильм не найден"));
            film.addLikes();
        } else {
            throw new NotFoundException("Пользователь с ID " + userId + " уже поставил лайк");
        }

    }

    public ResponseEntity<String> removeLike(Long userId, Long filmId) {
        validateUser(userId);
        if (!filmExists(filmId)) {
            throw new NotFoundException("Фильм с ID " + filmId + " не найден");
        }
        Likes like = new Likes(userId, filmId);

        if (likes.remove(like)) {
            Film film = filmStorage.findAll().stream()
                    .filter(f -> f.getId().equals(filmId))
                    .findFirst()
                    .orElseThrow(() -> new NotFoundException("Фильм с ID " + filmId + " не найден"));

            if (film.getLikeCount() > 0) {
                film.setLikeCount(film.getLikeCount() - 1);
            }

            return ResponseEntity.ok("Лайк удален");
        } else {
            throw new NotFoundException("Лайк не найден");
        }
    }

    public List<Film> getTopPopularFilms(int count) {
        return filmStorage.findAll().stream()
                .sorted(Comparator.comparingInt(Film::getLikeCount).reversed())
                .limit(count)
                .collect(Collectors.toList());
    }

    public boolean filmExists(Long filmId) {
        return filmStorage.findAll().stream().anyMatch(film -> film.getId().equals(filmId));
    }

    public Collection<Film> findAll() {
        return filmStorage.findAll();
    }

    private void validateUser(Long userId) {
        boolean userExists = userStorage.getUsers().stream()
                .anyMatch(user -> user.getId().equals(userId));
        if (!userExists) {
            throw new NotFoundException("Пользователь c ID " + userId + " найден");
        }
    }
}

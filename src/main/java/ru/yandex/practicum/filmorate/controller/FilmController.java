package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {

    private final FilmService filmService;

    @Autowired
    public FilmController(FilmService filmService) {
        this.filmService = filmService;
    }

    @GetMapping
    public Collection<Film> getAllFilms() {
        log.info("Получение всех фильмов. Всего фильмов {}:", filmService.findAll().size());
        return filmService.findAll();
    }

    @PostMapping
    public Film create(@Valid @RequestBody Film film) {
        log.warn("Создание нового фильма: {}", film);
        return filmService.addFilm(film);
    }

    @PutMapping
    public Film update(@Valid @RequestBody Film newFilm) {
        if (newFilm.getId() == null) {
            log.warn("Ошибка при обновлении фильма: Id должен быть указан");
            throw new ConditionsNotMetException("Id должен быть указан");
        }
        if (!filmService.findAll().stream().anyMatch(film -> film.getId().equals(newFilm.getId()))) {
            log.error("Фильм с ID {} не существует", newFilm.getId());
            throw new NotFoundException("Фильм не найден для ID: " + newFilm.getId());
        }
        return filmService.update(newFilm);
    }

    @PutMapping("/{filmId}/like/{userId}")
    public ResponseEntity<Map<String, String>> addLike(@PathVariable Long filmId, @PathVariable Long userId) {

        filmService.addLike(userId, filmId);
        return ResponseEntity.ok(Collections.singletonMap("message", "Лайк добавлен"));
    }

    @DeleteMapping("/{filmId}/like/{userId}")
    public ResponseEntity<String> removeLike(@PathVariable Long filmId, @PathVariable Long userId) {

        filmService.removeLike(userId, filmId);
        return ResponseEntity.ok("Лайк удален");
    }

    @GetMapping("/popular")
    public List<Film> getTopFilms() {
        return filmService.getTopPopularFilms(10);
    }
}

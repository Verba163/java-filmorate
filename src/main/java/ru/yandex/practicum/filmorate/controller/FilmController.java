package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {

    private final FilmService filmService;
    private static final String LIKES_PATH = "/{film-Id}/like/{user-Id}";

    @Autowired
    public FilmController(FilmService filmService) {
        this.filmService = filmService;
    }

    @GetMapping
    public List<FilmDto> getAllFilms() {
        log.info("Получение всех фильмов. Всего фильмов {}:", filmService.findAll().size());
        return filmService.findAll();
    }

    @GetMapping("/{id}")
    public FilmDto findById(@PathVariable Long id) {
        log.info("Найден фильм с ID {}: ", id);
        return filmService.findById(id);
    }

    @PostMapping
    public FilmDto create(@Valid @RequestBody FilmDto filmDto) {
        log.info("Создание нового фильма: {}", filmDto);
        return filmService.addFilm(filmDto);
    }


    @PutMapping
    public FilmDto updateFilm(@Valid @RequestBody FilmDto filmDto) {
        log.info("Фильм обновлен {}", filmDto);
        return filmService.updateFilm(filmDto);
    }

    @PutMapping(LIKES_PATH)
    public void addLike(@PathVariable("film-Id") Long filmId, @PathVariable("user-Id") Long userId) {
        log.info("Пользователь с ID {} поставил лайк фильму с ID {}", userId, filmId);
        filmService.addLike(filmId, userId);
    }

    @DeleteMapping(LIKES_PATH)
    public void removeLike(@PathVariable("film-Id") Long filmId, @PathVariable("user-Id") Long userId) {
        log.info("Пользователь с ID {} убрал лайк с фильма с ID {}", userId, filmId);
        filmService.removeLike(userId, filmId);
    }

    @GetMapping("/popular")
    public List<FilmDto> getTopFilms(@RequestParam(defaultValue = "10") Long count) {
        log.info("Получение топ {} популярных фильмов", count);
        return filmService.getTopPopularFilms(count);
    }
}

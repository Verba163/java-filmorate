package ru.yandex.practicum.filmorate.controller;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.dto.GenresDto;
import ru.yandex.practicum.filmorate.service.GenresService;

import java.util.Collection;

@RestController
@RequestMapping("/genres")
@Slf4j
public class GenresController {

    private final GenresService genresService;

    @Autowired
    public GenresController(GenresService genresService) {
        this.genresService = genresService;
    }

    @GetMapping
    public Collection<GenresDto> findAll() {
        log.info("Получение всех жанров.");
        return genresService.findAll();
    }

    @GetMapping("/{id}")
    public GenresDto getNameById(@PathVariable Long id) {
        log.info("Получение жанра с ID: {}", id);
        return genresService.findById(id);
    }
}
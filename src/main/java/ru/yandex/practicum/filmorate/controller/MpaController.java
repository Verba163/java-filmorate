package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.dto.MpaDto;
import ru.yandex.practicum.filmorate.service.MpaService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/mpa")
public class MpaController {

    private final MpaService mpaService;

    @Autowired
    public MpaController(MpaService mpaService) {
        this.mpaService = mpaService;
    }

    @GetMapping
    public List<MpaDto> findAll() {
        List<MpaDto> mpaList = mpaService.findAll();
        log.info("Получение всех рейтингов. Всего рейтингов: {}", mpaList.size());
        return mpaList;
    }

    @GetMapping("/{id}")
    public MpaDto findMpaById(@PathVariable Long id) {
        log.info("Получение рейтинга по id: {}", id);
        return mpaService.findById(id);
    }
}
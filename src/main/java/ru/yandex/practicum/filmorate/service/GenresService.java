package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dal.GenresDbStorage;
import ru.yandex.practicum.filmorate.dto.GenresDto;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.mapper.GenresMapper;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class GenresService {

    private final GenresDbStorage genresDbStorage;

    @Autowired
    public GenresService(GenresDbStorage genresDbStorage) {
        this.genresDbStorage = genresDbStorage;
    }

    public List<GenresDto> findAll() {
        return genresDbStorage.findAll().stream().map(GenresMapper::mapToDto).collect(Collectors.toList());
    }

    public GenresDto findById(Long id) {
        Genre genre = genresDbStorage.findById(id);
        if (genre == null) {
            throw new NotFoundException(String.format("Жанр по ID: %d не найден", id));
        }
        return GenresMapper.mapToDto(genre);
    }
}

package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dal.FilmDbStorage;
import ru.yandex.practicum.filmorate.dal.FilmLikeDbStorage;
import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.mapper.FilmMapper;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class FilmService {

    private final FilmDbStorage filmDbStorage;
    private final FilmLikeDbStorage filmLikeDbStorage;

    @Autowired
    public FilmService(FilmDbStorage filmDbStorage, FilmLikeDbStorage filmLikeDbStorage) {
        this.filmDbStorage = filmDbStorage;
        this.filmLikeDbStorage = filmLikeDbStorage;
    }

    public List<FilmDto> findAll() {
        return filmDbStorage.findAll().stream()
                .map(FilmMapper::mapToDto)
                .collect(Collectors.toList());
    }

    public FilmDto addFilm(FilmDto filmDto) {
        Film newFilm = FilmMapper.mapToFilm(filmDto);
        Film createdFilm = filmDbStorage.addFilm(newFilm);
        return FilmMapper.mapToDto(createdFilm);
    }

    public FilmDto findById(Long id) {
        Film film = filmDbStorage.findById(id);
        if (film == null) {
            throw new NotFoundException(String.format("Фильм с ID: %d не найден ", id));

        }

        return FilmMapper.mapToDto(film);
    }

    public FilmDto updateFilm(FilmDto filmDto) {
        Film filmToUpdate = FilmMapper.mapToFilm(filmDto);
        Film updatedFilm = filmDbStorage.updateFilm(filmToUpdate);
        return FilmMapper.mapToDto(updatedFilm);
    }

    public List<FilmDto> getTopPopularFilms(Long count) {
        return filmLikeDbStorage.getPopularFilms(count).stream()
                .map(FilmMapper::mapToDto)
                .collect(Collectors.toList());
    }

    public void addLike(long filmId, long userId) {
        filmLikeDbStorage.addLike(filmId, userId);
    }

    public void removeLike(long filmId, long userId) {
        filmLikeDbStorage.removeLike(filmId, userId);
    }
}
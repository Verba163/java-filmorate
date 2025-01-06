package ru.yandex.practicum.filmorate.dal;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.mapper.GenresRowMapper;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.GenreStorage;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Repository
@Primary
@RequiredArgsConstructor
public class GenresDbStorage implements GenreStorage {
    private final JdbcTemplate jdbcTemplate;
    private final GenresRowMapper mapper;

    @Override
    public Collection<Genre> findAll() {
        String query = "SELECT * FROM genres";
        return jdbcTemplate.query(query, mapper);
    }

    @Override
    public Genre findById(Long id) {
        String query = "SELECT genre_id, name FROM genres WHERE genre_id = ?";
        try {
            return jdbcTemplate.queryForObject(query, mapper, id);
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException("Жанр по id " + id + " не найден");
        }
    }

    public void validateGenres(List<Genre> genres) {
        for (Genre genre : genres) {
            validateGenreExists(genre.getId());
        }
    }

    private void validateGenreExists(Long genreId) {
        String genreCheckQuery = "SELECT COUNT(*) FROM genres WHERE genre_id = ?";
        Integer count = jdbcTemplate.queryForObject(genreCheckQuery, Integer.class, genreId);
        if (count == null || count == 0) {
            throw new ConditionsNotMetException("Жанр не найден с ID: " + genreId);
        }
    }

    public List<Genre> getListGenreFromDbGenres(Long filmId) {
        String filmGenresQuery = "SELECT genre_id FROM film_genres WHERE film_id = ?";
        List<Long> genreIds = jdbcTemplate.queryForList(filmGenresQuery, Long.class, filmId);

        return findAll().stream()
                .filter(genre -> genreIds.contains(genre.getId()))
                .collect(Collectors.toList());
    }
}
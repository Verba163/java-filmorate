package ru.yandex.practicum.filmorate.dal;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.mapper.FilmRowMapper;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

@Slf4j
@Repository
@Primary
@RequiredArgsConstructor
public class FilmDbStorage implements FilmStorage {
    private final JdbcTemplate jdbcTemplate;
    private final FilmRowMapper mapper;
    private final MpaDbStorage mpaDbStorage;
    private final GenresDbStorage genresDbStorage;

    @Override
    public Film addFilm(Film film) {
        validateFilm(film);

        String query = "INSERT INTO PUBLIC.FILM (name, description, releaseDate, duration, mpa_id) VALUES (?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(query, new String[]{"id"});
            ps.setString(1, film.getName());
            ps.setString(2, film.getDescription());
            ps.setTimestamp(3, Timestamp.valueOf(film.getReleaseDate().atStartOfDay()));
            ps.setInt(4, film.getDuration());
            ps.setLong(5, film.getMpa().getId());
            return ps;
        }, keyHolder);

        Long generatedId = getGeneratedId(keyHolder);
        film.setId(generatedId);
        addGenresForFilm(film.getGenres(), generatedId);

        addMpaForFilm(film.getMpa(), generatedId);

        return film;
    }

    private void validateFilm(Film film) {
        mpaDbStorage.validateMpa(film.getMpa().getId());
        genresDbStorage.validateGenres(film.getGenres());
    }

    private Long getGeneratedId(KeyHolder keyHolder) {
        Number key = keyHolder.getKey();
        if (key != null) {
            return key.longValue();
        } else {
            throw new RuntimeException("Не удалось получить сгенерированный ID для нового фильма.");
        }
    }

    private void addGenresForFilm(List<Genre> genres, Long filmId) {
        if (genres != null) {
            String genreQuery = "INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)";
            for (Genre genre : genres) {
                if (genre.getId() != null) {
                    jdbcTemplate.update(genreQuery, filmId, genre.getId());
                } else {
                    throw new IllegalArgumentException("Жанр не содержит идентификатор.");
                }
            }
        }
    }

    private void addMpaForFilm(Mpa mpa, Long filmId) {
        if (mpa.getId() != null) {
            String checkMpaExistsQuery = "SELECT COUNT(*) FROM mpa WHERE id = ?";
            Integer count = jdbcTemplate.queryForObject(checkMpaExistsQuery, new Object[]{mpa.getId()}, Integer.class);

            if (count != null && count > 0) {
                String mpaQuery = "INSERT INTO film_mpa (film_id, mpa_id) VALUES (?, ?)";
                jdbcTemplate.update(mpaQuery, filmId, mpa.getId());
            } else {
               throw new NotFoundException("Указанный рейтинг не существует" + mpa.getId());
            }
        } else {
            throw new IllegalArgumentException("Рейтинг не содержит идентификатор.");
        }
    }

    @Override
    public Film updateFilm(Film film) {
        String query = "UPDATE film SET name = ?, description = ?, releaseDate = ? WHERE id = ?";
        int rows = jdbcTemplate.update(query, film.getName(), film.getDescription(),
                Timestamp.valueOf(film.getReleaseDate().atStartOfDay()), film.getId());

        if (rows > 0) {
            log.info("Фильм с id = {} успешно обновлён", film.getId());
            return film;
        } else {
            log.error("Ошибка обновления фильма id = {}", film.getId());
            throw new NotFoundException("Фильм не найден для id = " + film.getId());
        }
    }

    @Override
    public List<Film> findAll() {
        String query = "SELECT * FROM film";
        return jdbcTemplate.query(query, mapper);
    }

    @Override
    public Film findById(Long id) {
        log.info("Поиск фильма по id = {}", id);

        final String sqlQuery = "SELECT * FROM film WHERE id = ?";
        return Optional.ofNullable(jdbcTemplate.queryForObject(sqlQuery, mapper::mapRow, id))
                .orElseThrow(() -> new NotFoundException("Фильм с id = " + id + " не найден"));
    }
}
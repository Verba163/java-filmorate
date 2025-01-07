package ru.yandex.practicum.filmorate.dal;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;

import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Component
public class FilmLikeDbStorage {
    private final JdbcTemplate jdbcTemplate;
    private final FilmDbStorage filmDbStorage;

    public FilmLikeDbStorage(JdbcTemplate jdbcTemplate, FilmDbStorage filmDbStorage) {
        this.jdbcTemplate = jdbcTemplate;
        this.filmDbStorage = filmDbStorage;
    }

    public void addLike(long filmId, long userId) {
        validateFilmAndUser(filmId, userId);

        String filmLikeQuery = "INSERT INTO likes (user_id, film_id) VALUES (?, ?)";
        int rows = jdbcTemplate.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(filmLikeQuery);
            stmt.setLong(1, userId);
            stmt.setLong(2, filmId);
            return stmt;
        });

        if (rows > 0) {
            log.info("Пользователь с id = {} поставил лайк фильму с id = {}", userId, filmId);
        }
    }

    private void validateFilmAndUser(long filmId, long userId) {
        if (!isFilmExists(filmId)) {
            log.error("Фильм с id = {} не найден", filmId);
            throw new NotFoundException("Фильм с id = " + filmId + " не найден");
        }

        if (!isUserExists(userId)) {
            log.error("Пользователь с id = {} не найден", userId);
            throw new NotFoundException("Пользователь с id = " + userId + " не найден");
        }
    }

    private boolean isFilmExists(long filmId) {
        String filmQuery = "SELECT COUNT(*) FROM film WHERE id = ?";
        Long filmCount = jdbcTemplate.queryForObject(filmQuery, Long.class, filmId);
        return Objects.nonNull(filmCount) && filmCount > 0;
    }

    private boolean isUserExists(long userId) {
        String userQuery = "SELECT COUNT(*) FROM users WHERE id = ?";
        Long userCount = jdbcTemplate.queryForObject(userQuery, Long.class, userId);
        return Objects.nonNull(userCount) && userCount > 0;
    }

    public void removeLike(long filmId, long userId) {
        log.info("Пользователь с id = {} пытается удалить свой лайк фильму с id = {}", userId, filmId);

        String filmLikeQuery = "SELECT user_id FROM likes WHERE film_id = ?";
        List<Long> userIds = jdbcTemplate.queryForList(filmLikeQuery, Long.class, filmId);

        if (userIds.contains(userId)) {
            String filmLikeRemoveQuery = "DELETE FROM likes WHERE user_id = ? AND film_id = ?";
            int rows = jdbcTemplate.update(filmLikeRemoveQuery, userId, filmId);

            if (rows > 0) {
                log.info("Пользователь с id = {} удалил свой лайк фильму с id = {}", userId, filmId);
            }
        }
    }

    public List<Film> getPopularFilms(Long count) {
        log.info("Получение популярных фильмов в количестве {}", count);

        String filmLikesQuery = """
                SELECT film_id, COUNT(DISTINCT user_id) AS like_count
                FROM likes \s
                GROUP BY film_id
                ORDER BY like_count DESC
                LIMIT ?
                \s""";

        List<Map<String, Object>> filmResults = jdbcTemplate.queryForList(filmLikesQuery, count);

        List<Film> popularFilms = new ArrayList<>();

        for (Map<String, Object> row : filmResults) {
            Long filmId = ((Number) row.get("film_id")).longValue();
            Long likeCount = ((Number) row.get("like_count")).longValue();

            Film film = filmDbStorage.findById(filmId);
            if (film != null) {
                film.setLikes(likeCount);
                popularFilms.add(film);
            }
        }

        popularFilms.forEach(film -> log.info("Количество лайков фильма {} равно {}", film.getId(), film.getLikes()));
        return popularFilms;
    }
}


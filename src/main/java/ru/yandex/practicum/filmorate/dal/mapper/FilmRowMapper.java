package ru.yandex.practicum.filmorate.dal.mapper;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dal.GenresDbStorage;
import ru.yandex.practicum.filmorate.dal.MpaDbStorage;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

@Component
public class FilmRowMapper implements RowMapper<Film> {

    private final MpaDbStorage mpaDbStorage;
    private final GenresDbStorage genresDbStorage;

    public FilmRowMapper(MpaDbStorage mpaDbStorage, GenresDbStorage genresDbStorage) {
        this.mpaDbStorage = mpaDbStorage;
        this.genresDbStorage = genresDbStorage;
    }

    @Override
    public Film mapRow(ResultSet resultSet, int rowNum) throws SQLException {
        Film film = new Film();

        film.setId(resultSet.getLong("id"));
        film.setName(resultSet.getString("name"));
        film.setDescription(resultSet.getString("description"));
        film.setDuration(resultSet.getInt("duration"));

        Timestamp releaseDate = resultSet.getTimestamp("releaseDate");
        if (releaseDate != null) {
            film.setReleaseDate(releaseDate.toLocalDateTime().toLocalDate());
        }

        Long mpaId = resultSet.getLong("mpa_id");
        Mpa mpa = mpaDbStorage.findById(mpaId);
        film.setMpa(mpa);

        List<Genre> genres = genresDbStorage.getListGenreFromDbGenres(film.getId());
        film.setGenres(genres);

        return film;
    }
}

package ru.yandex.practicum.filmorate.dal;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.mapper.MpaRowMapper;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.MpaStorage;

import java.util.Collection;

@Repository
@Primary
@RequiredArgsConstructor
public class MpaDbStorage implements MpaStorage {
    private final JdbcTemplate jdbcTemplate;
    private final MpaRowMapper mapper;

    @Override
    public Collection<Mpa> findAll() {
        String query = "SELECT * FROM mpa";
        return jdbcTemplate.query(query, mapper);
    }

    @Override
    public Mpa findById(Long id) {
        String query = "SELECT id, name FROM mpa WHERE id = ?";
        try {
            return jdbcTemplate.queryForObject(query, mapper, id);
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException(String.format("MPA не найден с ID: %d", id));
        }
    }

    public void validateMpa(Long mpaId) {
        if (!existsById(mpaId)) {
            throw new ConditionsNotMetException(String.format("MPA не найден с ID: %d", mpaId));
        }
    }

    private boolean existsById(Long mpaId) {
        String mpaCheckQuery = "SELECT COUNT(*) FROM mpa WHERE id = ?";
        Integer count = jdbcTemplate.queryForObject(mpaCheckQuery, Integer.class, mpaId);
        return count != null && count > 0;
    }
}
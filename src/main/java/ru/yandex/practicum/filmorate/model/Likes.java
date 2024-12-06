package ru.yandex.practicum.filmorate.model;

import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;

@Data
public class Likes {
    Long userId;
    Long filmId;

    @Autowired
    public Likes(Long userId, Long filmId) {
        this.userId = userId;
        this.filmId = filmId;
    }
}

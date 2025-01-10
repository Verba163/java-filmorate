package ru.yandex.practicum.filmorate.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import ru.yandex.practicum.filmorate.annotation.CustomDateAnnotation;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FilmDto {

    Long id;
    @NotBlank(message = "Ошибка при создании фильма: название не может быть пустым")
    String name;

    @NotBlank(message = "Описание не может быть пустым")
    @Size(max = 200, message = "Ошибка при создании фильма: описание не может превышать 200 символов")
    String description;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    @CustomDateAnnotation(value = "1895-12-28", message = "Дата выхода фильма должна быть не ранее 28 декабря 1895 года.")
    LocalDate releaseDate;

    @Positive(message = "Продолжительность не может быть меньше 0")
    Integer duration;

    Mpa mpa;

    List<Genre> genres = new ArrayList<>();
}


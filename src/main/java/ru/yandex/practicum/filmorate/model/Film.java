package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import ru.yandex.practicum.filmorate.annotation.CustomDateAnnotation;

import java.time.LocalDate;

/**
 * Film.
 */
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@EqualsAndHashCode(of = "id")
public class Film {
    Long id;

    @Getter
    private int likeCount;

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

    public void addLikes() {
        this.likeCount++;
    }
}

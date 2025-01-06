package ru.yandex.practicum.filmorate.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;


@Data
public class GenresDto {
    Long id;

    @NotBlank
    String name;
}


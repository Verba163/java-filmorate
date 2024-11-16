package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

@Data
@EqualsAndHashCode(of = "email")
public class User {
    Long id;

    @NotBlank(message = "Email не может быть пустым")
    @Email(message = "Некорректный Email")
    String email;

    @NotBlank(message = "Логин не может быть пустым или содержать пробелы")
    String login;

    String name;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    @Past(message = "Дата рождения должна быть в прошлом")
    LocalDate birthday;
}
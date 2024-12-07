package ru.yandex.practicum.filmorate;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Likes;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

public class FilmControllerServiceTest {
    @Mock
    private FilmStorage filmStorage;

    @Mock
    private UserService userService;
    @Mock
    private UserStorage userStorage;


    @InjectMocks
    private FilmService filmService;

    private Validator validator;
    private Long userId;
    private Long filmId;
    private Film film;
    private Likes likes;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();

        userId = 1L;
        filmId = 1L;
        film = new Film();
        film.setId(filmId);
        film.setLikeCount(0);
    }

    @Test
    public void validFilm_ShouldPassValidation() {
        Film film = new Film();
        film.setName("Inception");
        film.setDescription("A mind-bending thriller");
        film.setReleaseDate(LocalDate.of(2010, 7, 16));
        film.setDuration(148);

        Set<ConstraintViolation<Film>> violations = validator.validate(film);

        assertThat(violations).isEmpty();
    }

    @Test
    public void filmWithEmptyNameShouldFailValidation() {
        Film film = new Film();
        film.setName(" ");
        film.setDescription("Базука");
        film.setReleaseDate(LocalDate.of(2010, 7, 16));
        film.setDuration(148);

        Set<ConstraintViolation<Film>> violations = validator.validate(film);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Ошибка при создании фильма: название не может быть пустым");
    }

    @Test
    public void filmWithTooLongDescription_ShouldFailValidation() {
        Film film = new Film();
        film.setName("Базука");
        film.setDescription("Базука".repeat(201));
        film.setReleaseDate(LocalDate.of(2010, 7, 16));
        film.setDuration(148);

        Set<ConstraintViolation<Film>> violations = validator.validate(film);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Ошибка при создании фильма: описание не может превышать 200 символов");
    }

    @Test
    public void filmWithValidReleaseDate_ShouldPassValidation() {
        Film film = new Film();
        film.setName("Базука");
        film.setDescription("Базука");
        film.setReleaseDate(LocalDate.of(2023, 11, 1));
        film.setDuration(148);

        Set<ConstraintViolation<Film>> violations = validator.validate(film);

        assertThat(violations).isEmpty();
    }

    @Test
    public void filmWithNegativeDuration_ShouldFailValidation() {
        Film film = new Film();
        film.setName("9");
        film.setDescription("9999");
        film.setReleaseDate(LocalDate.of(2010, 7, 16));
        film.setDuration(-999);

        Set<ConstraintViolation<Film>> violations = validator.validate(film);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Продолжительность не может быть меньше 0");
    }

    @Test
    public void testGetTopPopularFilms() {

        film.setLikeCount(5);
        Film secondFilm = new Film();
        secondFilm.setId(2L);
        secondFilm.setLikeCount(10);
        when(filmStorage.findAll()).thenReturn(List.of(film, secondFilm));

        List<Film> topFilms = filmService.getTopPopularFilms(2);

        assertEquals(2, topFilms.size());
        assertEquals(2L, topFilms.get(0).getId());
        assertEquals(1L, topFilms.get(1).getId());
    }
}

package ru.yandex.practicum.filmorate;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import static org.assertj.core.api.Assertions.assertThat;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Set;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)

class FilmorateApplicationTests {

	private Validator validator;

	@BeforeEach
	public void setup() {
		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		validator = factory.getValidator();
	}

	@Test
	public void validFilm_ShouldPassValidation() {
		Film film = new Film();
		film.setName("Inception");
		film.setDescription("A mind-bending thriller");
		film.setReleaseDate(LocalDate.of(2010, 7, 16));
		film.setDuration(148);

		Set<ConstraintViolation<Film>> violations = validator.validate(film);

		assertThat(violations).isEmpty(); // Ожидаем, что нарушений нет
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
		film.setDescription("Базука".repeat(201)); // Описание длиной 201 символ
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
		film.setReleaseDate(LocalDate.of(2023, 11, 1)); // Корректная дата
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
	public void personWithFutureBirthdayShouldFailValidation() {
		User user = new User();
		user.setBirthday(LocalDate.now().plusDays(1));
		user.setName("111");
		user.setLogin("oiou1wio");
		user.setEmail("121113@yandex.ru");

		Set<ConstraintViolation<User>> violations = validator.validate(user);

		assertThat(violations).isNotEmpty();
		assertThat(violations.iterator().next().getMessage()).isEqualTo("Дата рождения должна быть в прошлом");
	}

	@Test
	public void personWithPastBirthdayShouldPassValidation() {
		User user = new User();
		user.setBirthday(LocalDate.of(2000, 1, 1));
		user.setName("111");
		user.setLogin("oiou1wio");
		user.setEmail("121113@yandex.ru");

		Set<ConstraintViolation<User>> violations = validator.validate(user);

		assertThat(violations).isEmpty();
	}
	@Test
	public void personWithIncorrectEmailShouldFailValidation() {
		User user = new User();
		user.setBirthday(LocalDate.of(2000, 1, 1));
		user.setName("111");
		user.setLogin("oiou1wio");
		user.setEmail("Лапшкаru");
		Set<ConstraintViolation<User>> violations = validator.validate(user);

		assertThat(violations).isNotEmpty();
		assertThat(violations.iterator().next().getMessage()).isEqualTo("Некорректный Email");
	}
	public void personWithCorrectEmailShouldPassValidation() {
		User user = new User();
		user.setBirthday(LocalDate.of(2000, 1, 1));
		user.setName("111");
		user.setLogin("oiou1wio");
		user.setEmail("121113@yandex.ru");
		Set<ConstraintViolation<User>> violations = validator.validate(user);

		assertThat(violations).isEmpty();
	}
}

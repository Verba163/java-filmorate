package ru.yandex.practicum.filmorate.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.yandex.practicum.filmorate.dto.GenresDto;

import ru.yandex.practicum.filmorate.model.Genre;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class GenresMapper {

    public static GenresDto mapToDto(Genre genre) {
        GenresDto dto = new GenresDto();
        dto.setId(genre.getId());
        dto.setName(genre.getName());

        return dto;
    }

    public static Genre mapToUser(GenresDto genresDto) {
        Genre genre = new Genre();
        genre.setName(genresDto.getName());

        return genre;
    }
}



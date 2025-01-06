package ru.yandex.practicum.filmorate.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.yandex.practicum.filmorate.dto.MpaDto;

import ru.yandex.practicum.filmorate.model.Mpa;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MpaMapper {

    public static MpaDto mapToDto(Mpa mpa) {
        MpaDto dto = new MpaDto();
        dto.setId(mpa.getId());
        dto.setName(mpa.getName());

        return dto;
    }

    public static Mpa mapToUser(MpaDto mpaDto) {
        Mpa mpa = new Mpa();
        mpa.setId(mpaDto.getId());
        mpa.setName(mpaDto.getName());

        return mpa;
    }
}



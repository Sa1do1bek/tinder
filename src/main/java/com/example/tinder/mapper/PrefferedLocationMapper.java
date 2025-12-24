package com.example.tinder.mapper;

import com.example.tinder.dto.PreferredLocationDto;
import com.example.tinder.entity.PreferredLocation;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class PrefferedLocationMapper {


    public PreferredLocationDto toDto(PreferredLocation entity) {
        if (entity == null) return null;

        PreferredLocationDto dto = new PreferredLocationDto();
        dto.setId(entity.getId());
        dto.setLatitude(entity.getLatitude());
        dto.setLongitude(entity.getLongitude());
        return dto;
    }

    public PreferredLocation toEntity(PreferredLocationDto dto) {
        if (dto == null) return null;

        PreferredLocation entity = new PreferredLocation();
        entity.setId(dto.getId());
        entity.setLatitude(dto.getLatitude());
        entity.setLongitude(dto.getLongitude());
        return entity;
    }

    public PreferredLocation fromCreateDto(PreferredLocationDto.PreferredLocationCreateDto dto) {
        if (dto == null) return null;
        PreferredLocation entity = new PreferredLocation();
        entity.setLatitude(dto.getLatitude());
        entity.setLongitude(dto.getLongitude());
        return entity;
    }

    public List<PreferredLocationDto> toListDto(List<PreferredLocation> entityList) {
        if (entityList == null) return new ArrayList<>();
        return entityList.stream().map(this::toDto).collect(Collectors.toList());
    }

    public List<PreferredLocation> toListEntity(List<PreferredLocationDto> dtoList) {
        if (dtoList == null) return new ArrayList<>();
        return dtoList.stream().map(this::toEntity).collect(Collectors.toList());
    }

    public List<PreferredLocation> fromCreateDtoList(List<PreferredLocationDto.PreferredLocationCreateDto> dtoList) {
        if (dtoList == null) return new ArrayList<>();
        return dtoList.stream().map(this::fromCreateDto).collect(Collectors.toList());
    }

}

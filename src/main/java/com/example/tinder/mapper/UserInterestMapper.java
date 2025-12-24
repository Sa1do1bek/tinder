package com.example.tinder.mapper;

import com.example.tinder.dto.UserInterestDto;
import com.example.tinder.entity.UserInterest;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class UserInterestMapper {

    public UserInterest toEntity(UserInterestDto dto) {
        if (dto == null) return null;

        UserInterest entity = new UserInterest();
        entity.setId(dto.getId());
        entity.setCode(dto.getCode());
        entity.setName(dto.getName());
        entity.setCategoryType(dto.getCategoryType());

        return entity;
    }

    public UserInterestDto toDto(UserInterest entity) {
        if (entity == null) return null;

        UserInterestDto dto = new UserInterestDto();
        dto.setId(entity.getId());
        dto.setCode(entity.getCode());
        dto.setName(entity.getName());
        dto.setCategoryType(entity.getCategoryType());

        return dto;
    }

    public UserInterest toCreateEntity(UserInterestDto.UserInterestCreateDto dto) {
        if (dto == null) return null;

        UserInterest entity = new UserInterest();
        entity.setCode(dto.getCode());
        entity.setName(dto.getName());
        entity.setCategoryType(dto.getCategoryType());

        return entity;
    }

    public List<UserInterestDto> toListDto(List<UserInterest> list) {
        if (list != null && !list.isEmpty()) {
            return list.stream().map(this::toDto).toList();
        }
        return new ArrayList<>();
    }
    public List<UserInterest> toListEntity(List<UserInterestDto> list) {
        if(list.isEmpty()) return new ArrayList<>();
        return list.stream().map(this::toEntity).toList();
    }
    public List<UserInterest> fromCreateListDto(List<UserInterestDto.UserInterestCreateDto> createDto) {
        if (createDto == null || createDto.isEmpty()) return new ArrayList<>();
        return createDto.stream().map(this::toCreateEntity).toList();
    }
}

package com.example.tinder.mapper;

import com.example.tinder.dto.interest.UserInterestDto;
import com.example.tinder.entity.UserInterest;
import org.springframework.stereotype.Component;

@Component
public class UserInterestMapper {

    public static UserInterest toEntity(UserInterestDto dto) {
        if (dto == null) return null;

        UserInterest entity = new UserInterest();
        entity.setId(dto.getId());
        entity.setUserId(dto.getUserId());
        entity.setCode(dto.getCode());
        entity.setName(dto.getName());
        entity.setCategoryType(dto.getCategoryType());

        return entity;
    }

    public static UserInterestDto toDto(UserInterest entity) {
        if (entity == null) return null;

        UserInterestDto dto = new UserInterestDto();
        dto.setId(entity.getId());
        dto.setUserId(entity.getUserId());
        dto.setCode(entity.getCode());
        dto.setName(entity.getName());
        dto.setCategoryType(entity.getCategoryType());

        return dto;
    }

    public static UserInterest toCreateEntity(UserInterestDto.UserInterestCreateDto dto) {
        if (dto == null) return null;

        UserInterest entity = new UserInterest();
        entity.setUserId(dto.getUserId());
        entity.setCode(dto.getCode());
        entity.setName(dto.getName());
        entity.setCategoryType(dto.getCategoryType());

        return entity;
    }

}

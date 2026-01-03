package com.example.tinder.mapper;

import com.example.tinder.dto.ApproveImageDto;
import com.example.tinder.entity.ApproveImage;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ApproveImageMapper {

    public ApproveImageDto toDto(ApproveImage entity) {
        if (entity == null) return null;

        return ApproveImageDto.builder()
                .id(entity.getId())
                .userId(entity.getUser().getId())
                .cloudPath(entity.getCloudPath())
                .build();
    }

    public List<ApproveImageDto> toListDto(List<ApproveImage> list) {
        if (list == null || list.isEmpty()) return new ArrayList<>();
        return list.stream().map(this::toDto).collect(Collectors.toList());
    }
}

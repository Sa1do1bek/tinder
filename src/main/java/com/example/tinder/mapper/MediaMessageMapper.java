package com.example.tinder.mapper;

import com.example.tinder.dto.message.MediaMessageDto;
import com.example.tinder.entity.MediaMessage;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class MediaMessageMapper {
    public MediaMessageDto toDto(MediaMessage entity) {
        if (entity == null) return null;

        return MediaMessageDto.builder()
                .id(entity.getId())
                .userId(entity.getUser().getId())
                .cloudPath(entity.getCloudPath())
                .build();
    }

    public List<MediaMessageDto> toListDto(List<MediaMessage> list) {
        if (list == null || list.isEmpty()) return new ArrayList<>();
        return list.stream().map(this::toDto).collect(Collectors.toList());
    }
}

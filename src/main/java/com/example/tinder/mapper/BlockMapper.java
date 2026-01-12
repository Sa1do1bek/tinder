package com.example.tinder.mapper;

import com.example.tinder.dto.BlockDto;
import com.example.tinder.entity.Block;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BlockMapper {
    private final UserMapper userMapper;

    public BlockDto toDto(Block block) {
        BlockDto dto = new BlockDto();
        dto.setId(block.getId());
        dto.setBlockedUser(userMapper.toDto(block.getBlocked()));
        dto.setBlockerUser(userMapper.toDto(block.getBlocker()));
        return dto;
    }

}

package com.example.tinder.mapper;

import com.example.tinder.dto.BlockDto;
import com.example.tinder.entity.Block;
import org.springframework.stereotype.Component;

@Component
public class BlockMapper {

    public BlockDto toDto(Block block) {
        BlockDto dto = new BlockDto();
        dto.setId(block.getId());
        dto.setBlockedId(block.getBlocked().getId());
        dto.setBlockerId(block.getBlocker().getId());
        return dto;
    }

}

package com.example.tinder.mapper;

import com.example.tinder.dto.VerificationDto;
import com.example.tinder.entity.User;
import com.example.tinder.entity.Verification;
import org.springframework.stereotype.Component;

@Component
public class VerificationMapper {

    public VerificationDto toDto(Verification entity) {
        if (entity == null) return null;
        VerificationDto dto = new VerificationDto();
        dto.setId(entity.getId());
        dto.setUserId(entity.getUser().getId());
        dto.setFaceImageURL(entity.getFaceImageURL());
        dto.setMatchImageURL(entity.getMatchImageURL());
        dto.setSimilarityScore(entity.getSimilarityScore());
        return dto;
    }

    public Verification fromUpdate(VerificationDto.VerificationUpdateDto dto,
                                   User user,
                                   Long verificationId) {
        if (dto == null) return null;
        Verification entity = new Verification();
        entity.setId(verificationId);
        entity.setUser(user);
        entity.setFaceImageURL(dto.getFaceImageURL());
        entity.setMatchImageURL(dto.getMatchImageURL());
        entity.setSimilarityScore(dto.getSimilarityScore());
        return entity;
    }
}

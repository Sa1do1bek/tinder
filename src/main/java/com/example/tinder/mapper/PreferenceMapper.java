package com.example.tinder.mapper;


import com.example.tinder.dto.PreferenceDto;
import com.example.tinder.entity.Preference;
import com.example.tinder.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PreferenceMapper {
    private final PrefferedLocationMapper prefferedLocationMapper;
    private final UserInterestMapper userInterestMapper;

    private final UserMapper userMapper;

    public Preference toEntity(PreferenceDto dto, User user) {
        if (dto == null) return null;

        Preference entity = new Preference();
        entity.setId(dto.getId());
        entity.setDistance(dto.getDistance());
        entity.setUser(user);
        entity.setPreferredGender(dto.getPreferredGender());
        entity.setMinAge(dto.getMinAge());
        entity.setMaxAge(dto.getMaxAge());
        entity.setPreferredLocations(prefferedLocationMapper.toListEntity(dto.getPreferredLocations()));
        entity.setLanguages(userInterestMapper.toListEntity(dto.getLanguages()));

        return entity;
    }


    public PreferenceDto toDto(Preference entity) {
        if (entity == null) return null;

        return PreferenceDto.builder()
                .distance(entity.getDistance())
                .id(entity.getId())
                .maxAge(entity.getMaxAge())
                .minAge(entity.getMinAge())
                .preferredGender(entity.getPreferredGender())
                .preferredLocations(prefferedLocationMapper.toListDto(entity.getPreferredLocations()))
                .languages(userInterestMapper.toListDto(entity.getLanguages()))
                .build();
    }
}

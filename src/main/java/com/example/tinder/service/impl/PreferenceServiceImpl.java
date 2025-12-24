package com.example.tinder.service.impl;

import com.example.tinder.dto.PreferenceDto;
import com.example.tinder.dto.base.ApiResponse;
import com.example.tinder.entity.Preference;
import com.example.tinder.enums.ErrorType;
import com.example.tinder.exception.RestException;
import com.example.tinder.mapper.PreferenceMapper;
import com.example.tinder.mapper.PrefferedLocationMapper;
import com.example.tinder.mapper.UserInterestMapper;
import com.example.tinder.repository.PreferenceRepository;
import com.example.tinder.service.PreferenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PreferenceServiceImpl implements PreferenceService {
    private final PreferenceRepository repository;
    private final PreferenceMapper mapper;
    private final PrefferedLocationMapper prefferedLocationMapper;
    private final UserInterestMapper userInterestMapper;


    @Override
    public ApiResponse<?> update(Long userId, PreferenceDto.PreferenceUpdateDto dto) {
        Optional<Preference> byUserId = repository.findByUserId(userId);
        Preference preference;
        if (byUserId.isPresent()) {
            Preference entity = byUserId.get();
            entity.setPreferredGender(dto.getPreferredGender());
            entity.setDistance(dto.getDistance());
            entity.setMinAge(dto.getMinAge());
            entity.setMaxAge(dto.getMaxAge());
            entity.setPreferredLocations(prefferedLocationMapper.fromCreateDtoList(dto.getPreferredLocations()));
            entity.setLanguages(userInterestMapper.fromCreateListDto(dto.getLanguages()));

            preference = repository.save(entity);
            return ApiResponse.success(mapper.toDto(preference), "SUCCESSFULLY UPDATE PREFERENCE");
        }
        throw RestException.restThrow(ErrorType.PREFERENCE_NOT_FOUND);
    }


    @Override
    public ApiResponse<?> getByUserId(Long userId) {
        Optional<Preference> byUserId = repository.findByUserId(userId);
        if (byUserId.isEmpty()) throw RestException.restThrow(ErrorType.PREFERENCE_NOT_FOUND);
        return ApiResponse.success(mapper.toDto(byUserId.get()));
    }


}

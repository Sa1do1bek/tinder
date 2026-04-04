package com.example.tinder.service.impl;

import com.example.tinder.dto.PreferenceDto;
import com.example.tinder.dto.base.ApiResponse;
import com.example.tinder.entity.Preference;
import com.example.tinder.enums.ErrorType;
import com.example.tinder.enums.SuccessType;
import com.example.tinder.exception.RestException;
import com.example.tinder.mapper.PreferenceMapper;
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


    @Override
    public ApiResponse<?> update(Long userId, PreferenceDto.PreferenceUpdateDto dto) {
        Optional<Preference> byUserId = repository.findByUserId(userId);
        Preference preference;
        if (byUserId.isPresent()) {
            Preference entity = byUserId.get();
            entity.setDistance(dto.getDistance());
            entity.setMinAge(dto.getMinAge());
            entity.setMaxAge(dto.getMaxAge());


            preference = repository.save(entity);
            return ApiResponse.success(mapper.toDto(preference), SuccessType.SUCCESSFULLY_UPDATE_PREFERENCE);
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

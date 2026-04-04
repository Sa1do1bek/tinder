package com.example.tinder.service;

import com.example.tinder.dto.PreferenceDto;
import com.example.tinder.dto.base.ApiResponse;
import org.springframework.stereotype.Service;

@Service
public interface PreferenceService {

    ApiResponse<?> update(Long userId, PreferenceDto.PreferenceUpdateDto preference);

    ApiResponse<?> getByUserId(Long userId);


}

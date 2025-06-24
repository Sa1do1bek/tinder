package com.example.tinder.service;

import com.example.tinder.dto.ApiResponse;
import com.example.tinder.dto.UserLocDto;
import com.example.tinder.dto.UserLocationDto;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public interface UserLocationService {

    ApiResponse update(Long userId, UserLocationDto userLocationDto);

    List<UserLocDto> getNearbyUsers(Long userId, double radiusKm);
}

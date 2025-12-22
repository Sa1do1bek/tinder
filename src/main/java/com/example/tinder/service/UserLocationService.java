package com.example.tinder.service;

import com.example.tinder.dto.base.ApiResponse;
import com.example.tinder.dto.UserDto;
import com.example.tinder.dto.UserLocationDto;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public interface UserLocationService {

    ApiResponse<?> update(Long userId, UserLocationDto userLocationDto);

    List<UserDto> getNearbyUsers(Long userId, double radiusKm);
}

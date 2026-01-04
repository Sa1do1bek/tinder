package com.example.tinder.service.impl;

import com.example.tinder.dto.*;
import com.example.tinder.dto.base.ApiResponse;
import com.example.tinder.entity.User;
import com.example.tinder.entity.UserLocation;
import com.example.tinder.enums.ErrorType;
import com.example.tinder.exception.RestException;
import com.example.tinder.mapper.UserMapper;
import com.example.tinder.repository.UserLocationRepository;
import com.example.tinder.repository.UserRepository;
import com.example.tinder.service.UserLocationService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserLocationServiceImpl implements UserLocationService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final UserLocationRepository userLocationRepository;


    @Override
    @Transactional
    public ApiResponse<?> update(Long userId, UserLocationDto userLocationDto) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            throw RestException.restThrow(ErrorType.USER_NOT_FOUND);
        }
        User user = userOpt.get();
        user.setLatitude(userLocationDto.getLatitude());
        user.setLongitude(userLocationDto.getLongitude());
        User save = userRepository.save(user);

        UserLocation userLocation = userLocationRepository.findByUserId(userId).orElse(UserLocation.builder().user(save).build());
        userLocation.setUser(user);
        userLocation.setLatitude(userLocationDto.getLatitude());
        userLocation.setLongitude(userLocationDto.getLongitude());
        userLocationRepository.save(userLocation);

        return ApiResponse.success("SUCCESSFULLY UPDATED USER LOCATION");
    }

    @Override
    public List<UserDto> getNearbyUsers(Long userId, double radiusKm) {
        UserLocation userLocation = userLocationRepository.findByUserId(userId)
                .orElseThrow(() -> RestException.restThrow(ErrorType.USER_LOCATION_NOT_FOUND));

        if (userLocation.getLatitude() == null || userLocation.getLongitude() == null) {
            throw RestException.restThrow(ErrorType.USER_LOCATION_NOT_FOUND);
        }

        double lat1 = Double.parseDouble(userLocation.getLatitude());
        double lon1 = Double.parseDouble(userLocation.getLongitude());

        return userLocationRepository.findAll()
                .stream()
                .filter(loc -> !loc.getUser().getId().equals(userId))
                .filter(loc -> loc.getUser().isActive()) // 🔒 faqat active userlar
                .filter(loc -> isValidLocation(loc, lat1, lon1, radiusKm))
                .map(loc -> userMapper.toDto(loc.getUser()))
                .toList();

    }

    private boolean isValidLocation(UserLocation loc, double lat1, double lon1, double radiusKm) {
        if (loc.getLatitude() == null || loc.getLongitude() == null) {
            return false;
        }
        try {
            double lat2 = Double.parseDouble(loc.getLatitude());
            double lon2 = Double.parseDouble(loc.getLongitude());
            return haversine(lat1, lon1, lat2, lon2) <= radiusKm;
        } catch (NumberFormatException e) {
            log.error("Noto'g'ri lokatsiya formati foydalanuvchi uchun {}", loc.getUser().getId(), e);
            return false;
        }
    }

    private double haversine(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        return R * (2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a)));
    }

}

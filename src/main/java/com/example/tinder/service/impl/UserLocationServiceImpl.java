package com.example.tinder.service.impl;

import com.example.tinder.dto.*;
import com.example.tinder.entity.User;
import com.example.tinder.entity.UserInterest;
import com.example.tinder.entity.UserLocation;
import com.example.tinder.enums.ErrorType;
import com.example.tinder.exception.RestException;
import com.example.tinder.repository.UserLocationRepository;
import com.example.tinder.repository.UserRepository;
import com.example.tinder.service.UserLocationService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserLocationServiceImpl implements UserLocationService {
    private final UserRepository userRepository;
    private final UserLocationRepository userLocationRepository;

    @Override
    @Transactional
    public ApiResponse<?> update(Long userId, UserLocationDto userLocationDto) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            throw RestException.restThrow(ErrorType.USER_NOT_FOUND);
        }
        User user = userOpt.get();

        UserLocation userLocation = userLocationRepository.findByUserId(userId).orElse(UserLocation.builder().user(user).build());
        userLocation.setUser(user);
        userLocation.setLatitude(userLocationDto.getLatitude());
        userLocation.setLongitude(userLocationDto.getLongitude());
        userLocationRepository.save(userLocation);

        return ApiResponse.success("SUCCESSFULLY UPDATED USER LOCATION");
    }

    @Override
    public List<UserDto> getNearbyUsers(Long userId, double radiusKm) {
        UserLocation userLocation = userLocationRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Foydalanuvchi lokatsiyasi topilmadi"));

        if (userLocation.getLatitude() == null || userLocation.getLongitude() == null) {
            throw new IllegalArgumentException("Lokatsiya o'rnatilmagan");
        }

        double lat1 = Double.parseDouble(userLocation.getLatitude());
        double lon1 = Double.parseDouble(userLocation.getLongitude());

        return userLocationRepository.findAll()
                .stream()
                .filter(loc -> !loc.getUser().getId().equals(userId))
                .filter(loc -> loc.getUser().isActive()) // 🔒 faqat active userlar
                .filter(loc -> isValidLocation(loc, lat1, lon1, radiusKm))
                .map(loc -> toUserDto(loc.getUser()))
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

    private UserDto toUserDto(User user) {
        return UserDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullname(user.getFullname())
                .status(user.getStatus().toString())
                .profilePic(user.getProfilePic())
                .gender(user.getGender())
                .birthDate(user.getBirthDate())
//                .interests(user.getUserInterests().stream()
//                        .map(UserInterest::getName)
//                        .collect(Collectors.toList()))
                .bio(user.getBio())
                .build();
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

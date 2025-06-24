package com.example.tinder.service.impl;

import com.example.tinder.dto.ApiResponse;
import com.example.tinder.dto.UserLocDto;
import com.example.tinder.dto.UserLocationDto;
import com.example.tinder.entity.User;
import com.example.tinder.entity.UserLocation;
import com.example.tinder.repository.UserLocationRepository;
import com.example.tinder.repository.UserRepository;
import com.example.tinder.service.UserLocationService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserLocationServiceImpl implements UserLocationService {
    private final UserRepository userRepository;
    private final UserLocationRepository userLocationRepository;

    @Override
    @Transactional
    public ApiResponse update(Long userId, UserLocationDto userLocationDto) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            return ApiResponse.builder()
                    .message("User not found")
                    .status(HttpStatus.BAD_REQUEST)
                    .build();

        }
        User user = userOpt.get();

        UserLocation userLocation=userLocationRepository.findByUserId(userId).orElse(UserLocation.builder().user(user).build());
        userLocation.setUser(user);
        userLocation.setLatitude(userLocationDto.getLatitude());
        userLocation.setLongitude(userLocationDto.getLongitude());
        userLocationRepository.save(userLocation);
        return ApiResponse.builder()
                .message("Successfully updated user location")
                .status(HttpStatus.OK)
                .build();
    }

    @Override
    public List<UserLocDto> getNearbyUsers(Long userId, double radiusKm) {
        UserLocation userLocation = userLocationRepository.findByUserId(userId).orElseThrow();

        if (userLocation.getLatitude() == null || userLocation.getLongitude() == null) {
            throw new IllegalArgumentException("Sizning lokatsiyangiz aniqlanmagan. Iltimos, ilovaga ruxsat bering.");
        }

        double lat1 = Double.parseDouble(userLocation.getLatitude());
        double lon1 = Double.parseDouble(userLocation.getLongitude());

        List<UserLocation> all = userLocationRepository.findAll();
        return all.stream()
                .filter(loc -> !loc.getUser().getId().equals(userId))
                .filter(loc -> {
                    if (loc.getLatitude() == null || loc.getLongitude() == null) {
                        return false;
                    }

                    try {
                        double lat2 = Double.parseDouble(loc.getLatitude());
                        double lon2 = Double.parseDouble(loc.getLongitude());
                        return haversine(lat1, lon1, lat2, lon2) <= radiusKm;
                    } catch (NumberFormatException e) {
                        return false;
                    }
                })
                .map(loc -> {
                    User user = loc.getUser();
                    return UserLocDto.builder()
                            .id(user.getId())
                            .fullname(user.getFullname())
                            .username(user.getUsername())
                            .profilePic(user.getProfilePic())
                            .gender(String.valueOf(user.getGender()))
                            .userInterests(user.getUserInterests())
                            .build();
                })
                .toList();
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

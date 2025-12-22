package com.example.tinder.service.impl;

import com.example.tinder.dto.DiscoveryResponseDTO;
import com.example.tinder.dto.UserInterestDto;
import com.example.tinder.dto.base.ApiResponse;
import com.example.tinder.entity.ProfilePicture;
import com.example.tinder.entity.User;
import com.example.tinder.entity.UserLocation;
import com.example.tinder.repository.UserLocationRepository;
import com.example.tinder.repository.UserRepository;
import com.example.tinder.service.DiscoveryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DiscoveryServiceImpl implements DiscoveryService {

    private final UserRepository userRepository;
    private final UserLocationRepository locationRepository;

    public ApiResponse<?> getDiscoveryUsers(Long currentUserId) {
        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Optional<UserLocation> currentUserLoc = locationRepository.findByUserId(currentUserId);


        List<User> allUsers = userRepository.findAll();

        return ApiResponse.success( allUsers.stream()
                .filter(user -> !user.getId().equals(currentUserId))
                .map(user -> convertToCardResponse(user, currentUserLoc))
                .collect(Collectors.toList()), "SUCCESSFULLY DISCOVERED USERS");
    }

    private DiscoveryResponseDTO convertToCardResponse(User targetUser, Optional<UserLocation> currentLoc) {
        int age = 0;
        if (targetUser.getBirthDate() != null) {
            LocalDate birthDate = targetUser.getBirthDate().toInstant()
                    .atZone(ZoneId.systemDefault()).toLocalDate();
            age = Period.between(birthDate, LocalDate.now()).getYears();
        }

        double distance = 0;
        Optional<UserLocation> targetLoc = locationRepository.findByUserId(targetUser.getId());
        if (currentLoc.isPresent() && targetLoc.isPresent()) {
            distance = calculateHaversine(
                    Double.parseDouble(currentLoc.get().getLatitude()),
                    Double.parseDouble(currentLoc.get().getLongitude()),
                    Double.parseDouble(targetLoc.get().getLatitude()),
                    Double.parseDouble(targetLoc.get().getLongitude())
            );
        }

        List<String> images = targetUser.getProfilePictures().stream()
                .map(ProfilePicture::getCloudPath)
                .collect(Collectors.toList());

        List<UserInterestDto> interests = targetUser.getUserInterests().stream()
                .map(i -> new UserInterestDto(i.getId(), targetUser.getId(), i.getCategoryType(), i.getCode(), i.getName()))
                .collect(Collectors.toList());

        return DiscoveryResponseDTO.builder()
                .id(targetUser.getId())
                .fullname(targetUser.getFullname())
                .bio(targetUser.getBio())
                .gender(targetUser.getGender().name())
                .age(age)
                .distanceKm(Math.round(distance * 10.0) / 10.0)
                .profilePictures(images)
                .interests(interests)
                .build();
    }

    private double calculateHaversine(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371;
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }
}

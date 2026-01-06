package com.example.tinder.service.impl;

import com.example.tinder.dto.DiscoveryResponseDTO;
import com.example.tinder.dto.UserInterestDto;
import com.example.tinder.dto.base.ApiResponse;
import com.example.tinder.entity.*;
import com.example.tinder.enums.ErrorType;
import com.example.tinder.exception.RestException;
import com.example.tinder.repository.PreferenceRepository;
import com.example.tinder.repository.UserLocationRepository;
import com.example.tinder.repository.UserRepository;
import com.example.tinder.service.DiscoveryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DiscoveryServiceImpl implements DiscoveryService {

    private final UserRepository userRepository;
    private final UserLocationRepository locationRepository;
    private final PreferenceRepository preferenceRepository;

    @Override
    public ApiResponse<?> getDiscoveryUsers(Long currentUserId, Pageable pageable) {

        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> RestException.restThrow(ErrorType.USER_NOT_FOUND));

        Preference pref = preferenceRepository.findByUserId(currentUserId)
                .orElseThrow(() -> RestException.restThrow(ErrorType.PREFERENCE_NOT_FOUND));

        UserLocation myLoc = locationRepository.findByUserId(currentUserId)
                .orElse(null);

        Page<User> usersPage;

        if (myLoc != null && myLoc.getLatitude() != null) {
            usersPage = userRepository.findDiscoveryUsersWithSmartMatching(
                    currentUserId,
                    pref.getPreferredGender().name(),
                    pref.getMinAge(),
                    pref.getMaxAge(),
                    pref.getDistance(),
                    Double.parseDouble(myLoc.getLatitude()),
                    Double.parseDouble(myLoc.getLongitude()),
                    pageable
            );
        } else {
            usersPage = userRepository.findDiscoveryUsersWithoutLocation(
                    currentUserId, pref.getPreferredGender().name(),
                    pref.getMinAge(), pref.getMaxAge(), pageable);
        }

        List<DiscoveryResponseDTO> responseList = usersPage.getContent().stream()
                .map(user -> convertToCardResponse(user, Optional.ofNullable(myLoc)))
                .collect(Collectors.toList());

        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("users", responseList);
        responseMap.put("currentPage", usersPage.getNumber());
        responseMap.put("totalPages", usersPage.getTotalPages());

        return ApiResponse.success(responseMap, "SUCCESSFULLY DISCOVERED USERS");
    }


    @Override
    public ApiResponse<?> findAllUsersByUserInterests(Long currentUserId, Long interestId, Pageable pageable) {
        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> RestException.restThrow(ErrorType.USER_NOT_FOUND));

        Preference pref = preferenceRepository.findByUserId(currentUserId)
                .orElseThrow(() -> RestException.restThrow(ErrorType.PREFERENCE_NOT_FOUND));

        UserLocation myLoc = locationRepository.findByUserId(currentUserId)
                .orElseThrow(() -> RestException.restThrow(ErrorType.LOCATION_NOT_FOUND));

        if (myLoc.getLatitude() == null || myLoc.getLongitude() == null) {
            throw RestException.restThrow(ErrorType.LOCATION_NOT_FOUND);
        }

        Page<User> usersPage = userRepository.findUsersBySpecificInterest(
                currentUserId,
                interestId,
                pref.getPreferredGender().name(),
                pref.getMinAge(),
                pref.getMaxAge(),
                pref.getDistance(),
                Double.parseDouble(myLoc.getLatitude()),
                Double.parseDouble(myLoc.getLongitude()),
                pageable
        );

        List<DiscoveryResponseDTO> dtoList = usersPage.getContent().stream()
                .map(user -> convertToCardResponse(user, Optional.of(myLoc)))
                .collect(Collectors.toList());

        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("users", dtoList);
        responseMap.put("currentPage", usersPage.getNumber());
        responseMap.put("totalPages", usersPage.getTotalPages());

        return ApiResponse.success(responseMap, "USERS FOUND BY INTEREST ID: " + interestId);
    }


    public DiscoveryResponseDTO convertToCardResponse(User targetUser, Optional<UserLocation> currentLoc) {
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
                .latitude(targetUser.getLatitude())
                .longitude(targetUser.getLongitude())
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
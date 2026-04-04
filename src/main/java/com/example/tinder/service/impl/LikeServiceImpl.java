package com.example.tinder.service.impl;

import com.example.tinder.dto.LikedUserDto;
import com.example.tinder.dto.UserInterestDto;
import com.example.tinder.dto.base.ApiResponse;
import com.example.tinder.entity.*;
import com.example.tinder.enums.ErrorType;
import com.example.tinder.enums.SuccessType;
import com.example.tinder.enums.SwipeStatus;
import com.example.tinder.exception.RestException;
import com.example.tinder.repository.PreferenceRepository;
import com.example.tinder.repository.SwipeRepository;
import com.example.tinder.repository.UserLocationRepository;
import com.example.tinder.repository.UserRepository;
import com.example.tinder.service.LikeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class LikeServiceImpl implements LikeService {
    private final SwipeRepository swipeRepository;
    private final UserLocationRepository locationRepository;
    private final UserRepository userRepository;
    private final PreferenceRepository preferenceRepository;

    @Override
    public ApiResponse<?> getAllRejectUsers(PageRequest pageRequest, Long userId) {
        Page<User> users = swipeRepository.findAllRejectedUsers(userId, pageRequest);
        return getApiResponse(pageRequest, userId, users);
    }

    @Override
    public ApiResponse<?> getMyLikedUsers(PageRequest pageRequest, Long userId) {
        // LIKE va SUPERLIKE aralash olamiz
        Page<User> users = swipeRepository.findAllLikeUsers(userId, pageRequest);
        return getApiResponseWithMixedSwipeTypesForMyLikedUsers(pageRequest, userId, users);
    }

    @Override
    public ApiResponse<?> getMyLovers(PageRequest pageRequest, Long userId) {
        // Men like qilgan userlar
        Page<User> users = swipeRepository.findAllUsersWhoLikeMe(userId, pageRequest);
        return getApiResponseWithMixedSwipeTypes(pageRequest, userId, users);
    }

    @NotNull
    private ApiResponse<?> getApiResponse(PageRequest pageRequest, Long userId, Page<User> users) {
        if (!users.isEmpty()) {
            UserLocation myLoc = locationRepository.findByUserId(userId)
                    .orElse(null);

            List<LikedUserDto> responseList = users.getContent().stream()
                    .map(user -> convertToLikedUserDto(user, Optional.ofNullable(myLoc), SwipeStatus.REJECT))
                    .collect(Collectors.toList());

            Map<String, Object> responseMap = new HashMap<>();
            responseMap.put("users", responseList);
            responseMap.put("currentPage", pageRequest.getPageNumber());
            responseMap.put("totalPages", users.getTotalPages());

            return ApiResponse.success(responseMap, SuccessType.SUCCESS);
        }

        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("users", new ArrayList<>());
        responseMap.put("currentPage", pageRequest.getPageNumber());
        responseMap.put("totalPages", 0);

        return ApiResponse.success(responseMap, SuccessType.SUCCESS);
    }

    @NotNull
    private ApiResponse<?> getApiResponseWithMixedSwipeTypes(PageRequest pageRequest, Long userId, Page<User> users) {
        if (!users.isEmpty()) {
            UserLocation myLoc = locationRepository.findByUserId(userId)
                    .orElse(null);

            List<LikedUserDto> responseList = users.getContent().stream()
                    .map(user -> {
                        // Har bir user uchun uning swipe type ni topamiz
                        SwipeStatus swipeType = getHisSwipeType(userId, user.getId());
                        return convertToLikedUserDto(user, Optional.ofNullable(myLoc), swipeType);
                    })
                    .collect(Collectors.toList());

            Map<String, Object> responseMap = new HashMap<>();
            responseMap.put("users", responseList);
            responseMap.put("currentPage", pageRequest.getPageNumber());
            responseMap.put("totalPages", users.getTotalPages());

            return ApiResponse.success(responseMap, SuccessType.SUCCESS);
        }

        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("users", new ArrayList<>());
        responseMap.put("currentPage", pageRequest.getPageNumber());
        responseMap.put("totalPages", 0);

        return ApiResponse.success(responseMap, SuccessType.SUCCESS);
    }


    @NotNull
    private ApiResponse<?> getApiResponseWithMixedSwipeTypesForMyLikedUsers(PageRequest pageRequest, Long userId, Page<User> users) {
        if (!users.isEmpty()) {
            UserLocation myLoc = locationRepository.findByUserId(userId)
                    .orElse(null);

            List<LikedUserDto> responseList = users.getContent().stream()
                    .map(user -> {
                        // Har bir user uchun uning swipe type ni topamiz
                        SwipeStatus swipeType = getSwipeType(userId, user.getId());
                        return convertToLikedUserDto(user, Optional.ofNullable(myLoc), swipeType);
                    })
                    .collect(Collectors.toList());

            Map<String, Object> responseMap = new HashMap<>();
            responseMap.put("users", responseList);
            responseMap.put("currentPage", pageRequest.getPageNumber());
            responseMap.put("totalPages", users.getTotalPages());

            return ApiResponse.success(responseMap, SuccessType.SUCCESS);
        }

        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("users", new ArrayList<>());
        responseMap.put("currentPage", pageRequest.getPageNumber());
        responseMap.put("totalPages", 0);

        return ApiResponse.success(responseMap, SuccessType.SUCCESS);
    }

    @Override
    public ApiResponse<?> getTopLikedUsers(Long currentUserId) {
        userRepository.findById(currentUserId)
                .orElseThrow(() -> RestException.restThrow(ErrorType.USER_NOT_FOUND));

        Preference pref = preferenceRepository.findByUserId(currentUserId)
                .orElseThrow(() -> RestException.restThrow(ErrorType.PREFERENCE_NOT_FOUND));

        UserLocation myLoc = locationRepository.findByUserId(currentUserId)
                .orElseThrow(() -> RestException.restThrow(ErrorType.LOCATION_NOT_FOUND));

        if (myLoc.getLatitude() == null || myLoc.getLongitude() == null) {
            throw RestException.restThrow(ErrorType.LOCATION_NOT_FOUND);
        }

        List<User> topLikedUsers = userRepository.findTopLikedUsersNearby(
                currentUserId,
                pref.getPreferredGender().name(),
                pref.getMinAge(),
                pref.getMaxAge(),
                pref.getDistance(),
                Double.parseDouble(myLoc.getLatitude()),
                Double.parseDouble(myLoc.getLongitude())
        );

        List<LikedUserDto> responseList = topLikedUsers.stream()
                .map(user -> {
                    SwipeStatus swipeType = getSwipeType(currentUserId, user.getId());
                    return convertToLikedUserDto(user, Optional.of(myLoc), swipeType);
                })
                .collect(Collectors.toList());

        return ApiResponse.success(responseList, SuccessType.TOP_LIKED_USERS_FETCHED_SUCCESSFULLY);
    }


    @Override
    public ApiResponse<?> getLeastLikedUsers(Long currentUserId) {
        userRepository.findById(currentUserId)
                .orElseThrow(() -> RestException.restThrow(ErrorType.USER_NOT_FOUND));

        Preference pref = preferenceRepository.findByUserId(currentUserId)
                .orElseThrow(() -> RestException.restThrow(ErrorType.PREFERENCE_NOT_FOUND));

        UserLocation myLoc = locationRepository.findByUserId(currentUserId)
                .orElseThrow(() -> RestException.restThrow(ErrorType.LOCATION_NOT_FOUND));

        if (myLoc.getLatitude() == null || myLoc.getLongitude() == null) {
            throw RestException.restThrow(ErrorType.LOCATION_NOT_FOUND);
        }

        List<User> topLikedUsers = userRepository.findLeastLikedUsersNearby(
                currentUserId,
                pref.getPreferredGender().name(),
                pref.getMinAge(),
                pref.getMaxAge(),
                pref.getDistance(),
                Double.parseDouble(myLoc.getLatitude()),
                Double.parseDouble(myLoc.getLongitude())
        );

        List<LikedUserDto> responseList = topLikedUsers.stream()
                .map(user -> {
                    SwipeStatus swipeType = getSwipeType(currentUserId, user.getId());
                    return convertToLikedUserDto(user, Optional.of(myLoc), swipeType);
                })
                .collect(Collectors.toList());

        return ApiResponse.success(responseList, SuccessType.TOP_LIKED_USERS_FETCHED_SUCCESSFULLY);
    }


    private SwipeStatus getSwipeType(Long meId, Long targetUserId) {
        return swipeRepository.findSwipeTypeByUsers(meId, targetUserId).orElse(null);
    }

    // U meni swipe qilganmi
    private SwipeStatus getHisSwipeType(Long meId, Long targetUserId) {
        return swipeRepository.findSwipeTypeByUsers(meId, targetUserId).orElse(null);
    }


    private LikedUserDto convertToLikedUserDto(User targetUser, Optional<UserLocation> currentLoc, SwipeStatus swipeType) {
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
                .map(i -> new UserInterestDto(i.getId(), targetUser.getId(), i.getCategoryType(), i.getCode(), i.getName(), i.getNameRu(), i.getNameUz()))
                .collect(Collectors.toList());

        return LikedUserDto.builder()
                .id(targetUser.getId())
                .fullname(targetUser.getFullname())
                .bio(targetUser.getBio())
                .gender(targetUser.getGender().name())
                .age(age)
                .distanceKm(Math.round(distance * 10.0) / 10.0)
                .height(targetUser.getHeight())
                .profilePictures(images)
                .interests(interests)
                .latitude(targetUser.getLatitude())
                .longitude(targetUser.getLongitude())
                .likeType(swipeType)
                .subscriptionType(targetUser.getSubscriptionType())
                .isApproved(targetUser.getIsApproved())
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
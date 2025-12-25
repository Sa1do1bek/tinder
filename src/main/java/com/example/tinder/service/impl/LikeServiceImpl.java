package com.example.tinder.service.impl;

import com.example.tinder.dto.DiscoveryResponseDTO;
import com.example.tinder.dto.base.ApiResponse;
import com.example.tinder.entity.Preference;
import com.example.tinder.entity.User;
import com.example.tinder.entity.UserLocation;
import com.example.tinder.enums.ErrorType;
import com.example.tinder.exception.RestException;
import com.example.tinder.repository.PreferenceRepository;
import com.example.tinder.repository.SwipeRepository;
import com.example.tinder.repository.UserLocationRepository;
import com.example.tinder.repository.UserRepository;
import com.example.tinder.service.LikeService;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LikeServiceImpl implements LikeService {
    private final SwipeRepository swipeRepository;
    private final DiscoveryServiceImpl discoveryService;
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
        Page<User> users = swipeRepository.findAllAcceptedUsers(userId, pageRequest);

        return getApiResponse(pageRequest, userId, users);
    }

    @NotNull
    private ApiResponse<?> getApiResponse(PageRequest pageRequest, Long userId, Page<User> users) {
        if (!users.isEmpty()) {
            UserLocation myLoc = locationRepository.findByUserId(userId)
                    .orElse(null);

            List<DiscoveryResponseDTO> responseList = users.getContent().stream()
                    .map(user -> discoveryService.convertToCardResponse(user, Optional.ofNullable(myLoc)))
                    .collect(Collectors.toList());

            Map<String, Object> responseMap = new HashMap<>();
            responseMap.put("users", responseList);
            responseMap.put("currentPage", pageRequest.getPageNumber());
            responseMap.put("totalPages", (pageRequest.getPageSize()+1) / 2);

            return ApiResponse.success(responseMap, "SUCCESS");
        }


        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("users", new ArrayList<>());
        responseMap.put("currentPage", pageRequest.getPageNumber());
        responseMap.put("totalPages", pageRequest.getPageSize());

        return ApiResponse.success(responseMap, "SUCCESS");
    }

    @Override
    public ApiResponse<?> getMyLovers(PageRequest pageRequest, Long userId) {

        Page<User> users = swipeRepository.findAllUsersWhoLikeMe(userId, pageRequest);

        return getApiResponse(pageRequest, userId, users);
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

        List<DiscoveryResponseDTO> responseList = topLikedUsers.stream()
                .map(user -> discoveryService.convertToCardResponse(user, Optional.of(myLoc)))
                .collect(Collectors.toList());

        return ApiResponse.success(responseList, "TOP LIKED USERS FETCHED SUCCESSFULLY");
    }





}

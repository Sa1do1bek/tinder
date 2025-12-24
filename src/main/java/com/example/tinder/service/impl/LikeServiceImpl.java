package com.example.tinder.service.impl;

import com.example.tinder.dto.DiscoveryResponseDTO;
import com.example.tinder.dto.base.ApiResponse;
import com.example.tinder.entity.User;
import com.example.tinder.entity.UserLocation;
import com.example.tinder.enums.ErrorType;
import com.example.tinder.exception.RestException;
import com.example.tinder.mapper.UserMapper;
import com.example.tinder.repository.SwipeRepository;
import com.example.tinder.repository.UserLocationRepository;
import com.example.tinder.repository.UserRepository;
import com.example.tinder.service.LikeService;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LikeServiceImpl implements LikeService {
    private final SwipeRepository swipeRepository;
    private final DiscoveryServiceImpl discoveryService;
    private final UserLocationRepository locationRepository;

    @Override
    public ApiResponse<?> getAllRejectUsers(PageRequest pageRequest, Long userId) {
        Page<User> users = swipeRepository.findAllRejectedUsers(userId, pageRequest);

        return getApiResponse(userId, users);
    }


    @NotNull
    private ApiResponse<?> getApiResponse(Long userId, Page<User> users) {
        if (!users.isEmpty()) {
            UserLocation myLoc = locationRepository.findByUserId(userId)
                    .orElse(null);

            List<DiscoveryResponseDTO> responseList = users.getContent().stream()
                    .map(user -> discoveryService.convertToCardResponse(user, Optional.ofNullable(myLoc)))
                    .collect(Collectors.toList());

            return ApiResponse.success(responseList, "SUCCESS");
        }

        return ApiResponse.success(new ArrayList<>(), "SUCCESS");
    }


    @Override
    public ApiResponse<?> getMyLikedUsers(PageRequest pageRequest, Long userId) {
        Page<User> users = swipeRepository.findAllAcceptedUsers(userId, pageRequest);

        return getApiResponse(userId, users);
    }

    @Override
    public ApiResponse<?> getMyLovers(PageRequest pageRequest, Long userId) {

        Page<User> users = swipeRepository.findAllUsersWhoLikeMe(userId, pageRequest);

        return getApiResponse(userId, users);
    }

}

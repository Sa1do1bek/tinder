package com.example.tinder.service;

import com.example.tinder.dto.base.ApiResponse;
import org.springframework.data.domain.PageRequest;

public interface LikeService {

    ApiResponse<?> getAllRejectUsers(PageRequest pageRequest, Long userId);

    ApiResponse<?> getMyLikedUsers(PageRequest pageRequest, Long userId);

    ApiResponse<?> getMyLovers(PageRequest pageRequest, Long userId);

    ApiResponse<?> getTopLikedUsers(Long currentUserId);

}

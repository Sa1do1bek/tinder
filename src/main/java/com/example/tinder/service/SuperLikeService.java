package com.example.tinder.service;

import com.example.tinder.dto.base.ApiResponse;
import com.example.tinder.dto.superLike.PurchaseSuperLikeRequest;
import com.example.tinder.dto.superLike.SendSuperLikeRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public interface SuperLikeService {

    ApiResponse<?> getAllPackages();

    ApiResponse<?> purchaseSuperLikes(Long userId, PurchaseSuperLikeRequest request);

    ApiResponse<?> sendSuperLike(Long senderId, SendSuperLikeRequest request);

    ApiResponse<?> getSuperLikeStats(Long userId);

    ApiResponse<?> getMyPurchases(Long userId, Pageable pageable);

    ApiResponse<?> getMySentSuperLikes(Long userId, Pageable pageable);

    ApiResponse<?> getMyReceivedSuperLikes(Long userId, Pageable pageable);
}
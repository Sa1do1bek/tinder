package com.example.tinder.service;

import com.example.tinder.dto.base.ApiResponse;
import com.example.tinder.dto.superLike.PurchaseSuperLikeRequest;
import com.example.tinder.dto.superLike.SendSuperLikeRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public interface SuperLikeService {
    
    /**
     * Barcha aktiv Super Like paketlarini olish
     */
    ApiResponse<?> getAllPackages();
    
    /**
     * Super Like sotib olish
     */
    ApiResponse<?> purchaseSuperLikes(Long userId, PurchaseSuperLikeRequest request);
    
    /**
     * Super Like yuborish
     */
    ApiResponse<?> sendSuperLike(Long senderId, SendSuperLikeRequest request);
    
    /**
     * User card ma'lumotlarini olish
     */
    ApiResponse<?> getMyCard(Long userId);
    
    /**
     * Super Like statistikasini olish
     */
    ApiResponse<?> getSuperLikeStats(Long userId);
    
    /**
     * Sotib olish tarixini olish
     */
    ApiResponse<?> getMyPurchases(Long userId, Pageable pageable);
    
    /**
     * Yuborgan Super Like'larni olish
     */
    ApiResponse<?> getMySentSuperLikes(Long userId, Pageable pageable);
    
    /**
     * Olgan Super Like'larni olish
     */
    ApiResponse<?> getMyReceivedSuperLikes(Long userId, Pageable pageable);
    
    /**
     * Tranzaksiya tarixini olish
     */
    ApiResponse<?> getMyTransactions(Long userId, Pageable pageable);
}
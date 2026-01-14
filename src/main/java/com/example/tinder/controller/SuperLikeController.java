package com.example.tinder.controller;

import com.example.tinder.dto.base.ApiResponse;
import com.example.tinder.dto.superLike.PurchaseSuperLikeRequest;
import com.example.tinder.dto.superLike.SendSuperLikeRequest;
import com.example.tinder.entity.User;
import com.example.tinder.service.impl.SuperLikeServiceImpl;
import com.example.tinder.utils.CurrentUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/superlike")
@RequiredArgsConstructor
@Tag(name = "Super Like", description = "Super Like sotib olish va yuborish API'lari")
public class SuperLikeController {
    
    private final SuperLikeServiceImpl superLikeService;
    
    @GetMapping("/packages")
    @Operation(summary = "Barcha Super Like paketlarini olish")
    public ApiResponse<?> getAllPackages() {
        return superLikeService.getAllPackages();
    }
    
    @PostMapping("/purchase")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "Super Like sotib olish")
    public ApiResponse<?>  purchaseSuperLikes(
            @CurrentUser User user,
            @Valid @RequestBody PurchaseSuperLikeRequest request) {
        return superLikeService.purchaseSuperLikes(user.getId(), request);
    }
    
    @PostMapping("/send")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "Super Like yuborish")
    public ApiResponse<?>  sendSuperLike(
            @CurrentUser User user,
            @RequestBody SendSuperLikeRequest request) {
        return superLikeService.sendSuperLike(user.getId(), request);
    }

    
    @GetMapping("/stats")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "Super Like statistikasini olish")
    public ApiResponse<?>  getSuperLikeStats(@CurrentUser User user) {
        return superLikeService.getSuperLikeStats(user.getId());
    }
    
    @GetMapping("/purchases")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "Sotib olish tarixini olish")
    public ApiResponse<?>  getMyPurchases(
            @CurrentUser User user,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return superLikeService.getMyPurchases(user.getId(), pageable);
    }
    
    @GetMapping("/sent")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "Yuborgan Super Like'larimni olish")
    public ApiResponse<?>  getMySentSuperLikes(
            @CurrentUser User user,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return superLikeService.getMySentSuperLikes(user.getId(), pageable);
    }
    
    @GetMapping("/received")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "Olgan Super Like'larimni olish")
    public ApiResponse<?> getMyReceivedSuperLikes(
            @CurrentUser User user,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return superLikeService.getMyReceivedSuperLikes(user.getId(), pageable);
    }

}


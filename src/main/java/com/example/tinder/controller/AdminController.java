package com.example.tinder.controller;

import com.example.tinder.dto.base.ApiResponse;
import com.example.tinder.entity.User;
import com.example.tinder.service.AdminService;
import com.example.tinder.service.ProfilePictureService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;
    private final ProfilePictureService profilePictureService;

    @GetMapping("/users")
    public ApiResponse<Page<User>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return adminService.getAllUsers(page, size);
    }

    @PatchMapping("/users/{userId}/toggle-active")
    public ApiResponse<User> toggleUserActive(@PathVariable Long userId) {
        return adminService.toggleUserActive(userId);
    }


    @DeleteMapping("/picture/{userId}/{imageId}")
    public ApiResponse<?> deletePicture(
            @PathVariable Long userId,
            @PathVariable Long imageId
    ) {
        return profilePictureService.deletePicture(userId, imageId);
    }
}

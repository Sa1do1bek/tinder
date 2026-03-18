package com.example.tinder.controller;

import com.example.tinder.dto.UserDto;
import com.example.tinder.dto.UserStatsDTO;
import com.example.tinder.dto.base.ApiResponse;
import com.example.tinder.enums.UserStatsType;
import com.example.tinder.service.AdminService;
import com.example.tinder.service.ProfilePictureService;
import com.example.tinder.service.UserDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;
    private final ProfilePictureService profilePictureService;
    private final UserDashboardService userDashboardService;

    @GetMapping("/users")
    public ApiResponse<?> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return adminService.getAllUsers(page, size);
    }

    @PatchMapping("/users/{userId}/toggle-active")
    public ApiResponse<UserDto> toggleUserActive(@PathVariable Long userId) {
        return adminService.toggleUserActive(userId);
    }

    @GetMapping("/users/{userId}/pictures")
    public ApiResponse<?> getUserPictures(@PathVariable Long userId) {
        return profilePictureService.getUserPictures(userId);
    }

    @DeleteMapping("/picture/{userId}/{imageId}")
    public ApiResponse<?> deletePicture(
            @PathVariable Long userId,
            @PathVariable Long imageId
    ) {
        return profilePictureService.deletePicture(userId, imageId);
    }
    @GetMapping("/getAllUsersStats") // dashboard ga userlarni oy bo'yicha olish nechta registartsiya qilingan.
    public ApiResponse<List<UserStatsDTO>> getUserStats(@RequestParam(defaultValue = "MONTH") UserStatsType type,
                                                        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
                                                        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        return userDashboardService.getUserRegistrationStats(type,start,end);
    }
}

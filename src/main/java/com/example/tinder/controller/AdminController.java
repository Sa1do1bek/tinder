package com.example.tinder.controller;

import com.example.tinder.dto.AdminReportUpdateDto;
import com.example.tinder.dto.AllReportDto;
import com.example.tinder.dto.UserDto;
import com.example.tinder.dto.ban.BanUserRequest;
import com.example.tinder.dto.base.ApiResponse;
import com.example.tinder.dto.report.ReportDto;
import com.example.tinder.enums.ReportStatus;
import com.example.tinder.service.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
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
    private final ReportService reportService;
    private final ProfilePictureService profilePictureService;
    private final UserDashboardService userDashboardService;
    private final BanService banService;

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

    /**
     * REPORT CONTROLLERS
     */

    @GetMapping("/reports")
    public ApiResponse<Page<AllReportDto>> getAllReports(
            @RequestParam ReportStatus reportStatus,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return reportService.getAllReports(reportStatus,page, size);
    }


    @PatchMapping("/report/update/status")
    public ApiResponse<?> updateReport(@RequestParam Long reportId,
                                       @RequestParam ReportStatus status,
                                       @RequestBody @Valid AdminReportUpdateDto updateDto) {
        return reportService.updateReport(reportId, status, updateDto.getComment());
    }


    /**
     * dashboard ga "userlarni oy bo'yicha olish" nechta registartsiya qilingan.
     */
    @GetMapping("/get/all/users-stats")
    public ApiResponse<List<?>> getUserStats(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
                                             @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        return userDashboardService.getUserRegistrationStats(start, end);

    }

    /**
     * Dashboardga "hamma userlarni, premium userlar va verify bo'lgan" userlarni sonini olib keladi
     */
    @GetMapping("/get/all/user-count")
    public ApiResponse<?> getAllUserCount() {
        return userDashboardService.getAllUserCount();
    }
//    -------------------------------------

    @PostMapping("/users/{userId}/ban")
    public ApiResponse<?> banUser(@PathVariable Long userId, @RequestBody BanUserRequest banUserRequest) {
        return banService.banUser(userId, banUserRequest);
    }

    @DeleteMapping("/users/{userId}/ban")
    public ApiResponse<?> unBan(@PathVariable Long userId) {
        return banService.unBan(userId);
    }

    @GetMapping("/reported-users/{userId}/reports")
    public ApiResponse<List<ReportDto>> getUserReports(
            @PathVariable Long userId) {

    return reportService.getReportsByReportedUser(userId);
    }
}

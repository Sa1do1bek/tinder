package com.example.tinder.controller;

import com.example.tinder.dto.AdminReportUpdateDto;
import com.example.tinder.dto.UserDto;
import com.example.tinder.dto.base.ApiResponse;
import com.example.tinder.enums.ReportStatus;
import com.example.tinder.service.AdminService;
import com.example.tinder.service.ProfilePictureService;
import com.example.tinder.service.ReportService;
import jakarta.validation.Valid;
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
    private final ReportService reportService;
    private final ProfilePictureService profilePictureService;

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
    public ApiResponse<Page<?>> getAllReports(@RequestParam(required = false, defaultValue = "0") int page,
                                              @RequestParam(required = false, defaultValue = "10") int size) {
        return reportService.getAllReports(page, size);
    }

    @GetMapping("/reports/by-reporter-id")
    public ApiResponse<?> getReportByReportedId(@RequestParam() Long reportedId,
                                                @RequestParam(required = false, defaultValue = "0") int page,
                                                @RequestParam(required = false, defaultValue = "10") int size,
                                                @RequestParam(required = false) ReportStatus status) {
        return reportService.getReports(reportedId, status, page, size);
    }


    @PatchMapping("/report/update/status")
    public ApiResponse<?> updateReport(@RequestParam Long reportId,
                                       @RequestParam ReportStatus status,
                                       @RequestBody @Valid AdminReportUpdateDto updateDto) {
        return reportService.updateReport(reportId, status, updateDto.getComment());
    }
}

package com.example.tinder.controller;

import com.example.tinder.dto.AdminReportUpdateDto;
import com.example.tinder.dto.ReportAdminDTO;
import com.example.tinder.dto.UserDto;
import com.example.tinder.dto.ban.BanUserRequest;
import com.example.tinder.dto.ban.BanUserResponse;
import com.example.tinder.dto.base.ApiResponse;
import com.example.tinder.dto.report.ReportDto;
import com.example.tinder.enums.ReportStatus;
import com.example.tinder.service.AdminService;
import com.example.tinder.service.ProfilePictureService;
import com.example.tinder.service.ReportService;
import com.example.tinder.service.impl.BanServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import com.example.tinder.service.UserDashboardService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    private final BanServiceImpl banServiceImpl;

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
    public ApiResponse<Page<ReportAdminDTO>> getAllReports(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return reportService.getAllReports(page, size);
    }

    @GetMapping("/reports/by-reported-id")
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

    @GetMapping("/getAllUsersStats") // dashboard ga userlarni oy bo'yicha olish nechta registartsiya qilingan.
    public ApiResponse<List<?>> getUserStats(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
                                             @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        return userDashboardService.getUserRegistrationStats(start, end);

    }

//    -------------------------------------
    @PostMapping("/users/{userId}/ban")
    @ResponseStatus(HttpStatus.OK)
    public BanUserResponse banUser(@PathVariable Long userId, @RequestBody BanUserRequest banUserRequest) {
        return banServiceImpl.banUser(userId, banUserRequest);
    }

    @ResponseStatus(HttpStatus.OK)
    @DeleteMapping("/users/{userId}/ban")
    public BanUserResponse unBan(@PathVariable Long userId) {
        return banServiceImpl.unBan(userId);
    }

    @GetMapping("/reported-users/{userId}/reports")
    public ResponseEntity<ApiResponse<List<ReportDto>>> getUserReports(
            @PathVariable Long userId) {

        ApiResponse<List<ReportDto>> response = reportService.getReportsByReportedUser(userId);

        return ResponseEntity.ok(response);
    }
}

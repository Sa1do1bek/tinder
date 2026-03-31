package com.example.tinder.service.impl;


import com.example.tinder.dto.AllReportDto;
import com.example.tinder.dto.CreateReportUserDto;
import com.example.tinder.dto.base.ApiResponse;
import com.example.tinder.dto.report.ReportDto;
import com.example.tinder.entity.Report;
import com.example.tinder.entity.User;
import com.example.tinder.enums.ErrorType;
import com.example.tinder.enums.ReportReason;
import com.example.tinder.enums.ReportStatus;
import com.example.tinder.enums.SuccessType;
import com.example.tinder.exception.RestException;
import com.example.tinder.mapper.UserMapper;
import com.example.tinder.repository.ReportRepository;
import com.example.tinder.repository.UserRepository;
import com.example.tinder.service.ReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;


@Service
@Slf4j
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final ReportRepository reportRepository;
    private final UserRepository userRepository;
    private final BlockServiceImpl blockService;
    private final UserMapper userMapper;

    @Transactional(readOnly = true)
    @Override
    public ApiResponse<Page<AllReportDto>> getAllReports(ReportStatus status,int page, int size) {

        Pageable pageable = PageRequest.of(page, size);

        Page<AllReportDto> reports =
                reportRepository.findAllReport(status,pageable);

        return ApiResponse.success(reports, SuccessType.SUCCESSFULLY_GET_ALL_REPORTS);
    }



    @Transactional
    @Override
    public ApiResponse<?> createReport(CreateReportUserDto createReportDto) {


        if (createReportDto.getReason().equals(ReportReason.OTHER) && createReportDto.getDescription().isBlank()) {
            throw RestException.restThrow(ErrorType.OTHER_WITH_NO_DESCRIPTION);
        }

        Long reporterId = checkAnonymousUser().getId();
        Long reportedId = createReportDto.getReportedUserId();

        if (reportRepository.existsByReporterIdAndReportedId(reporterId, reportedId)) {
            throw RestException.restThrow(ErrorType.REPORT_ALREADY_EXISTS);
        }


        if (!isBlocked(reporterId, reportedId)) {
            throw new RestException(ErrorType.USER_BLOCK_FAILED);
        }

        User reporter = userRepository.findByIdCustom(reporterId).
                orElseThrow(() -> RestException.restThrow(ErrorType.USER_NOT_FOUND));
        User reported = userRepository.findByIdCustom(reportedId).
                orElseThrow(() -> RestException.restThrow(ErrorType.USER_NOT_FOUND));

        reportRepository.save(
                new Report(
                        reporter,
                        reported,
                        ReportStatus.PENDING,
                        createReportDto.getReason(),
                        createReportDto.getDescription(),
                        null,
                        null,
                        null));

        return ApiResponse.success(SuccessType.USER_SUCCESSFULLY_REPORTED);
    }


    @Transactional
    @Override
    public ApiResponse<?> updateReport(Long reportId, ReportStatus status, String comment) {

        User user = checkAnonymousUser();
        checkStatus(status);

        if (0 == reportRepository.updateReviewedBy(
                reportId,
                user,
                LocalDateTime.now(),
                status,
                comment,
                ReportStatus.PENDING)) {
            throw RestException.restThrow(ErrorType.REPORT_ADMIN_ALREADY_ASSIGNED);
        }

        return successUpdate();
    }

    private User checkAnonymousUser() {
        Object principal = SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();

        if (principal instanceof User user) {
            return user;
        }

        throw RestException.restThrow(ErrorType.USER_NOT_AUTHENTICATED);
    }

    private ApiResponse<?> successUpdate() {
        return ApiResponse.success(SuccessType.REPORT_STATUS_SUCCESSFULLY_UPDATED);
    }

    private boolean isBlocked(Long blocker, Long blocked) {
        return blockService.blockUser(blocker, blocked).getSuccess();
    }

    private void checkStatus(ReportStatus status) {
        if (ReportStatus.PENDING.equals(status)) {
            throw RestException.restThrow(ErrorType.REPORT_STATUS_CAN_NOT_BE_UPDATE_TO_PENDING);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<List<ReportDto>> getReportsByReportedUser(Long reportedUserId) {

        userRepository.findById(reportedUserId)
                .orElseThrow(() -> new RestException(ErrorType.USER_NOT_FOUND));

        List<Report> reports = reportRepository.findByReportedIdWithBothUsers(reportedUserId);

        List<ReportDto> reportDto = reports.stream()
                .map(report -> new ReportDto(
                        report.getId(),
                        report.getDescription(),
                        report.getReportReason(),
                        report.getCreatedAt(),
                        report.getReportStatus(),
                        userMapper.toDto(report.getReported()),
                        userMapper.toDto(report.getReporter())
                ))
                .toList();

        return ApiResponse.success(reportDto);
    }
}

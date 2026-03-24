package com.example.tinder.service;


import com.example.tinder.dto.CreateReportUserDto;
import com.example.tinder.dto.base.ApiResponse;
import com.example.tinder.enums.ReportStatus;
import org.springframework.data.domain.Page;

public interface ReportService {


    ApiResponse<Page<?>> getAllReports(int page, int size);

    ApiResponse<?> getReports(Long reportedId, ReportStatus status, int page, int size);

    ApiResponse<?> createReport(CreateReportUserDto createReportUserDto);

    ApiResponse<?> updateReport(Long reportId,  ReportStatus status,String comment);
}

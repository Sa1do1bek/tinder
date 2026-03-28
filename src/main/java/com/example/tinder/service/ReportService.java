package com.example.tinder.service;


import com.example.tinder.dto.AllReportDto;
import com.example.tinder.dto.CreateReportUserDto;
import com.example.tinder.dto.base.ApiResponse;
import com.example.tinder.dto.report.ReportDto;
import com.example.tinder.enums.ReportStatus;
import org.springframework.data.domain.Page;

import java.util.List;

public interface ReportService {


    ApiResponse<Page<AllReportDto>> getAllReports(ReportStatus status, int page, int size);

    ApiResponse<?> createReport(CreateReportUserDto createReportUserDto);

    ApiResponse<?> updateReport(Long reportId,  ReportStatus status,String comment);

    ApiResponse<List<ReportDto>> getReportsByReportedUser(Long reportedUserId);

}

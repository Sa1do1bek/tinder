package com.example.tinder.controller;


import com.example.tinder.dto.CreateReportUserDto;
import com.example.tinder.dto.base.ApiResponse;
import com.example.tinder.service.ReportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/report")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @PostMapping("/create")
    public ApiResponse<?> createReport( @RequestBody @Valid CreateReportUserDto reportDto) {
        return reportService.createReport(reportDto);
    }


}

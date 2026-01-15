package com.example.tinder.controller;

import com.example.tinder.dto.base.ApiResponse;
import com.example.tinder.service.RevenueReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Slf4j
@RestController
@RequestMapping("/api/v1/admin/revenue/report")
@RequiredArgsConstructor
@Tag(name = "Revenue Report", description = "Platform daromad hisobotlari")
@PreAuthorize("hasRole('ADMIN')")
public class RevenueReportController {


    private final RevenueReportService reportService;
    
    @GetMapping
    @Operation(
        summary = "To'liq hisobot olish",
        description = "Kunlik, haftalik, oylik yoki yillik hisobot. Diagram uchun ma'lumotlar ham qaytariladi."
    )
    public ApiResponse<?> getRevenueReport(
            @Parameter(description = "Davr: daily, weekly, monthly, yearly")
            @RequestParam(defaultValue = "monthly") String period,
            
            @Parameter(description = "Boshlanish sanasi (optional)")
            @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) 
            LocalDate startDate,
            
            @Parameter(description = "Tugash sanasi (optional)")
            @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) 
            LocalDate endDate) {
        
        log.info("GET /api/v1/admin/revenue/report - period: {}, startDate: {}, endDate: {}", 
                period, startDate, endDate);
        
        return reportService.getRevenueReport(period, startDate, endDate);
    }
    
    @GetMapping("/summary")
    @Operation(
        summary = "Qisqacha statistika (Dashboard uchun)",
        description = "Bugun, bu hafta, bu oy, bu yil va barcha vaqt statistikasi"
    )
    public ApiResponse<?> getRevenueSummary() {
        log.info("GET /api/v1/admin/revenue/report/summary");

        return reportService.getRevenueSummary();
    }
    
    @GetMapping("/compare")
    @Operation(
        summary = "Davr bilan taqqoslash",
        description = "Joriy davr vs oldingi davr taqqoslash. Masalan: bu oy vs o'tgan oy"
    )
    public ApiResponse<?> compareRevenue(
            @Parameter(description = "Davr: daily, weekly, monthly")
            @RequestParam(defaultValue = "monthly") String period) {

        return reportService.compareRevenue(period);
    }
    
    @GetMapping("/chart")
    @Operation(
        summary = "Faqat diagramma uchun ma'lumotlar",
        description = "Frontend Chart.js, Recharts yoki boshqa kutubxona uchun ma'lumotlar"
    )
    public ApiResponse<?> getChartData(
            @Parameter(description = "Davr: daily, weekly, monthly, yearly")
            @RequestParam(defaultValue = "monthly") String period,
            
            @Parameter(description = "Boshlanish sanasi (optional)")
            @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) 
            LocalDate startDate,
            
            @Parameter(description = "Tugash sanasi (optional)")
            @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) 
            LocalDate endDate) {
        
        return reportService.getChartData(period, startDate, endDate);
    }
}
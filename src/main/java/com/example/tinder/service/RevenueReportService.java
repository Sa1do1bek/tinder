package com.example.tinder.service;

import com.example.tinder.dto.base.ApiResponse;
import java.time.LocalDate;

public interface RevenueReportService {
    
    /**
     * To'liq hisobot (kunlik, haftalik, oylik, yillik)
     */
    ApiResponse<?> getRevenueReport(String period, LocalDate startDate, LocalDate endDate);

    /**
     * Qisqacha statistika (dashboard uchun)
     */
    ApiResponse<?> getRevenueSummary();

    /**
     * Taqqoslash (bugun vs kecha, bu oy vs o'tgan oy va h.k.)
     */
    ApiResponse<?> compareRevenue(String period);

    /**
     * Diagramma uchun ma'lumotlar
     */
    ApiResponse<?> getChartData(String period, LocalDate startDate, LocalDate endDate);
}
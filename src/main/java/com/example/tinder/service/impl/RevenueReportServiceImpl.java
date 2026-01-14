package com.example.tinder.service.impl;

import com.example.tinder.dto.base.ApiResponse;
import com.example.tinder.dto.report.DailyRevenueDto;
import com.example.tinder.dto.report.RevenueComparisonDto;
import com.example.tinder.dto.report.RevenueReportDto;
import com.example.tinder.dto.report.RevenueSummaryDto;
import com.example.tinder.enums.SuccessType;
import com.example.tinder.repository.PlatformRevenueRepository;
import com.example.tinder.service.RevenueReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RevenueReportServiceImpl implements RevenueReportService {
    
    private final PlatformRevenueRepository revenueRepository;
    
    @Override
    public ApiResponse<?> getRevenueReport(String period, LocalDate startDate, LocalDate endDate) {
        log.info("Generating revenue report: period={}, startDate={}, endDate={}", period, startDate, endDate);
        
        // Date range aniqlash
        DateRange dateRange = calculateDateRange(period, startDate, endDate);
        LocalDateTime start = dateRange.start();
        LocalDateTime end = dateRange.end();
        
        // Asosiy statistika
        BigDecimal totalIncome = revenueRepository.calculateTotalIncome(start);
        BigDecimal totalExpense = revenueRepository.calculateTotalExpense(start);
        BigDecimal netProfit = totalIncome.subtract(totalExpense);
        BigDecimal grossIncome = revenueRepository.calculateGrossIncome(start);
        BigDecimal totalWithdrawals = revenueRepository.calculateTotalWithdrawals(start);
        
        Long incomeCount = revenueRepository.countIncomeTransactions(start);
        Long expenseCount = revenueRepository.countExpenseTransactions(start);
        
        BigDecimal avgPurchase = revenueRepository.calculateAveragePurchaseAmount(start);
        BigDecimal avgWithdrawal = calculateAverageWithdrawal(start);
        
        // Kunlik breakdown (diagram uchun)
        List<DailyRevenueDto> dailyBreakdown = generateDailyBreakdown(start, end);
        
        RevenueReportDto report = RevenueReportDto.builder()
                .period(period)
                .startDate(dateRange.startDate())
                .endDate(dateRange.endDate())
                .totalIncome(totalIncome)
                .totalExpense(totalExpense)
                .netProfit(netProfit)
                .grossIncome(grossIncome)
                .totalWithdrawals(totalWithdrawals)
                .incomeTransactionsCount(incomeCount)
                .expenseTransactionsCount(expenseCount)
                .averagePurchaseAmount(avgPurchase != null ? avgPurchase : BigDecimal.ZERO)
                .averageWithdrawalAmount(avgWithdrawal)
                .dailyBreakdown(dailyBreakdown)
                .build();
        
        log.info("Report generated: income={}, expense={}, profit={}", totalIncome, totalExpense, netProfit);
        return ApiResponse.success(report, SuccessType.REVENUE_REPORT_GENERATED_SUCCESSFULLY);
    }
    
    @Override
    public ApiResponse<?> getRevenueSummary() {
        log.info("Fetching revenue summary");
        
        LocalDateTime now = LocalDateTime.now();
        
        // Bugun
        LocalDateTime todayStart = now.toLocalDate().atStartOfDay();
        BigDecimal todayIncome = revenueRepository.calculateTotalIncome(todayStart);
        BigDecimal todayExpense = revenueRepository.calculateTotalExpense(todayStart);
        
        // Bu hafta
        LocalDateTime weekStart = now.minusWeeks(1);
        BigDecimal weekIncome = revenueRepository.calculateTotalIncome(weekStart);
        BigDecimal weekExpense = revenueRepository.calculateTotalExpense(weekStart);
        
        // Bu oy
        LocalDateTime monthStart = now.minusMonths(1);
        BigDecimal monthIncome = revenueRepository.calculateTotalIncome(monthStart);
        BigDecimal monthExpense = revenueRepository.calculateTotalExpense(monthStart);
        
        // Bu yil
        LocalDateTime yearStart = now.minusYears(1);
        BigDecimal yearIncome = revenueRepository.calculateTotalIncome(yearStart);
        BigDecimal yearExpense = revenueRepository.calculateTotalExpense(yearStart);
        
        // Barcha vaqt
        LocalDateTime allTimeStart = LocalDateTime.of(2020, 1, 1, 0, 0);
        BigDecimal allTimeIncome = revenueRepository.calculateTotalIncome(allTimeStart);
        BigDecimal allTimeExpense = revenueRepository.calculateTotalExpense(allTimeStart);
        
        RevenueSummaryDto summary = RevenueSummaryDto.builder()
                .todayIncome(todayIncome)
                .todayExpense(todayExpense)
                .todayProfit(todayIncome.subtract(todayExpense))
                .weekIncome(weekIncome)
                .weekExpense(weekExpense)
                .weekProfit(weekIncome.subtract(weekExpense))
                .monthIncome(monthIncome)
                .monthExpense(monthExpense)
                .monthProfit(monthIncome.subtract(monthExpense))
                .yearIncome(yearIncome)
                .yearExpense(yearExpense)
                .yearProfit(yearIncome.subtract(yearExpense))
                .allTimeIncome(allTimeIncome)
                .allTimeExpense(allTimeExpense)
                .allTimeProfit(allTimeIncome.subtract(allTimeExpense))
                .build();
        
        log.info("Summary fetched: today={}, week={}, month={}", 
                summary.getTodayProfit(), summary.getWeekProfit(), summary.getMonthProfit());
        
        return ApiResponse.success(summary, SuccessType.REVENUE_SUMMARY_FETCHED_SUCCESSFULLY);
    }
    
    @Override
    public ApiResponse<?> compareRevenue(String period) {
        log.info("Comparing revenue for period: {}", period);
        
        LocalDateTime now = LocalDateTime.now();
        DateRange currentRange;
        DateRange previousRange;
        
        switch (period.toLowerCase()) {
            case "daily":
                currentRange = new DateRange(
                        now.toLocalDate().atStartOfDay(),
                        now,
                        now.toLocalDate(),
                        now.toLocalDate()
                );
                previousRange = new DateRange(
                        now.minusDays(1).toLocalDate().atStartOfDay(),
                        now.minusDays(1).toLocalDate().atTime(LocalTime.MAX),
                        now.minusDays(1).toLocalDate(),
                        now.minusDays(1).toLocalDate()
                );
                break;
            case "weekly":
                currentRange = new DateRange(now.minusWeeks(1), now, 
                        now.minusWeeks(1).toLocalDate(), now.toLocalDate());
                previousRange = new DateRange(now.minusWeeks(2), now.minusWeeks(1),
                        now.minusWeeks(2).toLocalDate(), now.minusWeeks(1).toLocalDate());
                break;
            case "monthly":
                currentRange = new DateRange(now.minusMonths(1), now,
                        now.minusMonths(1).toLocalDate(), now.toLocalDate());
                previousRange = new DateRange(now.minusMonths(2), now.minusMonths(1),
                        now.minusMonths(2).toLocalDate(), now.minusMonths(1).toLocalDate());
                break;
            default:
                currentRange = new DateRange(now.minusMonths(1), now,
                        now.minusMonths(1).toLocalDate(), now.toLocalDate());
                previousRange = new DateRange(now.minusMonths(2), now.minusMonths(1),
                        now.minusMonths(2).toLocalDate(), now.minusMonths(1).toLocalDate());
        }
        
        // Current period
        BigDecimal currentIncome = revenueRepository.calculateTotalIncome(currentRange.start());
        BigDecimal currentExpense = revenueRepository.calculateTotalExpense(currentRange.start());
        BigDecimal currentProfit = currentIncome.subtract(currentExpense);
        
        // Previous period
        BigDecimal previousIncome = revenueRepository.calculateTotalIncome(previousRange.start());
        BigDecimal previousExpense = revenueRepository.calculateTotalExpense(previousRange.start());
        BigDecimal previousProfit = previousIncome.subtract(previousExpense);
        
        // O'zgarish foizini hisoblash
        BigDecimal incomeChange = calculatePercentageChange(previousIncome, currentIncome);
        BigDecimal expenseChange = calculatePercentageChange(previousExpense, currentExpense);
        BigDecimal profitChange = calculatePercentageChange(previousProfit, currentProfit);
        
        RevenueComparisonDto comparison = RevenueComparisonDto.builder()
                .period(period)
                .currentIncome(currentIncome)
                .previousIncome(previousIncome)
                .incomeChange(incomeChange)
                .currentExpense(currentExpense)
                .previousExpense(previousExpense)
                .expenseChange(expenseChange)
                .currentProfit(currentProfit)
                .previousProfit(previousProfit)
                .profitChange(profitChange)
                .build();
        
        log.info("Comparison: incomeChange={}%, expenseChange={}%, profitChange={}%", 
                incomeChange, expenseChange, profitChange);
        
        return ApiResponse.success(comparison, SuccessType.REVENUE_COMPARISON_GENERATED_SUCCESSFULLY);
    }
    
    @Override
    public ApiResponse<?> getChartData(String period, LocalDate startDate, LocalDate endDate) {
        log.info("Generating chart data: period={}, startDate={}, endDate={}", period, startDate, endDate);
        
        DateRange dateRange = calculateDateRange(period, startDate, endDate);
        List<DailyRevenueDto> chartData = generateDailyBreakdown(dateRange.start(), dateRange.end());
        
        return ApiResponse.success(chartData, SuccessType.CHART_DATA_GENERATED_SUCCESSFULLY);
    }
    
    // ========== HELPER METHODS ==========
    
    private DateRange calculateDateRange(String period, LocalDate startDate, LocalDate endDate) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime start;
        LocalDateTime end = now;
        LocalDate startD;
        LocalDate endD = now.toLocalDate();
        
        if (startDate != null && endDate != null) {
            start = startDate.atStartOfDay();
            end = endDate.atTime(LocalTime.MAX);
            startD = startDate;
            endD = endDate;
        } else {
            switch (period.toLowerCase()) {
                case "daily":
                    start = now.toLocalDate().atStartOfDay();
                    startD = now.toLocalDate();
                    break;
                case "weekly":
                    start = now.minusWeeks(1);
                    startD = now.minusWeeks(1).toLocalDate();
                    break;
                case "monthly":
                    start = now.minusMonths(1);
                    startD = now.minusMonths(1).toLocalDate();
                    break;
                case "yearly":
                    start = now.minusYears(1);
                    startD = now.minusYears(1).toLocalDate();
                    break;
                default:
                    start = now.minusMonths(1);
                    startD = now.minusMonths(1).toLocalDate();
            }
        }
        
        return new DateRange(start, end, startD, endD);
    }
    
    private List<DailyRevenueDto> generateDailyBreakdown(LocalDateTime start, LocalDateTime end) {
        List<DailyRevenueDto> breakdown = new ArrayList<>();
        LocalDate currentDate = start.toLocalDate();
        LocalDate endDate = end.toLocalDate();
        
        while (!currentDate.isAfter(endDate)) {
            LocalDateTime dayStart = currentDate.atStartOfDay();
            LocalDateTime dayEnd = currentDate.atTime(LocalTime.MAX);
            
            BigDecimal dayIncome = revenueRepository.calculateTotalIncome(dayStart);
            BigDecimal dayExpense = revenueRepository.calculateTotalExpense(dayStart);
            Long incomeCount = revenueRepository.countIncomeTransactions(dayStart);
            Long expenseCount = revenueRepository.countExpenseTransactions(dayStart);
            
            breakdown.add(DailyRevenueDto.builder()
                    .date(currentDate)
                    .income(dayIncome)
                    .expense(dayExpense)
                    .profit(dayIncome.subtract(dayExpense))
                    .incomeCount(incomeCount)
                    .expenseCount(expenseCount)
                    .build());
            
            currentDate = currentDate.plusDays(1);
        }
        
        return breakdown;
    }
    
    private BigDecimal calculateAverageWithdrawal(LocalDateTime start) {
        Long count = revenueRepository.countExpenseTransactions(start);
        if (count == 0) return BigDecimal.ZERO;
        
        BigDecimal total = revenueRepository.calculateTotalWithdrawals(start);
        return total.divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP);
    }
    
    private BigDecimal calculatePercentageChange(BigDecimal previous, BigDecimal current) {
        if (previous.compareTo(BigDecimal.ZERO) == 0) {
            return current.compareTo(BigDecimal.ZERO) > 0 ? new BigDecimal("100") : BigDecimal.ZERO;
        }
        
        BigDecimal change = current.subtract(previous);
        return change.divide(previous, 4, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"))
                .setScale(2, RoundingMode.HALF_UP);
    }
    
    private record DateRange(LocalDateTime start, LocalDateTime end, LocalDate startDate, LocalDate endDate) {}
}
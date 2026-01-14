package com.example.tinder.dto.report;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;


@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RevenueReportDto {
    private String period; // "daily", "weekly", "monthly", "yearly"
    private LocalDate startDate;
    private LocalDate endDate;
    
    private BigDecimal totalIncome; // Jami kirim
    private BigDecimal totalExpense; // Jami chiqim
    private BigDecimal netProfit; // Sof foyda (kirim - chiqim)
    private BigDecimal grossIncome; // Jami sotilgan summa
    private BigDecimal totalWithdrawals; // Userlarga yechib berilgan
    
    // Tranzaksiya sonlari
    private Long incomeTransactionsCount;
    private Long expenseTransactionsCount;
    
    private BigDecimal averagePurchaseAmount;
    private BigDecimal averageWithdrawalAmount;
    
    private List<DailyRevenueDto> dailyBreakdown; // Kunlik breakdown
}
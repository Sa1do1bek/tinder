package com.example.tinder.dto.report;

import lombok.*;

import java.math.BigDecimal;


@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RevenueComparisonDto {
    private String period; // "vs_previous_period"
    
    private BigDecimal currentIncome;
    private BigDecimal previousIncome;
    private BigDecimal incomeChange; // %
    
    private BigDecimal currentExpense;
    private BigDecimal previousExpense;
    private BigDecimal expenseChange; // %
    
    private BigDecimal currentProfit;
    private BigDecimal previousProfit;
    private BigDecimal profitChange; // %
}
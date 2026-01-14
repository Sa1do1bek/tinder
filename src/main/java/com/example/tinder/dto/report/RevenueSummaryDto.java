package com.example.tinder.dto.report;

import lombok.*;

import java.math.BigDecimal;

/**
 * Qisqacha statistika (dashboard uchun)
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RevenueSummaryDto {
    private BigDecimal todayIncome;
    private BigDecimal todayExpense;
    private BigDecimal todayProfit;
    
    private BigDecimal weekIncome;
    private BigDecimal weekExpense;
    private BigDecimal weekProfit;
    
    private BigDecimal monthIncome;
    private BigDecimal monthExpense;
    private BigDecimal monthProfit;
    
    private BigDecimal yearIncome;
    private BigDecimal yearExpense;
    private BigDecimal yearProfit;
    
    private BigDecimal allTimeIncome;
    private BigDecimal allTimeExpense;
    private BigDecimal allTimeProfit;
}

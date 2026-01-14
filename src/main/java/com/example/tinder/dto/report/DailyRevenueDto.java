package com.example.tinder.dto.report;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DailyRevenueDto {
    private LocalDate date;
    private BigDecimal income;
    private BigDecimal expense;
    private BigDecimal profit; // income - expense
    private Long incomeCount;
    private Long expenseCount;
}
package com.example.tinder.service;

import com.example.tinder.dto.base.ApiResponse;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
public interface UserDashboardService {
    ApiResponse<List<?>> getUserRegistrationStats(
            LocalDate start,
            LocalDate end);
    //userlar statusini dashboard ga chiqaryapmiz DAY,MONTH,YEAR bo'yicha.
}

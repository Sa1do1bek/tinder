package com.example.tinder.service;

import com.example.tinder.dto.UserStatsDTO;
import com.example.tinder.dto.base.ApiResponse;
import com.example.tinder.enums.UserStatsType;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
public interface UserDashboardService {
    ApiResponse<List<UserStatsDTO>> getUserRegistrationStats(UserStatsType userStatsType, LocalDate start, LocalDate end);//userlar statusini dashboard ga chiqaryapmiz DAY,MONTH,YEAR bo'yicha.
}

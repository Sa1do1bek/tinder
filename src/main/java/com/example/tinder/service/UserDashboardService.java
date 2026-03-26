package com.example.tinder.service;

import com.example.tinder.dto.base.ApiResponse;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public interface UserDashboardService {

    /**
     * userlar "statusini dashboard ga chiqaryapmiz" "DAY,MONTH,YEAR" bo'yicha.
     */
    ApiResponse<List<?>> getUserRegistrationStats(
            LocalDate start,
            LocalDate end);


    /**
     * @return "hamma userlarni, premium userlar va verify bo'lgan" userlarni sonini DTO da.
     */
    ApiResponse<?> getAllUserCount();
}

package com.example.tinder.service.impl;

import com.example.tinder.dto.UserStatsDTO;
import com.example.tinder.dto.base.ApiResponse;
import com.example.tinder.enums.ErrorType;
import com.example.tinder.enums.SuccessType;
import com.example.tinder.enums.UserStatsType;
import com.example.tinder.exception.RestException;
import com.example.tinder.repository.UserRepository;
import com.example.tinder.service.UserDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;


@Service
@RequiredArgsConstructor
public class UserDashboardServiceImpl implements UserDashboardService {
    private final UserRepository userRepository;

    @Override
    public ApiResponse<List<UserStatsDTO>> getUserRegistrationStats(UserStatsType type,
                                                                    LocalDate start,
                                                                    LocalDate end) {
        if (start.isAfter(end) || end == null) {
            throw RestException.restThrow(ErrorType.INVALID_DATE);
        }

        String format = type.getFormat();

        List<Object[]> result = userRepository.getUserStats(
                start.atStartOfDay(),
                end.atTime(23, 59, 59),
                format
        );

        List<UserStatsDTO> stats = result.stream().map(r ->
                new UserStatsDTO((String) r[0],
                        ((Number) r[1]).longValue())).toList();
        return ApiResponse.success(stats, SuccessType.USER_STATS_SUCCESSFULLY_GET);
    }
}

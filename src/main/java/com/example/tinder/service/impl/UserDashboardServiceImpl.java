package com.example.tinder.service.impl;

import com.example.tinder.dto.UserStatsDTO;
import com.example.tinder.dto.base.ApiResponse;
import com.example.tinder.enums.ErrorType;
import com.example.tinder.enums.SuccessType;
import com.example.tinder.exception.RestException;
import com.example.tinder.repository.UserStatsRepository;
import com.example.tinder.service.UserDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;


@Service
@RequiredArgsConstructor
public class UserDashboardServiceImpl implements UserDashboardService {

    private final UserStatsRepository userStatsRepository;

    @Override
    public ApiResponse<List<?>> getUserRegistrationStats(LocalDate start,
                                                                    LocalDate end) {
        checkData(start, end);

        String formattedDate = getFormattedDate(start, end);

        List<UserStatsDTO> stats = userStatsRepository.getUserStats(
                        start.atStartOfDay(),
                        end.plusDays(1).atStartOfDay(),
                        formattedDate)
                .stream()
                .map(p -> new UserStatsDTO(p.getPeriod(), p.getCount()))
                .toList();

        return ApiResponse.success(stats, SuccessType.USER_STATS_SUCCESSFULLY_GET);
    }

    private void checkData(LocalDate start, LocalDate end) {
        if (start == null || end == null) {
            throw RestException.restThrow(ErrorType.INVALID_DATE);
        }

        if (start.isAfter(end)) {
            throw RestException.restThrow(ErrorType.START_DATE_CAN_NOT_BE_AFTER_END_DATE);
        }
    }

    private String getFormattedDate(LocalDate start, LocalDate end) {
        long days = ChronoUnit.DAYS.between(start, end);

        if (days <= 31) return "YYYY-MM-DD";
        if (days <= 365) return "YYYY-MM";
        return "YYYY";
    }

}

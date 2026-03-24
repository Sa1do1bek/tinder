package com.example.tinder.service.impl;

import com.example.tinder.dto.ProfileViewStatsDTO;
import com.example.tinder.dto.base.ApiResponse;
import com.example.tinder.entity.User;
import com.example.tinder.enums.ErrorType;
import com.example.tinder.enums.SuccessType;
import com.example.tinder.exception.RestException;
import com.example.tinder.repository.SwipeRepository;
import com.example.tinder.service.ProfileViewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProfileViewServiceImpl implements ProfileViewService {
    private final SwipeRepository repository;

    // profile ko'rishlar soni ni olish
    @Override
    @Transactional(readOnly = true)
    public ApiResponse<?> getProfileViewCount() {

        Long userId = checkAnonymUser();

        Long count = repository.countViewsByUserId(userId);
        return ApiResponse.success(count, SuccessType.PROFILE_VIEW_SUCCESSFULLY_GET);

    }

    // current userni topyapmiz va id sini qaytaryapmiz
    private Long checkAnonymUser() {
        Object principal = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        if (principal instanceof User user) {
            return user.getId();
        }

        throw new RestException(ErrorType.USER_NOT_FOUND);
    }

    //profil ni nechi kishi ko'rishi sana bo'yicha statiskasi
    @Override
    public ApiResponse<?> getProfileViewStats(LocalDate start, LocalDate end) {

        checkData(start, end);

        Long userId = checkAnonymUser();

        String format = getFormattedDate(start, end);

        List<ProfileViewStatsDTO> stats = repository.getProfileViewStats(
                        userId,
                        start.atStartOfDay(),
                        end.plusDays(1).atStartOfDay(),
                        format
                )
                .stream()
                .map(p -> new ProfileViewStatsDTO(
                        p.getPeriod(),
                        p.getCount()
                ))
                .toList();

        return ApiResponse.success(stats,SuccessType.PROFILE_VIEW_STATS_SUCCESSFULLY_GET);
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

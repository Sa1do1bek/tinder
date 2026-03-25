package com.example.tinder.service.impl;

import com.example.tinder.dto.base.ApiResponse;
import com.example.tinder.entity.User;
import com.example.tinder.enums.ErrorType;
import com.example.tinder.exception.RestException;
import com.example.tinder.repository.SwipeRepository;
import com.example.tinder.service.ProfileViewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProfileViewServiceImpl implements ProfileViewService {
    private final SwipeRepository repository;

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<?> getProfileViewCount(LocalDate start, LocalDate end) {

        Long userId = checkAnonymUser();

        // default: hech nima kiritmasa hozirgi kundan oxirgi 1 oy ni olib beradi
        LocalDateTime startDate = (start != null)
                ? start.atStartOfDay()
                : LocalDateTime.now().minusMonths(1);

        LocalDateTime endDate = (end != null)
                ? end.atTime(23, 59, 59)
                : LocalDateTime.now();

        Long count = repository.countProfileViews(userId, startDate, endDate);

        return ApiResponse.success(count);
    }

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


}

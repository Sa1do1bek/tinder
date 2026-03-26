package com.example.tinder.service.impl;

import com.example.tinder.dto.ban.BanUserRequest;
import com.example.tinder.dto.ban.BanUserResponse;
import com.example.tinder.dto.base.ApiResponse;
import com.example.tinder.entity.User;
import com.example.tinder.enums.ErrorType;
import com.example.tinder.enums.UserStatus;
import com.example.tinder.exception.RestException;
import com.example.tinder.repository.UserRepository;
import com.example.tinder.service.BanService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class BanServiceImpl implements BanService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public ApiResponse<BanUserResponse> banUser(Long userId, BanUserRequest ban) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User topilmadi" + userId));

        LocalDateTime banDateTime = LocalDateTime.now();

        if (ban.permanent()) {
            user.setStatus(UserStatus.BANNED_PERM);
            user.setBannedAt(banDateTime);
            user.setBanUtil(null);
            user.setBanReason(ban.banReason());
        } else {

            if (ban.banUtil() == null) {
                throw new RestException(ErrorType.USER_BANNED_PERMANENT);
            }
            if (ban.banUtil().isBefore(banDateTime)) {
                throw new RestException(ErrorType.USER_BANNED_TEMP);
            }

            user.setStatus(UserStatus.BANNED_TEMP);
            user.setBannedAt(banDateTime);
            user.setBanUtil(ban.banUtil());
            user.setBanReason(ban.banReason());

        }

        userRepository.save(user);

        BanUserResponse banUserResponse = toResponse(user, ban.permanent());

        return ApiResponse.success(banUserResponse);
    }

    @Override
    @Transactional
    public ApiResponse<BanUserResponse> unBan(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("user topilmadi" + userId));

        user.setStatus(UserStatus.ACTIVE);
        user.setBannedAt(null);
        user.setBanUtil(null);
        user.setBanReason(null);
        userRepository.save(user);
        BanUserResponse banUserResponse = new  BanUserResponse(
                user.getId(),
                user.getUsername(),
                UserStatus.ACTIVE,
                null,
                null,
                false,
                null
        );

        return ApiResponse.success(banUserResponse);
    }

    private BanUserResponse toResponse(User user, boolean permanent) {
        return new BanUserResponse(
                user.getId(),
                user.getUsername(),
                user.getStatus(),
                user.getBannedAt(),
                user.getBanUtil(),
                permanent,
                user.getBanReason()
        );
    }
}

package com.example.tinder.service.impl;

import com.example.tinder.entity.User;
import com.example.tinder.enums.ErrorType;
import com.example.tinder.enums.UserStatus;
import com.example.tinder.exception.RestException;
import com.example.tinder.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsServiceImpl implements UserDetailsService {
    private final UserRepository userRepository;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) {

        User user = userRepository.findByUsernameAndActive(username, true)
                .orElseThrow(() -> new UsernameNotFoundException("User not found or disabled"));

        if (user.getStatus() == UserStatus.BANNED_TEMP) {
            if (user.getBanUtil() != null &&
                    LocalDateTime.now().isAfter(user.getBanUtil())) {
                user.setStatus(UserStatus.ACTIVE);
                user.setBannedAt(null);
                user.setBanUtil(null);
                user.setBanReason(null);
                userRepository.save(user);
            } else {
                throw new RestException(ErrorType.USER_BANNED_TEMP);
            }
        }
        if (user.getStatus() == UserStatus.BANNED_PERM) {
            throw new RestException(ErrorType.USER_BANNED_PERMANENT);
        }
        return user;
    }

}
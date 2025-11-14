package com.example.tinder.mapper;

import com.example.tinder.dto.RegisterDto;
import com.example.tinder.entity.PendingUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class PendingUserMapper {

    private final PasswordEncoder passwordEncoder;

    public PendingUser toPendingUser(PendingUser user, RegisterDto dto, String code) {
        user.setEmail(dto.getEmail());
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setFullName(dto.getFullName());
        user.setGender(dto.getGender());
        user.setBirthDate(dto.getBirthDate());
        user.setDeviceId(dto.getDeviceId());
        user.setFcmToken(dto.getFcmToken());
        user.setRole(dto.getRole());
        user.setCode(code);
        user.setCreatedAt(LocalDateTime.now());
        return user;
    }

}

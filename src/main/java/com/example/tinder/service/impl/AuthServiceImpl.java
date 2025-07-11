package com.example.tinder.service.impl;

import com.example.tinder.dto.ApiResponse;
import com.example.tinder.dto.LoginDto;
import com.example.tinder.dto.RegisterDto;
import com.example.tinder.dto.VerifyDto;
import com.example.tinder.entity.*;
import com.example.tinder.enums.Role;
import com.example.tinder.enums.UserStatus;
import com.example.tinder.exception.ResourceNotFoundException;
import com.example.tinder.mapper.UserMapper;
import com.example.tinder.repository.*;
import com.example.tinder.service.AuthService;
import com.example.tinder.service.DeviceService;
import com.example.tinder.utils.JwtUtil;
import com.example.tinder.utils.Utils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final DeviceRepository deviceRepository;
    private final TokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final CodeRepository codeRepository;
    private final UserRepository userRepository;
    private final DeviceService deviceService;
    private final UserMapper userMapper;
    private final JwtUtil jwtUtil;
    private final Utils utils;
    private final PendingUserRepository pendingUserRepository;

    @Override
    public ApiResponse registerByEmail(RegisterDto dto) {
        if (!utils.checkEmail(dto.getEmail())) {
            return ApiResponse.builder()
                    .status(HttpStatus.BAD_REQUEST)
                    .message("Invalid email format")
                    .build();
        }

        if (userRepository.existsByEmail(dto.getEmail())) {
            return ApiResponse.builder()
                    .status(HttpStatus.BAD_REQUEST)
                    .message("Email already registered")
                    .build();
        }

        if (userRepository.existsByUsername(dto.getUsername())) {
            return ApiResponse.builder()
                    .status(HttpStatus.BAD_REQUEST)
                    .message("Username already taken")
                    .build();
        }

        String code = utils.getCode();

        if (!utils.sendCodeToMail(dto.getEmail(), code)) {
            return ApiResponse.builder()
                    .status(HttpStatus.BAD_REQUEST)
                    .message("Error sending verification code")
                    .build();
        }

        PendingUser pendingUser = PendingUser.builder()
                .email(dto.getEmail())
                .username(dto.getUsername())
                .password(passwordEncoder.encode(dto.getPassword()))
                .fullName(dto.getFullName())
                .gender(dto.getGender())
                .birthDate(dto.getBirthDate())
                .deviceId(dto.getDeviceId())
                .role(dto.getRole())
                .code(code)
                .createdAt(LocalDateTime.now())
                .build();

        pendingUserRepository.save(pendingUser);

        return ApiResponse.builder()
                .status(HttpStatus.OK)
                .message("Verification code sent to email")
                .build();
    }


    @Override
    @Transactional
    public ApiResponse verify(VerifyDto dto) {
        PendingUser pending = pendingUserRepository.findByEmail(dto.getEmail())
                .orElse(null);

        if (pending == null || !pending.getCode().equals(dto.getCode())) {
            return ApiResponse.builder()
                    .status(HttpStatus.BAD_REQUEST)
                    .message("Invalid verification code or email")
                    .build();
        }

        UserRole role = roleRepository.findByName(pending.getRole()).orElse(null);

        User user = User.builder()
                .email(pending.getEmail())
                .username(pending.getUsername())
                .password(pending.getPassword())
                .status(UserStatus.ACTIVE)
                .gender(pending.getGender())
                .birthDate(pending.getBirthDate())
                .fullname(pending.getFullName())
                .role(role != null ? Set.of(role) : new HashSet<>())
                .build();

        User saved = userRepository.save(user);

        Device device = deviceService.addTrustedDevice(saved.getId(), pending.getDeviceId());

        String jwtToken = jwtUtil.generateToken(user.getUsername());

        Token token = Token.builder()
                .user(saved)
                .token(jwtToken)
                .type("Bearer")
                .createdAt(LocalDateTime.now())
                .expiredAt(LocalDateTime.now().plusDays(10))
                .build();

        tokenRepository.save(token);

        pendingUserRepository.delete(pending);

        return ApiResponse.builder()
                .status(HttpStatus.OK)
                .message("User registered")
                .data(Map.of(
                        "token", jwtToken,
                        "user", userMapper.toDto(saved)
                ))
                .build();
    }


    @Override
    @Transactional
    public ApiResponse loginByEmail(LoginDto dto) {
        if (dto.getEmail() == null || dto.getEmail().trim().isEmpty()) {
            return ApiResponse.builder()
                    .status(HttpStatus.BAD_REQUEST)
                    .message("User email invalid")
                    .build();
        }

        User user;
        try {
            user = this.userRepository.findByEmail(dto.getEmail())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        } catch (ResourceNotFoundException e) {
            return ApiResponse.builder()
                    .status(HttpStatus.BAD_REQUEST)
                    .message("Invalid authentication characters")
                    .build();
        }

        if (!utils.checkEmail(dto.getEmail())) {
            return ApiResponse.builder()
                    .status(HttpStatus.BAD_REQUEST)
                    .message("Invalid credentials")
                    .build();
        }

        try {
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(user.getUsername(), dto.getPassword());
            authenticationManager.authenticate(authentication);
        } catch (Exception e) {
            return ApiResponse.builder()
                    .status(HttpStatus.BAD_REQUEST)
                    .message("Invalid credentials")
                    .build();
        }

        Token token = this.tokenRepository.findByUserId(user.getId()).orElse(null);
        String userToken = null;
        if (token != null) {
            userToken = token.getToken();
            if (token.getExpiredAt() != null && token.getExpiredAt().isBefore(LocalDateTime.now())) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());
                userToken = jwtUtil.generateToken(userDetails.getUsername());
                token.setToken(userToken);
                token.setCreatedAt(LocalDateTime.now());
                token.setExpiredAt(LocalDateTime.now().plusDays(10));
                this.tokenRepository.save(token);
            }
        }

        Device trustedDevice = this.deviceRepository.findFirstByUserIdAndDeviceName(user.getId(), dto.getDeviceId())
                .orElse(null);

        String code = utils.getCode();
        if (trustedDevice == null) {
            if (utils.sendCodeToMail(dto.getEmail(), code)) {
                Device device = new Device();
                device.setUser(user);
                device.setDeviceName(dto.getDeviceId());
                Device save = this.deviceRepository.save(device);

                Code c = new Code();
                c.setCreatedAt(LocalDateTime.now());
                c.setCode(code);
                c.setUser(user);
                c.setDevice(save);
                this.codeRepository.save(c);

                return ApiResponse.builder()
                        .code(0)
                        .status(HttpStatus.OK)
                        .message("Code sent")
                        .data(userToken)
                        .meta(Map.of("user", this.userMapper.toDto(user)))
                        .build();
            }
        }

        return ApiResponse.builder()
                .code(1)
                .status(HttpStatus.OK)
                .message("SUCCESS")
                .data(userToken)
                .meta(Map.of("user", this.userMapper.toDto(user)))
                .build();
    }

    @Override
    @Transactional
    public ApiResponse resendCodeToEmail(String email, String deviceId, Long id) {
        if (email == null || email.trim().isEmpty()) {
            return ApiResponse.builder()
                    .status(HttpStatus.BAD_REQUEST)
                    .message("User email invalid")
                    .build();
        }

        User user = this.userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Device device = this.deviceRepository.findFirstByUserIdAndDeviceName(user.getId(), deviceId)
                .orElseThrow(() -> new ResourceNotFoundException("Device not found"));

        String code = utils.getCode();
        if (utils.sendCodeToMail(email, code)) {
            Code c = this.codeRepository.findByUserIdAndDeviceId(user.getId(), device.getId()).orElse(null);
            if (c != null) {
                c.setCode(code);
                c.setCreatedAt(LocalDateTime.now());
                this.codeRepository.save(c);
            }
            return ApiResponse.builder()
                    .status(HttpStatus.OK)
                    .message("Auth code sent")
                    .build();
        }
        return ApiResponse.builder()
                .status(HttpStatus.BAD_REQUEST)
                .message("Error while sending auth code")
                .build();
    }

    @Override
    public ApiResponse checkCodeByEmail(String email, String code, String deviceId) {
        if (email == null || email.trim().isEmpty()) {
            return ApiResponse.builder()
                    .status(HttpStatus.BAD_REQUEST)
                    .message("User email invalid")
                    .build();
        }

        User user = this.userRepository.findByEmail(email).orElse(null);
        if (user != null) {
            Device device = this.deviceRepository.findFirstByUserIdAndDeviceName(user.getId(), deviceId)
                    .orElse(null);
            if (device != null) {
                Code c = this.codeRepository.findByUserIdAndDeviceId(user.getId(), device.getId()).orElse(null);

                if (c != null && c.getDevice() != null && c.getDevice().getUser() != null
                        && c.getDevice().getUser().getId().equals(user.getId())) {
                    c.getDevice().setAddedAt(LocalDateTime.now());
                    c.getDevice().setDeviceName(deviceId);
                    c.getDevice().setExpiredAt(LocalDateTime.now().plusDays(15));
                    this.deviceRepository.save(c.getDevice());
                }

                if (!this.utils.checkCode(email, code, device.getId())) {
                    return ApiResponse.builder()
                            .status(HttpStatus.BAD_REQUEST)
                            .message("Error")
                            .build();
                }

                Map<String, Object> map = new HashMap<>();
                Device devices = this.deviceService.addTrustedDeviceByLogin(user.getId(), deviceId);
                devices.setUser(user);
                devices.setDeviceName(deviceId);
                Device save = this.deviceRepository.save(devices);

                Code cc = this.codeRepository.findByUserIdAndDeviceId(user.getId(), device.getId()).orElse(null);
                if (cc != null) {
                    cc.setApprovedAt(LocalDateTime.now());
                    cc.setDevice(save);
                    this.codeRepository.save(cc);
                }

                Token token = this.tokenRepository.findByUserId(user.getId()).orElse(null);
                map.put("token", token != null ? token.getToken() : "");
                map.put("user", this.userMapper.toDto(user));

                return ApiResponse.builder()
                        .status(HttpStatus.OK)
                        .message("SUCCESS")
                        .data(map)
                        .build();
            }
        }

        return ApiResponse.builder()
                .status(HttpStatus.BAD_REQUEST)
                .message("User not found")
                .build();
    }
}
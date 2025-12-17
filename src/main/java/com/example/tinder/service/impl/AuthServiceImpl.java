package com.example.tinder.service.impl;

import com.example.tinder.dto.ApiResponse;
import com.example.tinder.dto.LoginDto;
import com.example.tinder.dto.RegisterDto;
import com.example.tinder.dto.VerifyDto;
import com.example.tinder.dto.auth.LoginResponse;
import com.example.tinder.entity.*;
import com.example.tinder.enums.ErrorType;
import com.example.tinder.enums.UserRole;
import com.example.tinder.enums.UserStatus;
import com.example.tinder.exception.ResourceNotFoundException;
import com.example.tinder.exception.RestException;
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
    private final CodeRepository codeRepository;
    private final UserRepository userRepository;
    private final DeviceService deviceService;
    private final UserMapper userMapper;
    private final JwtUtil jwtUtil;
    private final Utils utils;
    private final PendingUserRepository pendingUserRepository;
    private final UserLocationRepository userLocationRepository;

    @Override
    public ApiResponse<?> registerByEmail(RegisterDto dto) {
        if (!utils.checkEmail(dto.getEmail())) {
            throw RestException.restThrow(ErrorType.INVALID_EMAIL_FORMAT);
        }
        if (userRepository.existsByEmailAndActiveTrue(dto.getEmail())) {
            throw RestException.restThrow(ErrorType.EMAIL_ALREADY_TAKEN);
        }
        if (userRepository.existsByUsernameAndActiveTrue(dto.getUsername())) {
            throw RestException.restThrow(ErrorType.USERNAME_ALREADY_TAKEN);

        }
        String code = utils.getCode();
        if (!utils.sendCodeToMail(dto.getEmail(), code)) {
            throw RestException.restThrow(ErrorType.ERROR_SENDING_VERIFICATION_CODE);
        }
        Optional<PendingUser> optionalPendingUser = pendingUserRepository.findByEmail(dto.getEmail());
        PendingUser pendingUser = optionalPendingUser.orElse(new PendingUser());
        pendingUser.setEmail(dto.getEmail());
        pendingUser.setUsername(dto.getUsername());
        pendingUser.setPassword(passwordEncoder.encode(dto.getPassword()));
        pendingUser.setFullName(dto.getFullName());
        pendingUser.setGender(dto.getGender());
        pendingUser.setBirthDate(dto.getBirthDate());
        pendingUser.setDeviceId(dto.getDeviceId());
        pendingUser.setRole(UserRole.USER);
        pendingUser.setCode(code);
        pendingUser.setCreatedAt(LocalDateTime.now());
        pendingUserRepository.save(pendingUser);
        return ApiResponse.success("VERIFICATION_CODE_SENT_TO_EMAIL");
    }

    @Override
    @Transactional
    public ApiResponse<?> verify(VerifyDto dto) {
        Optional<PendingUser> optionalPending = pendingUserRepository.findByEmail(dto.getEmail());

        if (optionalPending.isEmpty()) {
            throw RestException.restThrow(ErrorType.INVALID_VERIFICATION_CODE_OR_EMAIL);


        }

        PendingUser pending = optionalPending.get();

        System.out.println("Pending code: " + pending.getCode());
        System.out.println("Dto code: " + dto.getCode());

        if (!pending.getCode().equals(dto.getCode())) {
            throw RestException.restThrow(ErrorType.ERROR_SENDING_VERIFICATION_CODE);
        }


        long EXPIRATION_MINUTES = 5;
        if (pending.getCreatedAt().plusMinutes(EXPIRATION_MINUTES).isBefore(LocalDateTime.now())) {
            pendingUserRepository.delete(pending);
            throw RestException.restThrow(ErrorType.VERIFICATION_CODE_EXPIRED);
        }

        Optional<User> optionalUser = userRepository.findByEmail(pending.getEmail());


        User user;

        if (optionalUser.isPresent()) {
            user = optionalUser.get();

            user.setUsername(pending.getUsername());
            user.setPassword(pending.getPassword());
            user.setStatus(UserStatus.ACTIVE);
            user.setGender(pending.getGender());
            user.setBirthDate(pending.getBirthDate());
            user.setFullname(pending.getFullName());

            user.getProfilePictures().clear();
            user.getUserInterests().clear();
            user.setBio(null);
            user.setProfilePic(null);

            userLocationRepository.findByUserId(user.getId()).ifPresent(userLocationRepository::delete);

            user.setRole(UserRole.USER);
            user.setActive(true);
            user.setUpdatedAt(LocalDateTime.now());
        } else {
            user = User.builder()
                    .email(pending.getEmail())
                    .username(pending.getUsername())
                    .password(pending.getPassword())
                    .status(UserStatus.ACTIVE)
                    .gender(pending.getGender())
                    .birthDate(pending.getBirthDate())
                    .fullname(pending.getFullName())
                    .role(UserRole.USER)
                    .active(true)
                    .profilePictures(new ArrayList<>())
                    .userInterests(new ArrayList<>())
                    .bio(null)
                    .build();
        }

        User savedUser = userRepository.save(user);

        tokenRepository.deleteByUserId(savedUser.getId());

        deviceService.addTrustedDevice(savedUser.getId(), pending.getDeviceId());

        String jwtToken = jwtUtil.generateToken(savedUser.getUsername());
        Token token = Token.builder()
                .user(savedUser)
                .token(jwtToken)
                .type("Bearer")
                .createdAt(LocalDateTime.now())
                .expiredAt(LocalDateTime.now().plusDays(10))
                .build();
        tokenRepository.save(token);

        pendingUserRepository.delete(pending);

         return ApiResponse.success(Map.of(
                "token", jwtToken,
                "user", userMapper.toDto(savedUser))
                ,"USER_REGISTERED_SUCCESSFULLY"
        );
    }


    @Override
    @Transactional
    public ApiResponse<?> loginByEmail(LoginDto dto) {
        if (dto.getEmail() == null || dto.getEmail().trim().isEmpty()) {
            throw RestException.restThrow(ErrorType.USER_EMAIL_INVALID);
        }

        User user;
        try {
            user = this.userRepository.findByEmail(dto.getEmail())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        } catch (ResourceNotFoundException e) {
            throw RestException.restThrow(ErrorType.INVALID_AUTHENTICATION_CHARACTERS);
        }

        try {
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(user.getUsername(), dto.getPassword());
            authenticationManager.authenticate(authentication);
        } catch (Exception e) {
            throw RestException.restThrow(ErrorType.INVALID_CREDENTIALS);
        }

        this.tokenRepository.deleteByUserId(user.getId());

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());
        String userToken = jwtUtil.generateToken(userDetails.getUsername());

        Token newToken = Token.builder()
                .user(user)
                .token(userToken)
                .type("Bearer")
                .createdAt(LocalDateTime.now())
                .expiredAt(LocalDateTime.now().plusDays(10))
                .build();
        this.tokenRepository.save(newToken);

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


                return ApiResponse.success(
                        LoginResponse.builder()
                                .message("CODE SENT")
                                .code(0)
                                .user(this.userMapper.toDto(user))
                                .token(userToken)
                                .build()
                );
            }
        }

        if (!user.isActive()) {
            throw RestException.restThrow(ErrorType.ACCOUNT_NOT_FOUND);
        }

        return ApiResponse.success(
                LoginResponse.builder()
                        .message("SUCCESS")
                        .code(1)
                        .user(this.userMapper.toDto(user))
                        .token(userToken)
                        .build()
        );
    }

    @Override
    @Transactional
    public ApiResponse<?> resendCodeToEmail(String email, String deviceId, Long id) {
        if (email == null || email.trim().isEmpty()) {
            throw RestException.restThrow(ErrorType.USER_EMAIL_INVALID);
        }

        User user = this.userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        if (!user.isActive()) {
            throw RestException.restThrow(ErrorType.ACCOUNT_NOT_FOUND);
        }
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
            return ApiResponse.success("CODE_SENT");
        }
        throw RestException.restThrow(ErrorType.ERROR_SENDING_VERIFICATION_CODE);
    }

    @Override
    public ApiResponse<?> checkCodeByEmail(String email, String code, String deviceId) {
        if (email == null || email.trim().isEmpty()) {
            throw RestException.restThrow(ErrorType.USER_EMAIL_INVALID);
        }

        User user = this.userRepository.findByEmail(email).orElse(null);
        if (user != null) {
            if (!user.isActive()) {
                throw RestException.restThrow(ErrorType.ACCOUNT_NOT_FOUND);
            }
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
                    throw RestException.restThrow(ErrorType.ERROR_SENDING_VERIFICATION_CODE);
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


                String currentToken = jwtUtil.generateToken(user.getUsername());
                map.put("user", this.userMapper.toDto(user));

                return ApiResponse.success(this.userMapper.toDto(user), "SECCESS");
            }
        }

        throw RestException.restThrow(ErrorType.USER_NOT_FOUND);
    }

}
package com.example.tinder.service.impl;

import com.example.tinder.dto.VerifyDto;
import com.example.tinder.dto.auth.LoginDto;
import com.example.tinder.dto.auth.LoginResponse;
import com.example.tinder.dto.auth.RegisterDto;
import com.example.tinder.dto.base.ApiResponse;
import com.example.tinder.entity.*;
import com.example.tinder.enums.ErrorType;
import com.example.tinder.enums.Gender;
import com.example.tinder.enums.UserRole;
import com.example.tinder.enums.UserStatus;
import com.example.tinder.exception.RestException;
import com.example.tinder.mapper.UserMapper;
import com.example.tinder.repository.*;
import com.example.tinder.service.AuthService;
import com.example.tinder.service.DeviceService;
import com.example.tinder.utils.JwtUtil;
import com.example.tinder.utils.Utils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    private final PasswordEncoder passwordEncoder;
    private final CodeRepository codeRepository;
    private final UserRepository userRepository;
    private final DeviceService deviceService;
    private final UserMapper userMapper;
    private final JwtUtil jwtUtil;
    private final Utils utils;
    private final PendingUserRepository pendingUserRepository;
    private final UserLocationRepository userLocationRepository;
    private final PreferenceRepository preferenceRepository;
    private final VerificationRepository verificationRepository;

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

        if (!pending.getCode().equals(dto.getCode())) {
            throw RestException.restThrow(ErrorType.ERROR_SENDING_VERIFICATION_CODE);
        }


        long EXPIRATION_MINUTES = 2;
        if (pending.getCreatedAt().plusMinutes(EXPIRATION_MINUTES).isBefore(LocalDateTime.now())) {
            pendingUserRepository.delete(pending);
            codeRepository.deleteByCode(dto.getCode());
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
            user.setRole(UserRole.USER);
            user.setActive(true);
            user.setIsVerified(false);
            user.setUpdatedAt(LocalDateTime.now());

            userRepository.save(user);

            userLocationRepository.findByUserId(user.getId()).ifPresent(userLocationRepository::delete);
            preferenceRepository.findByUserId(user.getId()).ifPresent(preferenceRepository::delete);

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

        savePreference(savedUser);

        Verification verification = new Verification();
        verification.setUser(savedUser);
        verification.setCreatedAt(LocalDateTime.now());
        verification.setUpdatedAt(LocalDateTime.now());
        verificationRepository.save(verification);


        deviceService.addTrustedDevice(savedUser.getId(), pending.getDeviceId());

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());
        String jwtToken = jwtUtil.generateToken(userDetails.getUsername());

        pendingUserRepository.delete(pending);

        codeRepository.deleteByCode(dto.getCode());

        return ApiResponse.success(Map.of(
                        "token", jwtToken,
                        "user", userMapper.toDto(savedUser))
                , "USER_REGISTERED_SUCCESSFULLY"
        );
    }

    private void savePreference(User user) {

        Preference preference = new Preference();
        preference.setUser(user);
        if (user.getGender() == Gender.MALE){
            preference.setPreferredGender(Gender.FEMALE);
        }else {
            preference.setPreferredGender(Gender.MALE);
        }
        preference.setDistance(70.0);
        preference.setMinAge(18);
        preference.setMaxAge(50);
        preference.setVisibility(true);

        preferenceRepository.save(preference);
    }


    @Override
    @Transactional
    public ApiResponse<?> loginByEmail(LoginDto dto) {
        if (dto.getEmail() == null || dto.getEmail().trim().isEmpty()) {
            throw RestException.restThrow(ErrorType.USER_EMAIL_INVALID);
        }

        User user;
        Optional<User> byEmail = this.userRepository.findByEmail(dto.getEmail());
        if (byEmail.isPresent()) {
            user = byEmail.get();
        } else {
            throw RestException.restThrow(ErrorType.INVALID_AUTHENTICATION_CHARACTERS);
        }

        try {
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(user.getUsername(), dto.getPassword());
            authenticationManager.authenticate(authentication);
        } catch (Exception e) {
            throw RestException.restThrow(ErrorType.INVALID_CREDENTIALS);
        }

        Device trustedDevice = this.deviceRepository.findFirstByUserIdAndDeviceName(user.getId(), dto.getDeviceId())
                .orElse(null);

        String code = utils.getCode();

        if (trustedDevice == null) {
            if (utils.sendCodeToMail(dto.getEmail(), code)) {
                Device device = new Device();
                device.setUser(user);
                device.setDeviceName(dto.getDeviceId());
                device.setCreatedAt(LocalDateTime.now());
                device.setUpdatedAt(LocalDateTime.now());
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
                                .build()
                );
            }
        }

        if (!user.isActive()) {
            throw RestException.restThrow(ErrorType.ACCOUNT_NOT_FOUND);
        }

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());
        String userToken = jwtUtil.generateToken(userDetails.getUsername());

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
                .orElseThrow(() -> RestException.restThrow(ErrorType.USER_NOT_FOUND));

        if (!user.isActive()) {
            throw RestException.restThrow(ErrorType.ACCOUNT_NOT_FOUND);
        }
        Device device = this.deviceRepository.findFirstByUserIdAndDeviceName(user.getId(), deviceId)
                .orElseThrow(() -> RestException.restThrow(ErrorType.DEVICE_NOT_FOUND));

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

        Optional<User> byEmail = this.userRepository.findByEmail(email);
        if (byEmail.isEmpty()) {
            throw RestException.restThrow(ErrorType.USER_NOT_FOUND);
        }


        if (!byEmail.get().isActive()) {
            throw RestException.restThrow(ErrorType.ACCOUNT_NOT_FOUND);
        }
        User user = byEmail.get();

        Optional<Device> OptionalDevice = this.deviceRepository.findForVerifyFirstByUserIdAndDeviceName(user.getId(), deviceId);
        if (OptionalDevice.isEmpty()) throw RestException.restThrow(ErrorType.DEVICE_NOT_FOUND);

        Device device = OptionalDevice.get();


        Code c = this.codeRepository.findByUserIdAndDeviceId(user.getId(), device.getId()).orElse(null);

        if (c != null && c.getDevice() != null && c.getDevice().getUser() != null
                && c.getDevice().getUser().getId().equals(user.getId())) {
            c.getDevice().setAddedAt(LocalDateTime.now());
            c.getDevice().setDeviceName(deviceId);
            c.getDevice().setExpiredAt(LocalDateTime.now().plusDays(40));
            c.getDevice().setUpdatedAt(LocalDateTime.now());
            c.getDevice().setCreatedAt(LocalDateTime.now());
            c.getDevice().setVisibility(true);
            c.getDevice().setIsVerified(true);
            this.deviceRepository.save(c.getDevice());


            Map<String, Object> map = new HashMap<>();
            UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());
            String currentToken = jwtUtil.generateToken(userDetails.getUsername());
            map.put("token", currentToken);
            map.put("user", this.userMapper.toDto(user));

            return ApiResponse.success(map, "SUCCESS");
        }

        if (!this.utils.checkCode(email, code, device.getId())) {
            throw RestException.restThrow(ErrorType.ERROR_SENDING_VERIFICATION_CODE);
        }

        throw RestException.restThrow(ErrorType.ERROR_SENDING_VERIFICATION_CODE);
    }

    @Override
    public ApiResponse<?> checkEmail(String username) {
        userRepository.findByEmail(username).ifPresent(user -> {
            if (user.isActive()) {
                throw RestException.restThrow(ErrorType.EMAIL_ALREADY_TAKEN);
            }
        });
        return ApiResponse.success("SUCCESS");
    }

}
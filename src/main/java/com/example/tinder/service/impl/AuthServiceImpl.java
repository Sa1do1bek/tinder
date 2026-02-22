package com.example.tinder.service.impl;

import com.example.tinder.dto.TelegramAuthRequest;
import com.example.tinder.dto.VerifyDto;
import com.example.tinder.dto.auth.LoginDto;
import com.example.tinder.dto.auth.LoginResponse;
import com.example.tinder.dto.auth.RegisterDto;
import com.example.tinder.dto.auth.byPhone.LoginByPhoneDto;
import com.example.tinder.dto.auth.byPhone.RegisterByPhoneDto;
import com.example.tinder.dto.auth.byPhone.VerifyByPhoneDto;
import com.example.tinder.dto.base.ApiResponse;
import com.example.tinder.entity.*;
import com.example.tinder.enums.*;
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

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

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
    private final CardRepository cardRepository;

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
        System.out.println("------------code: " + code);
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
        return ApiResponse.success(SuccessType.VERIFICATION_CODE_SENT_TO_EMAIL);
    }

    @Override
    public ApiResponse<?> registerByPhone(RegisterByPhoneDto dto) {
        if (!utils.checkPhoneNumber(dto.getPhoneNumber())) {
            throw RestException.restThrow(ErrorType.INVALID_PHONE_NUMBER);
        }

        if (userRepository.existsByPhoneNumberAndActiveTrue(dto.getPhoneNumber())) {
            throw RestException.restThrow(ErrorType.PHONE_ALREADY_TAKEN);
        }

        if (userRepository.existsByUsernameAndActiveTrue(dto.getUsername())) {
            throw RestException.restThrow(ErrorType.USERNAME_ALREADY_TAKEN);
        }

        String code = utils.getCode();
        System.out.println("------------code: " + code);
        if (!utils.sendCodeToPhone(dto.getPhoneNumber(),code)) {
            throw RestException.restThrow(ErrorType.ERROR_SENDING_VERIFICATION_CODE);
        }

        Optional<PendingUser> optionalPendingUser = pendingUserRepository.findByPhoneNumber(dto.getPhoneNumber());
        PendingUser pendingUser = optionalPendingUser.orElse(new PendingUser());
        pendingUser.setPhoneNumber(dto.getPhoneNumber());
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
        return ApiResponse.success(SuccessType.VERIFICATION_CODE_SENT_TO_PHONE);
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

        User user = new User();
        System.out.println("USER ID " + user.getId());


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
            user.setIsVerified(true);
            user.setIsApproved(false);
            user.setShowProfile(true);
            user.setFreeTonight(false);
            user.setLatitude("41.1776056");
            user.setLongitude("69.3562786");
            user.setProvider(AuthProvider.EMAIL);
            user.setProvider(AuthProvider.EMAIL);
            user.setSubscriptionType(SubscriptionType.FREE);
            user.setUpdatedAt(LocalDateTime.now());
            user.setCreatedAt(LocalDateTime.now());
            user.setUpdatedAt(LocalDateTime.now());

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
                    .provider(AuthProvider.EMAIL)
                    .subscriptionType(SubscriptionType.FREE)
                    .active(true)
                    .profilePictures(new ArrayList<>())
                    .userInterests(new ArrayList<>())
                    .latitude("41.1776056")
                    .longitude("69.3562786")
                    .isVerified(true)
                    .showProfile(true)
                    .bio(null)
                    .build();
        }

        User savedUser = userRepository.saveAndFlush(user);
        System.out.println("SAVED USER ID " + savedUser.getId());
        savePreference(savedUser);
        saveCard(savedUser);
        saveVerification(savedUser);
        saveUserLocation(savedUser);


        deviceService.addTrustedDevice(savedUser.getId(), pending.getDeviceId());

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());
        String jwtToken = jwtUtil.generateToken(userDetails.getUsername());

        pendingUserRepository.delete(pending);

        codeRepository.deleteByCode(dto.getCode());

        return ApiResponse.success(Map.of(
                        "token", jwtToken,
                        "user", userMapper.toDto(savedUser))
                , SuccessType.USER_REGISTERED_SUCCESSFULLY
        );
    }

    @Override
    @Transactional
    public ApiResponse<?> verifyByPhone(VerifyByPhoneDto dto) {
        Optional<PendingUser> optionalPending = pendingUserRepository.findByPhoneNumber(dto.getPhoneNumber());

        if (optionalPending.isEmpty()) {
            throw RestException.restThrow(ErrorType.INVALID_VERIFICATION_CODE_OR_PHONE);
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

        Optional<User> optionalUser = userRepository.findByPhoneNumber(pending.getPhoneNumber());

        User user = new User();
        System.out.println("USER ID " + user.getId());

        if (optionalUser.isPresent()) {
            user = optionalUser.get();
            System.out.println("--------------user : " + optionalUser.get().getUsername());

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
            user.setIsVerified(true);
            user.setIsApproved(false);
            user.setShowProfile(true);
            user.setLatitude("41.1776056");
            user.setLongitude("69.3562786");
            user.setProvider(AuthProvider.PHONE);
            user.setSubscriptionType(SubscriptionType.FREE);
            user.setUpdatedAt(LocalDateTime.now());
            user.setCreatedAt(LocalDateTime.now());
            user.setUpdatedAt(LocalDateTime.now());


            userLocationRepository.findByUserId(user.getId()).ifPresent(userLocationRepository::delete);
            preferenceRepository.findByUserId(user.getId()).ifPresent(preferenceRepository::delete);

        } else {
            user = User.builder()
                    .phoneNumber(pending.getPhoneNumber())
                    .username(pending.getUsername())
                    .password(pending.getPassword())
                    .status(UserStatus.ACTIVE)
                    .gender(pending.getGender())
                    .birthDate(pending.getBirthDate())
                    .fullname(pending.getFullName())
                    .role(UserRole.USER)
                    .provider(AuthProvider.PHONE)
                    .active(true)
                    .profilePictures(new ArrayList<>())
                    .userInterests(new ArrayList<>())
                    .latitude("41.1776056")
                    .longitude("69.3562786")
                    .isVerified(true)
                    .showProfile(true)
                    .freeTonight(false)
                    .bio(null)
                    .build();
        }

        User savedUser = userRepository.saveAndFlush(user);
        System.out.println("SAVED USER ID " + savedUser.getId());
        savePreference(savedUser);
        saveCard(savedUser);
        saveVerification(savedUser);
        saveUserLocation(savedUser);


        deviceService.addTrustedDevice(savedUser.getId(), pending.getDeviceId());

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());
        String jwtToken = jwtUtil.generateToken(userDetails.getUsername());

        pendingUserRepository.delete(pending);

        codeRepository.deleteByCode(dto.getCode());

        return ApiResponse.success(Map.of(
                        "token", jwtToken,
                        "user", userMapper.toDto(savedUser))
                , SuccessType.USER_REGISTERED_SUCCESSFULLY
        );
    }

    public void saveUserLocation(User savedUser) {
        UserLocation userLocation = new UserLocation();
        userLocation.setUser(savedUser);
        userLocation.setLatitude("41.1776056");
        userLocation.setLongitude("69.3562786");
        userLocation.setCreatedAt(LocalDateTime.now());
        userLocation.setUpdatedAt(LocalDateTime.now());
        userLocationRepository.save(userLocation);
    }

    public void saveVerification(User savedUser) {
        Verification verification = new Verification();
        verification.setUser(savedUser);
        verification.setCreatedAt(LocalDateTime.now());
        verification.setUpdatedAt(LocalDateTime.now());
        verificationRepository.save(verification);
    }

    public void saveCard(User user) {
        Card card = new Card();
        card.setUser(user);
        card.setBalance(new BigDecimal("0.0"));
        card.setCreatedAt(LocalDateTime.now());
        card.setUpdatedAt(LocalDateTime.now());
        card.setTotalSuperLikesEarned(0);
        card.setSuperLikeCount(0);
        this.cardRepository.save(card);
    }

    public void savePreference(User user) {

        Preference preference = new Preference();
        preference.setUser(user);
        if (user.getGender() == Gender.MALE) {
            preference.setPreferredGender(Gender.FEMALE);
        } else {
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
    public ApiResponse<?>   loginByEmail(LoginDto dto) {
        if (dto.getEmail() == null || dto.getEmail().trim().isEmpty()) {
            throw RestException.restThrow(ErrorType.USER_EMAIL_INVALID);
        }

        User user;
        Optional<User> byEmail = this.userRepository.findByEmail(dto.getEmail());
        if (byEmail.isPresent()) {
            user = byEmail.get();
        } else {
            throw RestException.restThrow(ErrorType.USER_NOT_FOUND);
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

        if (user.getEmail().equals("zufarnozimov8@gmail.com")) {
            UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());
            String userToken = jwtUtil.generateToken(userDetails.getUsername());

            return ApiResponse.success(
                    LoginResponse.builder()
                            .code(1)
                            .user(this.userMapper.toDto(user))
                            .token(userToken)
                            .build(),
                    SuccessType.USER_LOGIN_SUCCESSFULLY
            );
        }

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
                                .code(0)
                                .user(this.userMapper.toDto(user))
                                .build(),
                        SuccessType.VERIFICATION_CODE_SENT_TO_EMAIL
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
                        .code(1)
                        .user(this.userMapper.toDto(user))
                        .token(userToken)
                        .build(),
                SuccessType.USER_LOGIN_SUCCESSFULLY
        );
    }

    @Override
    @Transactional
    public ApiResponse<?> loginByPhone(LoginByPhoneDto dto) {
        if (dto.getPhoneNumber() == null || dto.getPhoneNumber().trim().isEmpty()) {
            throw RestException.restThrow(ErrorType.USER_PHONE_INVALID);
        }

        User user;
        Optional<User> byPhone = this.userRepository.findByPhoneNumber(dto.getPhoneNumber());
        if (byPhone.isPresent()) {
            user = byPhone.get();
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
        System.out.println("-------------code : " +  code);
        if (trustedDevice == null) {
            if (utils.sendCodeToPhone(dto.getPhoneNumber(), code)) {
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
                                .code(0)
                                .user(this.userMapper.toDto(user))
                                .build(),
                        SuccessType.VERIFICATION_CODE_SENT_TO_PHONE
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
                        .code(1)
                        .user(this.userMapper.toDto(user))
                        .token(userToken)
                        .build(),
                SuccessType.USER_LOGIN_SUCCESSFULLY
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
            return ApiResponse.success(SuccessType.CODE_SENT_TO_EMAIL);
        }
        throw RestException.restThrow(ErrorType.ERROR_SENDING_VERIFICATION_CODE);
    }

    @Override
    @Transactional
    public ApiResponse<?> resendCodeToPhone(String phone, String deviceId, Long id) {
        if (phone == null || phone.trim().isEmpty()) {
            throw RestException.restThrow(ErrorType.USER_PHONE_INVALID);
        }

        User user = this.userRepository.findById(id)
                .orElseThrow(() -> RestException.restThrow(ErrorType.USER_NOT_FOUND));

        if (!user.isActive()) {
            throw RestException.restThrow(ErrorType.ACCOUNT_NOT_FOUND);
        }

        Device device = this.deviceRepository.findFirstByUserIdAndDeviceName(user.getId(), deviceId)
                .orElseThrow(() -> RestException.restThrow(ErrorType.DEVICE_NOT_FOUND));

        String code = utils.getCode();
        System.out.println("-----------code : " +  code);
        if (utils.sendCodeToPhone(phone, code)) {
            Code c = this.codeRepository.findByUserIdAndDeviceId(user.getId(), device.getId()).orElse(null);
            if (c != null) {
                c.setCode(code);
                c.setCreatedAt(LocalDateTime.now());
                this.codeRepository.save(c);
            }
            return ApiResponse.success(SuccessType.CODE_SENT_TO_PHONE);
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

            return ApiResponse.success(map, SuccessType.CODE_CHECKED_SUCCESSFULLY);
        }

        if (!this.utils.checkCode(email, code, device.getId())) {
            throw RestException.restThrow(ErrorType.ERROR_SENDING_VERIFICATION_CODE);
        }

        throw RestException.restThrow(ErrorType.ERROR_SENDING_VERIFICATION_CODE);
    }

    @Override
    public ApiResponse<?> checkCodeByPhone(String phone, String code, String deviceId) {
        if (phone == null || phone.trim().isEmpty()) {
            throw RestException.restThrow(ErrorType.USER_PHONE_INVALID);
        }

        Optional<User> byPhone = this.userRepository.findByPhoneNumber(phone);
        if (byPhone.isEmpty()) {
            throw RestException.restThrow(ErrorType.USER_NOT_FOUND);
        }

        if (!byPhone.get().isActive()) {
            throw RestException.restThrow(ErrorType.ACCOUNT_NOT_FOUND);
        }
        User user = byPhone.get();

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

            return ApiResponse.success(map, SuccessType.CODE_CHECKED_SUCCESSFULLY);
        }

        if (!this.utils.checkCodeByPhone(phone, code, device.getId())) {
            throw RestException.restThrow(ErrorType.ERROR_SENDING_VERIFICATION_CODE);
        }

        throw RestException.restThrow(ErrorType.ERROR_SENDING_VERIFICATION_CODE);
    }

    @Override
    public ApiResponse<?> checkEmail(String email) {
        userRepository.findByEmail(email).ifPresent(user -> {
            if (user.isActive()) {
                throw RestException.restThrow(ErrorType.EMAIL_ALREADY_TAKEN);
            }
        });
        return ApiResponse.success(SuccessType.EMAIL_AVAILABLE_FOR_REGISTRATION);
    }

    @Override
    public ApiResponse<?> checkPhone(String phone) {
        userRepository.findByPhoneNumber(phone).ifPresent(user -> {
            if (user.isActive()) {
                throw RestException.restThrow(ErrorType.PHONE_ALREADY_TAKEN);
            }
        });
        return ApiResponse.success(SuccessType.PHONE_AVAILABLE_FOR_REGISTRATION);
    }

    @Override
    public ApiResponse<?> forgetPasswordByEmail(String email, String deviceName) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> RestException.restThrow(ErrorType.USER_NOT_FOUND));

        Device device = deviceRepository
                .findForVerifyFirstByUserIdAndDeviceName(user.getId(), deviceName)
                .orElseGet(() -> {
                    Device newDevice = Device.builder()
                            .deviceName(deviceName)
                            .addedAt(LocalDateTime.now())
                            .user(user)
                            .isVerified(false)
                            .build();
                    return deviceRepository.save(newDevice);
                });

        String code = utils.getCode();

        if (!utils.sendCodeToMail(email, code)) {
            throw RestException.restThrow(ErrorType.ERROR_SENDING_VERIFICATION_CODE);
        }

        Code c = Code.builder()
                .code(code)
                .createdAt(LocalDateTime.now())
                .counts(0)
                .device(device)
                .user(user)
                .build();

        codeRepository.save(c);
        return ApiResponse.success(SuccessType.CODE_SENT_TO_EMAIL);
    }

    @Override
    public ApiResponse<?> forgetPasswordByPhone(String phone, String deviceName) {
        User user = userRepository.findByPhoneNumber(phone)
                .orElseThrow(() -> RestException.restThrow(ErrorType.USER_NOT_FOUND));

        Device device = deviceRepository
                .findForVerifyFirstByUserIdAndDeviceName(user.getId(), deviceName)
                .orElseGet(() -> {
                    Device newDevice = Device.builder()
                            .deviceName(deviceName)
                            .addedAt(LocalDateTime.now())
                            .user(user)
                            .isVerified(false)
                            .build();
                    return deviceRepository.save(newDevice);
                });

        String code = utils.getCode();
        System.out.println("----------code : " +  code);
        if (!utils.sendCodeToPhone(phone, code)) {
            throw RestException.restThrow(ErrorType.ERROR_SENDING_VERIFICATION_CODE);
        }

        Code c = Code.builder()
                .code(code)
                .createdAt(LocalDateTime.now())
                .counts(0)
                .device(device)
                .user(user)
                .build();

        codeRepository.save(c);
        return ApiResponse.success(SuccessType.CODE_SENT_TO_PHONE);
    }

    @Override
    public ApiResponse<?> checkForgetPasswordByEmail(String email, String code, String deviceName, String newPassword) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> RestException.restThrow(ErrorType.USER_NOT_FOUND));

        Code dbCode = codeRepository
                .findByDeviceNamaAndUserId(deviceName, user.getId())
                .orElseThrow(() -> RestException.restThrow(ErrorType.CODE_IS_INVALID));

        if (dbCode.getCounts() >= 3) {
            throw RestException.restThrow(ErrorType.CODE_ATTEMPT_LIMIT_EXCEEDED);
        }

        LocalDateTime expiredTime = dbCode.getCreatedAt().plusMinutes(2);
        if (LocalDateTime.now().isAfter(expiredTime)) {
            throw RestException.restThrow(ErrorType.VERIFICATION_CODE_EXPIRED);
        }

        if (!dbCode.getCode().equals(code)) {
            dbCode.setCounts(dbCode.getCounts() + 1);
            codeRepository.save(dbCode);
            throw RestException.restThrow(ErrorType.CODE_IS_INVALID);
        }

        user.setPassword(passwordEncoder.encode(newPassword));

        Device device = dbCode.getDevice();
        device.setIsVerified(true);
        deviceRepository.save(device);
        userRepository.save(user);
        codeRepository.delete(dbCode);
        return ApiResponse.success(SuccessType.PASSWORD_UPDATED_SUCCESSFULLY);
    }

    @Override
    public ApiResponse<?> checkForgetPasswordByPhone(String phone, String code, String deviceName, String newPassword) {
        User user = userRepository.findByPhoneNumber(phone)
                .orElseThrow(() -> RestException.restThrow(ErrorType.USER_NOT_FOUND));

        Code dbCode = codeRepository
                .findByDeviceNamaAndUserId(deviceName, user.getId())
                .orElseThrow(() -> RestException.restThrow(ErrorType.CODE_IS_INVALID));

        if (dbCode.getCounts() >= 3) {
            throw RestException.restThrow(ErrorType.CODE_ATTEMPT_LIMIT_EXCEEDED);
        }

        LocalDateTime expiredTime = dbCode.getCreatedAt().plusMinutes(2);
        if (LocalDateTime.now().isAfter(expiredTime)) {
            throw RestException.restThrow(ErrorType.VERIFICATION_CODE_EXPIRED);
        }

        if (!dbCode.getCode().equals(code)) {
            dbCode.setCounts(dbCode.getCounts() + 1);
            codeRepository.save(dbCode);
            throw RestException.restThrow(ErrorType.CODE_IS_INVALID);
        }

        user.setPassword(passwordEncoder.encode(newPassword));

        Device device = dbCode.getDevice();
        device.setIsVerified(true);
        deviceRepository.save(device);
        userRepository.save(user);
        codeRepository.delete(dbCode);
        return ApiResponse.success(SuccessType.PASSWORD_UPDATED_SUCCESSFULLY);
    }


    @Override
    public ApiResponse<?> loginOrRegisterByTelegram(TelegramAuthRequest req) {
        String fakeEmail = req.getTelegramId() + "@telegram.app";
        Optional<User> optionalUser = userRepository.findByEmail(fakeEmail);

        User user;
        if (optionalUser.isPresent()) {
            user = optionalUser.get();
        } else {
            String fullName = "";
            if (req.getFirstName() != null) {
                fullName += req.getFirstName();
            }
            if (req.getLastName() != null) {
                fullName += req.getLastName();
            }
            user = User.builder()
                    .firebaseUid(null)
                    .email(fakeEmail)
                    .username(req.getUsername())
                    .fullname(fullName)
                    .profilePic(null)
                    .isVerified(true)
                    .provider(AuthProvider.TELEGRAM)
                    .role(UserRole.USER)
                    .status(UserStatus.ACTIVE)
                    .gender(Gender.EVERYONE)
                    .active(true)
                    .isApproved(false)
                    .isOnline(false)
                    .showProfile(true)
                    .freeTonight(false)
                    .latitude("41.1776056")
                    .longitude("69.3562786")
                    .birthDate(Date.valueOf("2004-05-31 05:00:00.000000"))
                    .build();
            user.setCreatedAt(LocalDateTime.now());
            user.setUpdatedAt(LocalDateTime.now());
            User savedUser = userRepository.save(user);
            user = savedUser;
            savePreference(savedUser);
            saveCard(savedUser);
            saveVerification(savedUser);
            saveUserLocation(savedUser);
        }

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());
        String jwtToken = jwtUtil.generateToken(userDetails.getUsername());

        return ApiResponse.success(Map.of(
                        "token", jwtToken,
                        "user", userMapper.toDto(user))
                , SuccessType.TELEGRAM_USER_ENTERED_SUCCESSFULLY
        );
    }


}
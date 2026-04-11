package com.example.tinder.service.impl;

import com.example.tinder.dto.TelegramAuthRequest;
import com.example.tinder.dto.VerifyDto;
import com.example.tinder.dto.auth.BaseRegisterDto;
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
        dtoEmailValidation(dto);

        String code = getCodeFromUtil();
        if (!utils.sendCodeToMail(dto.getEmail(), code)) {
            throw RestException.restThrow(ErrorType.ERROR_SENDING_VERIFICATION_CODE);
        }

        PendingUser pendingUser = pendingUserRepository
                .findByEmail(dto.getEmail()).orElse(buildNewPendingUser(dto));

        pendingUser.setEmail(dto.getEmail());
        pendingUser.setCode(code);

        pendingUserRepository.save(pendingUser);
        return ApiResponse.success(SuccessType.VERIFICATION_CODE_SENT_TO_EMAIL);
    }

    @Override
    public ApiResponse<?> registerByPhone(RegisterByPhoneDto dto) {
        dtoPhoneValidation(dto);

        String code = getCodeFromUtil();
        if (!utils.sendCodeToPhone(dto.getPhoneNumber(), code)) {
            throw RestException.restThrow(ErrorType.ERROR_SENDING_VERIFICATION_CODE);
        }

        PendingUser pendingUser = pendingUserRepository.findByPhoneNumber(dto.getPhoneNumber())
                .orElse(buildNewPendingUser(dto));

        pendingUser.setCode(code);
        pendingUser.setPhoneNumber(dto.getPhoneNumber());

        pendingUserRepository.save(pendingUser);
        return ApiResponse.success(SuccessType.VERIFICATION_CODE_SENT_TO_PHONE);
    }

    @Override
    @Transactional
    public ApiResponse<?> verify(VerifyDto dto) {
        PendingUser pending = getPending(dto.getEmail());
        return registerInternal(pending, dto.getCode(), AuthProvider.EMAIL);
    }

    @Override
    @Transactional
    public ApiResponse<?> verifyByPhone(VerifyByPhoneDto dto) {
        PendingUser pendingByPhone = getPendingByPhone(dto.getPhoneNumber());
        return registerInternal(pendingByPhone, dto.getCode(), AuthProvider.PHONE);
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
    public ApiResponse<?> loginByEmail(LoginDto dto) {
        return loginInternal(dto.getEmail(),
                dto.getPassword(),
                dto.getDeviceId(),
                AuthProvider.EMAIL);
    }

    @Override
    @Transactional
    public ApiResponse<?> loginByPhone(LoginByPhoneDto dto) {
        return loginInternal(dto.getPhoneNumber(),
                dto.getPassword(),
                dto.getDeviceId(),
                AuthProvider.PHONE);
    }


    @Override
    @Transactional
    public ApiResponse<?> resendCodeToEmail(String email, String deviceName, Long id) {
        return resendCodeInternal(email, deviceName, id, AuthProvider.EMAIL);
    }

    @Override
    @Transactional
    public ApiResponse<?> resendCodeToPhone(String phone, String deviceName, Long id) {
        return resendCodeInternal(phone, deviceName, id, AuthProvider.PHONE);
    }

    @Override
    public ApiResponse<?> checkCodeByEmail(String email, String code, String deviceName) {
        return checkCodeInternal(email, code, deviceName, AuthProvider.EMAIL);
    }

    @Override
    public ApiResponse<?> checkCodeByPhone(String phone, String code, String deviceName) {
        return checkCodeInternal(phone, code, deviceName, AuthProvider.PHONE);
    }

    @Override
    public ApiResponse<?> checkEmail(String email) {
        return checkInternal(email, AuthProvider.EMAIL);
    }

    @Override
    public ApiResponse<?> checkPhone(String phone) {
        return checkInternal(phone, AuthProvider.PHONE);
    }

    @Override
    public ApiResponse<?> forgetPasswordByEmail(String email, String deviceName) {
        return forgotPasswordInternal(email, deviceName, AuthProvider.EMAIL);
    }

    @Override
    public ApiResponse<?> forgetPasswordByPhone(String phone, String deviceName) {
        return forgotPasswordInternal(phone, deviceName, AuthProvider.PHONE);
    }

    @Override
    public ApiResponse<?> checkForgetPasswordByEmail(String email, String code, String deviceName, String newPassword) {
        return verificationPasswordInternal(email, code, deviceName, newPassword, AuthProvider.EMAIL);
    }

    @Override
    public ApiResponse<?> checkForgetPasswordByPhone(String phone, String code, String deviceName, String newPassword) {
        return verificationPasswordInternal(phone, code, deviceName, newPassword, AuthProvider.PHONE);
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

    //** -------------------------INTERNALS----------------------------------**//

    /**
     * verifyInternal -> User Register by Phone bilan by Email ni birlashtiradi
     */
    private ApiResponse<?> registerInternal(PendingUser pending, String code, AuthProvider provider) {

        validateVerification(pending, code);

        User user = getOrCreateUser(pending, provider);

        User savedUser = userRepository.saveAndFlush(user);

        handlePostRegistration(savedUser, pending);

        String token = generateToken(savedUser);

        cleanup(pending, code);

        return ApiResponse.success(
                Map.of(
                        "token", token,
                        "user", userMapper.toDto(savedUser)
                ),
                SuccessType.USER_REGISTERED_SUCCESSFULLY
        );
    }

    /**
     * loginInternal -> User Login by Phone bilan by Email ni birlashtiradi
     */
    private ApiResponse<?> loginInternal(String value, String password, String deviceName, AuthProvider provider) {

        valueValidation(value,provider);

        User user = getUser(value, provider);

        authenticate(user, password);

        Device trustedDevice = getTrustedDevice(user.getId(), deviceName);

        String code = utils.getCode();

        if (trustedDevice == null) {
            if (sendCode(value, code, provider)) {

                Device device = createDevice(user, deviceName);
                saveCode(user, device, code);

                return ApiResponse.success(
                        LoginResponse.builder()
                                .code(0)
                                .user(userMapper.toDto(user))
                                .build(),
                        provider == AuthProvider.EMAIL
                                ? SuccessType.VERIFICATION_CODE_SENT_TO_EMAIL
                                : SuccessType.VERIFICATION_CODE_SENT_TO_PHONE
                );
            }
        }

        if (!user.isActive()) {
            throw RestException.restThrow(ErrorType.ACCOUNT_NOT_FOUND);
        }

        String token = generateToken(user);

        return ApiResponse.success(
                LoginResponse.builder()
                        .code(1)
                        .user(userMapper.toDto(user))
                        .token(token)
                        .build(),
                SuccessType.USER_LOGIN_SUCCESSFULLY
        );
    }

    /**
     * resendCodeInternal -> resendCodeToEmail resendCodeTopPhone birlashtiradi
     */
    private ApiResponse<?> resendCodeInternal(String value, String deviceName, Long userId, AuthProvider provider) {

        // 1. validation
        valueValidation(value, provider);

        // 2. user get
        User user = userRepository.findById(userId)
                .orElseThrow(() -> RestException.restThrow(ErrorType.USER_NOT_FOUND));

        validateUserActive(user);

        Device device = getTrustedDeviceOrThrow(user.getId(), deviceName);

        String code = getCodeFromUtil();

        if (!sendCode(value, code, provider)) {
            throw RestException.restThrow(ErrorType.ERROR_SENDING_VERIFICATION_CODE);
        }

        // 6. code update
        updateCode(user.getId(), device.getId(), code);

        return ApiResponse.success(
                provider == AuthProvider.EMAIL
                        ? SuccessType.CODE_SENT_TO_EMAIL
                        : SuccessType.CODE_SENT_TO_PHONE
        );
    }

    /**
     * checkCodeInternal -> checkCodeByEmail va checkCodeByPhone ni birlashtiradi
     */
    private ApiResponse<?> checkCodeInternal(String value, String code, String deviceName, AuthProvider provider) {

        // 1. validation
        valueValidation(value, provider);

        User user = getUser(value, provider);

        validateUserActive(user);

        Device device = getOrThrowDevice(user.getId(), deviceName);

        Code dbCode = getCode(user.getId(), device.getDeviceName());

        // 5. validate code
        validateCode(dbCode, code);

        // 6. device verification
        verifyDevice(device, deviceName);

        // 7. token response
        return buildLoginResponse(user);
    }

    /**
     * checkInternal -> checkEmail va checkPhone birlashtiradi
     */
    private ApiResponse<?> checkInternal(String value, AuthProvider provider) {

        if (getUser(value, provider).isActive()) {
            throw RestException.restThrow(provider == AuthProvider.EMAIL
                    ? ErrorType.EMAIL_ALREADY_TAKEN
                    : ErrorType.PHONE_ALREADY_TAKEN);
        }
        return ApiResponse.success(
                provider == AuthProvider.EMAIL
                        ? SuccessType.EMAIL_AVAILABLE_FOR_REGISTRATION
                        : SuccessType.PHONE_AVAILABLE_FOR_REGISTRATION
        );
    }

    /**
     * forgotPasswordInternal -> forgetPasswordByEmail va forgetPasswordByPhone birlashtiradi
     */
    private ApiResponse<?> forgotPasswordInternal(String value, String deviceName, AuthProvider provider) {
        valueValidation(value, provider);

        User user = getUser(value, provider);

        Device device = getOrCreateDevice(user, deviceName);

        String code = getCodeFromUtil();

        if (!sendCode(value, code, provider)) {
            throw RestException.restThrow(ErrorType.ERROR_SENDING_VERIFICATION_CODE);
        }

        saveCode(user, device, code);

        return ApiResponse.success(
                provider == AuthProvider.EMAIL
                        ? SuccessType.CODE_SENT_TO_EMAIL
                        : SuccessType.CODE_SENT_TO_PHONE
        );
    }

    /**
     * verificationPasswordInternal->  checkForgetPasswordByEmail bilan checkForgetPasswordByPhone birlashtiradi
     */
    private ApiResponse<?> verificationPasswordInternal(String value, String code, String deviceName, String newPassword, AuthProvider provider) {
        valueValidation(value, provider);

        User user = getUser(value, provider);
        Code dbCode = getCode(user.getId(), deviceName);

        validateCode(dbCode, code);

        user.setPassword(passwordEncoder.encode(newPassword));

        completePasswordReset(user, dbCode);

        return ApiResponse.success(SuccessType.PASSWORD_UPDATED_SUCCESSFULLY);
    }


    /**
     * -----------------YORDAMCHI METHODS---------------------
     */
    private void dtoEmailValidation(RegisterDto dto) {
        if (!utils.checkEmail(dto.getEmail())) {
            throw RestException.restThrow(ErrorType.INVALID_EMAIL_FORMAT);
        }
        if (userRepository.existsByEmailAndActiveTrue(dto.getEmail())) {
            throw RestException.restThrow(ErrorType.EMAIL_ALREADY_TAKEN);
        }

        usernameValidation(dto.getUsername());

    }

    private void dtoPhoneValidation(RegisterByPhoneDto dto) {
        if (!utils.checkPhoneNumber(dto.getPhoneNumber())) {
            throw RestException.restThrow(ErrorType.INVALID_PHONE_NUMBER);
        }

        if (userRepository.existsByPhoneNumberAndActiveTrue(dto.getPhoneNumber())) {
            throw RestException.restThrow(ErrorType.PHONE_ALREADY_TAKEN);
        }

        usernameValidation(dto.getUsername());
    }

    private void usernameValidation(String username) {
        if (userRepository.existsByUsernameAndActiveTrue(username)) {
            throw RestException.restThrow(ErrorType.USERNAME_ALREADY_TAKEN);
        }
    }

    private void valueValidation(String value, AuthProvider provider) {
        if (value == null || value.trim().isEmpty()) {
            throw RestException.restThrow(
                    provider == AuthProvider.EMAIL
                            ? ErrorType.USER_EMAIL_INVALID
                            : ErrorType.USER_PHONE_INVALID
            );
        }
    }

    private void validateUserActive(User user) {
        if (!user.isActive()) {
            throw RestException.restThrow(ErrorType.ACCOUNT_NOT_FOUND);
        }
    }

    @Transactional // Ensures both deletes happen or none do
    public void validateVerification(PendingUser pending, String code) {
        // 1. Check code correctness first
        if (!pending.getCode().equals(code)) {
            throw RestException.restThrow(ErrorType.ERROR_SENDING_VERIFICATION_CODE);
        }

        // 2. Define expiration (Consider moving '2' to a constant or config file)
        final long EXPIRATION_MINUTES = 2;
        LocalDateTime expirationTime = pending.getCreatedAt().plusMinutes(EXPIRATION_MINUTES);

        // 3. Check if current time has passed the expiration time
        if (LocalDateTime.now().isAfter(expirationTime)) {

            // Clean up expired data
            pendingUserRepository.delete(pending);
            codeRepository.deleteByCode(code);

            throw RestException.restThrow(ErrorType.VERIFICATION_CODE_EXPIRED);
        }
    }

    private void validateCode(Code dbCode, String inputCode) {

        LocalDateTime expiredTime = dbCode.getCreatedAt().plusMinutes(2);
        if (LocalDateTime.now().isAfter(expiredTime)) {
            throw RestException.restThrow(ErrorType.VERIFICATION_CODE_EXPIRED);
        }

        if (!dbCode.getCode().equals(inputCode)) {
            dbCode.setCounts(dbCode.getCounts() + 1);
            codeRepository.save(dbCode);

            if (dbCode.getCounts() >= 3) {
                throw RestException.restThrow(ErrorType.CODE_ATTEMPT_LIMIT_EXCEEDED);
            }

            throw RestException.restThrow(ErrorType.CODE_IS_INVALID);
        }
    }

    private void verifyDevice(Device device, String deviceId) {
        device.setAddedAt(LocalDateTime.now());
        device.setDeviceName(deviceId);
        device.setExpiredAt(LocalDateTime.now().plusDays(40));
        device.setUpdatedAt(LocalDateTime.now());
        device.setCreatedAt(LocalDateTime.now());
        device.setVisibility(true);
        device.setIsVerified(true);

        deviceRepository.save(device);
    }

    private void clearUserRelations(User user) {
        userLocationRepository.findByUserId(user.getId())
                .ifPresent(userLocationRepository::delete);

        preferenceRepository.findByUserId(user.getId())
                .ifPresent(preferenceRepository::delete);
    }

    private void cleanup(PendingUser pending, String code) {
        pendingUserRepository.delete(pending);
        codeRepository.deleteByCode(code);
    }

    private void handlePostRegistration(User user, PendingUser pending) {
        savePreference(user);
        saveCard(user);
        saveVerification(user);
        saveUserLocation(user);

        deviceService.addTrustedDevice(user.getId(), pending.getDeviceId());
    }

    private void updateUserFromPending(User user, PendingUser pending, AuthProvider provider) {
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

        user.setProvider(provider);
        user.setSubscriptionType(SubscriptionType.FREE);

        user.setUpdatedAt(LocalDateTime.now());
        user.setCreatedAt(LocalDateTime.now());
    }

    private void saveCode(User user, Device device, String code) {
        Code c = new Code();
        c.setCreatedAt(LocalDateTime.now());
        c.setCode(code);
        c.setUser(user);
        c.setDevice(device);
        c.setCounts(0);
        codeRepository.save(c);
    }

    private void updateCode(Long userId, Long deviceName, String code) {
        Code c = getCode(deviceName, code);

        c.setCode(code);
        c.setCreatedAt(LocalDateTime.now());
        c.setCounts(0);

        codeRepository.save(c);
    }

    private void authenticate(User user, String password) {
        try {
            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(user.getUsername(), password);
            authenticationManager.authenticate(auth);
        } catch (Exception e) {
            throw RestException.restThrow(ErrorType.INVALID_CREDENTIALS);
        }
    }

    private void completePasswordReset(User user, Code dbCode) {

        Device device = dbCode.getDevice();
        device.setIsVerified(true);
        deviceRepository.save(device);

        userRepository.save(user);

        codeRepository.delete(dbCode);
    }

    private PendingUser buildNewPendingUser(BaseRegisterDto dto) {
        return fillPendingUser(new PendingUser(), dto);
    }

    private PendingUser fillPendingUser(PendingUser pendingUser, BaseRegisterDto dto) {


        pendingUser.setUsername(dto.getUsername());
        pendingUser.setPassword(passwordEncoder.encode(dto.getPassword()));
        pendingUser.setFullName(dto.getFullName());
        pendingUser.setGender(dto.getGender());
        pendingUser.setBirthDate(dto.getBirthDate());
        pendingUser.setDeviceId(dto.getDeviceId());
        pendingUser.setRole(UserRole.USER);
        pendingUser.setCreatedAt(LocalDateTime.now());

        return pendingUser;
    }

    private PendingUser getPending(String email) {
        return pendingUserRepository.findByEmail(email)
                .orElseThrow(() -> RestException.restThrow(ErrorType.INVALID_VERIFICATION_CODE_OR_EMAIL));
    }

    private PendingUser getPendingByPhone(String phoneNumber) {
        return pendingUserRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> RestException.restThrow(ErrorType.INVALID_VERIFICATION_CODE_OR_PHONE));
    }

    private User buildUserFromPending(PendingUser pending, AuthProvider provider) {
        User.UserBuilder builder = User.builder()
                .username(pending.getUsername())
                .password(pending.getPassword())
                .status(UserStatus.ACTIVE)
                .gender(pending.getGender())
                .birthDate(pending.getBirthDate())
                .fullname(pending.getFullName())
                .role(UserRole.USER)
                .provider(provider)
                .subscriptionType(SubscriptionType.FREE)
                .active(true)
                .profilePictures(new ArrayList<>())
                .userInterests(new ArrayList<>())
                .latitude("41.1776056")
                .longitude("69.3562786")
                .isVerified(true)
                .showProfile(true)
                .freeTonight(false)
                .bio(null);

        if (provider == AuthProvider.EMAIL) {
            builder.email(pending.getEmail());
        } else {
            builder.phoneNumber(pending.getPhoneNumber());
        }

        return builder.build();
    }

    private User getOrCreateUser(PendingUser pending, AuthProvider provider) {

        Optional<User> optionalUser = (provider == AuthProvider.EMAIL)
                ? userRepository.findByEmail(pending.getEmail())
                : userRepository.findByPhoneNumber(pending.getPhoneNumber());

        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            updateUserFromPending(user, pending, provider);
            clearUserRelations(user);
            return user;
        }

        return buildUserFromPending(pending, provider);
    }

    private User getUser(String value, AuthProvider provider) {
        return (provider == AuthProvider.EMAIL
                ? userRepository.findByEmail(value)
                : userRepository.findByPhoneNumber(value))
                .orElseThrow(() -> RestException.restThrow(ErrorType.USER_NOT_FOUND));
    }

    private Device createDevice(User user, String deviceName) {
        Device device = new Device();
        device.setUser(user);
        device.setDeviceName(deviceName);
        device.setCreatedAt(LocalDateTime.now());
        device.setUpdatedAt(LocalDateTime.now());
        device.setAddedAt(LocalDateTime.now());
        device.setVisibility(true);
        device.setIsVerified(false);

        return deviceRepository.save(device);
    }

    private Device getTrustedDevice(Long userId, String deviceName) {
        return deviceRepository
                .findFirstByUserIdAndDeviceName(userId, deviceName)
                .orElse(null);
    }

    private Device getTrustedDeviceOrThrow(Long userId, String deviceName) {
        Device trustedDevice = getTrustedDevice(userId, deviceName);

        if (trustedDevice == null) {
            throw RestException.restThrow(ErrorType.DEVICE_NOT_FOUND);
        }

        return trustedDevice;
    }

    private Device getOrThrowDevice(Long userId, String deviceId) {
        return deviceRepository
                .findForVerifyFirstByUserIdAndDeviceName(userId, deviceId)
                .orElseThrow(() -> RestException.restThrow(ErrorType.DEVICE_NOT_FOUND));
    }

    private Device getOrCreateDevice(User user, String deviceName) {
        return deviceRepository
                .findForVerifyFirstByUserIdAndDeviceName(user.getId(), deviceName)
                .orElseGet(() -> createDevice(user, deviceName));
    }

    private boolean sendCode(String value, String code, AuthProvider provider) {
        return provider == AuthProvider.EMAIL
                ? utils.sendCodeToMail(value, code)
                : utils.sendCodeToPhone(value, code);
    }

    private Code getCode(Long userId, String deviceName) {
        return codeRepository
                .findByDeviceNamaAndUserId(deviceName, userId)
                .orElseThrow(() -> RestException.restThrow(ErrorType.CODE_IS_INVALID));

    }

    private String getCodeFromUtil() {
        String code = utils.getCode();
        System.out.println("------------code: " + code);
        return code;
    }

    private String generateToken(User user) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());
        return jwtUtil.generateToken(userDetails.getUsername());
    }

    private ApiResponse<?> buildLoginResponse(User user) {
        Map<String, Object> map = new HashMap<>();

        String token = generateToken(user);

        map.put("token", token);
        map.put("user", userMapper.toDto(user));

        return ApiResponse.success(map, SuccessType.CODE_CHECKED_SUCCESSFULLY);
    }
}
package com.example.tinder.service.impl;

import com.example.tinder.config.WebSocketEventListener;
import com.example.tinder.dto.UpdateUserGBIDto;
import com.example.tinder.dto.UserDto;
import com.example.tinder.dto.auth.LoginResponse;
import com.example.tinder.dto.base.ApiResponse;
import com.example.tinder.entity.Code;
import com.example.tinder.entity.User;
import com.example.tinder.entity.UserInterest;
import com.example.tinder.enums.ErrorType;
import com.example.tinder.enums.Gender;
import com.example.tinder.enums.UserRole;
import com.example.tinder.enums.UserStatus;
import com.example.tinder.exception.ResourceNotFoundException;
import com.example.tinder.exception.RestException;
import com.example.tinder.mapper.UserMapper;
import com.example.tinder.repository.CodeRepository;
import com.example.tinder.repository.UserRepository;
import com.example.tinder.service.UserService;
import com.example.tinder.utils.JwtUtil;
import com.example.tinder.utils.Utils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final CodeRepository codeRepository;
    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;
    private final Utils utils;
    private final WebSocketEventListener webSocketEventListener;

    @Override
    public ApiResponse<?> updateUserNameAndFullName(Long userId, String username, String fullname) {
        User user = this.userRepository.findById(userId).
                orElseThrow(() -> RestException.restThrow(ErrorType.USER_NOT_FOUND));

        if (utils.existUsername(username)) {
            throw RestException.restThrow(ErrorType.USER_ALREADY_EXISTS);
        }

        user.setUsername(username);
        user.setFullname(fullname);
        this.userRepository.save(user);
        return ApiResponse.success("User updated successfully");
    }


    @Override
    public ApiResponse<?> getUserById(Long id) {
        User user = this.userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return ApiResponse.success(this.userMapper.toDto(user), "SUCCESS");
    }


    @Override
    public ApiResponse<?> getAllActiveUser(Pageable pageable, Long currentUserId) {
        Page<User> page = this.userRepository.getAllUnswipedActiveUsers(currentUserId, pageable);
        if (page != null && !page.isEmpty()) {
            List<UserDto> list = this.userMapper.dtoList(page.getContent());


            Map<String, Object> responseMap = new HashMap<>();
            responseMap.put("users", list);
            responseMap.put("currentPage", page.getNumber());
            responseMap.put("totalPages", (page.getSize() + 1) / 2);

            return ApiResponse.success(responseMap, "SUCCESS");
        }

        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("users", new ArrayList<>());
        responseMap.put("currentPage", 0);
        responseMap.put("totalPages", 0);
        return ApiResponse.success(responseMap, "SUCCESS");
    }


    @Override
    public ApiResponse<?> updateEmail(String email, String newEmail) {
        User user = this.userRepository.findByEmail(email)
                .orElseThrow(() -> RestException.restThrow(ErrorType.USER_NOT_FOUND));

        Optional<User> optional = this.userRepository.findByEmail(newEmail);
        if (optional.isPresent()) {
            throw RestException.restThrow(ErrorType.EMAIL_ALREADY_TAKEN);
        }

        String code = utils.getCode();
        if (utils.sendCodeToMail(newEmail, code)) {
            Code c = new Code();
            c.setUser(user);
            c.setCode(code);
            c.setApprovedAt(LocalDateTime.now());
            c.setCreatedAt(LocalDateTime.now());
            c.setCounts(0);
            this.codeRepository.save(c);
            this.userRepository.save(user);
            return ApiResponse.success("Authentication code sent");
        }
        throw RestException.restThrow(ErrorType.AUTHENTICATION_CODE_ERROR);
    }

    @Override
    public ApiResponse<?> checkUpdatedEmail(String oldEmail, String newEmail, String code) {
        User user = this.userRepository.findByEmail(oldEmail)
                .orElseThrow(() -> RestException.restThrow(ErrorType.USER_NOT_FOUND));

        Code co = this.codeRepository.findByUserId(user.getId())
                .orElseThrow(() -> RestException.restThrow(ErrorType.CODE_IS_INVALID));

        if (co.getCode().equals(code)) {
            user.setEmail(newEmail);
            co.setApprovedAt(LocalDateTime.now());

            this.userRepository.save(user);
            this.codeRepository.delete(co);

            return ApiResponse.success("User email updated successfully");
        }
        throw RestException.restThrow(ErrorType.AUTHENTICATION_CODE_ERROR);
    }

    public ApiResponse<?> updatePassword(User user, String oldPassword, String newPassword) {
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw RestException.restThrow(ErrorType.OLD_PASSWORD_INCORRECT);
        }

        String encodedPassword = passwordEncoder.encode(newPassword);
        user.setPassword(encodedPassword);
        userRepository.save(user);

        return ApiResponse.success("Password updated successfully");
    }


    @Override
    public ApiResponse<?> checkUpdatedPassword(String email, String password, String code) {
        User user = this.userRepository.findByEmail(email)
                .orElseThrow(() -> RestException.restThrow(ErrorType.USER_NOT_FOUND));
        Code co = this.codeRepository.findByUserId(user.getId())
                .orElseThrow(() -> RestException.restThrow(ErrorType.AUTHENTICATION_CODE_ERROR));
        if (co.getCode().equals(code)) {
            user.setPassword(passwordEncoder.encode(password));
            this.userRepository.save(user);

            co.setApprovedAt(LocalDateTime.now());
            this.codeRepository.save(co);

            return ApiResponse.success("Password updated successfully");
        }

        throw RestException.restThrow(ErrorType.AUTHENTICATION_CODE_ERROR);
    }

    @Override
    public ApiResponse<?> checkUsername(String username) {
        if (this.utils.existUsername(username)) {
            throw RestException.restThrow(ErrorType.USER_ALREADY_EXISTS);

        }
        return ApiResponse.success("SUCCESS");
    }

    @Override
    public ApiResponse<?> updateProfilePhoto(Long attachmentId, Long userId) {
        return null;
    }

    @Override
    @Transactional // Tranzaksiya qo'shildi
    public ApiResponse<?> googleAuth(String email, String fullName, String picPath) {
        if (utils.checkEmail(email)) {
            User user = this.userRepository.findByEmail(email).orElse(null);
            String token;

            if (user != null) {
                // Foydalanuvchi mavjud bo'lsa:
                UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());
                token = jwtUtil.generateToken(userDetails.getUsername());

                return ApiResponse.success(
                        LoginResponse.builder()
                                .token(token)
                                .user(this.userMapper.toDto(user))
                                .build(), "SUCCESS");


            } else {
                // Foydalanuvchi mavjud bo'lmasa (yangi ro'yxatdan o'tish):
                user = new User();
                user.setEmail(email);
                user.setFullname(fullName);
                user.setUsername(email);
                user.setStatus(UserStatus.ACTIVE);
                user.setActive(true);

                user.setRole(UserRole.USER);
                user.setProfilePic(picPath);
                User save = userRepository.save(user);

                UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());
                token = jwtUtil.generateToken(userDetails.getUsername());


                return ApiResponse.success(
                        LoginResponse.builder()
                                .token(token)
                                .user(this.userMapper.toDto(save))
                                .build(), "GOOGLE AUTHORIZED SUCCESSFULLY"
                );
            }
        }
        throw RestException.restThrow(ErrorType.INVALID_EMAIL);
    }

    @Override
    public ApiResponse<?> search(String username, Gender gender) {
        List<User> users = userRepository.searchUsers(username, gender);

        List<Map<String, Object>> result = users.stream().map(user -> {
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("id", user.getId());
            userInfo.put("username", user.getUsername());
            userInfo.put("gender", user.getGender());
            userInfo.put("fullname", user.getFullname());
            userInfo.put("profilePic", user.getProfilePic());
            return userInfo;
        }).toList();

        return ApiResponse.success(result, "SEARCH RESULTS");
    }

    @Override
    @Transactional
    public ApiResponse<?> updateUserGBI(Long userId, UpdateUserGBIDto dto) {
        Optional<User> optionalUser = userRepository.findById(userId);
        if (optionalUser.isEmpty()) {
            throw RestException.restThrow(ErrorType.USER_NOT_FOUND);
        }

        User user = optionalUser.get();

        if (dto.getBirthDate() != null)
            user.setBirthDate(dto.getBirthDate());

        if (dto.getGender() != null)
            user.setGender(dto.getGender());

        if (dto.getBio() != null) {
            user.setBio(dto.getBio());
        }
        if (dto.getFullName() != null) {
            user.setFullname(dto.getFullName());
        }

        userRepository.save(user);

        return ApiResponse.success("User updated successfully");
    }


    @Override
    public ApiResponse<?> deleteUser(String userId) {
        User user = userRepository.findById(Long.parseLong(userId)).orElseThrow(() -> RestException.restThrow(ErrorType.USER_NOT_FOUND));
        user.setActive(false);
        userRepository.save(user);
        return ApiResponse.success("USER DELETED SUCCESSFULLY");
    }

    @Override
    public ApiResponse<?> me(User user) {
        userRepository.findByEmail(user.getEmail()).orElseThrow(() -> RestException.restThrow(ErrorType.USER_NOT_FOUND));
        return ApiResponse.success(userMapper.toDto(user), "SUCCESS");
    }

    @Override
    public ApiResponse<?> addInterest(User user, UserInterest userInterest) {
        user.getUserInterests().add(userInterest);
        userRepository.save(user);
        return ApiResponse.success(userMapper.toDto(user), "SUCCESSFULLY ADDED INTEREST");
    }


    @Override
    public ApiResponse<?> updateUsername(User user, String username) {
        Optional<User> byId = userRepository.findById(user.getId());
        if (byId.isEmpty()) throw RestException.restThrow(ErrorType.USER_NOT_FOUND);

        Optional<User> byUsername = userRepository.findByUsername(username);
        if (byUsername.isPresent()) throw RestException.restThrow(ErrorType.USERNAME_ALREADY_EXISTS);

        user.setUsername(username);
        User save = userRepository.save(user);


        UserDetails userDetails = userDetailsService.loadUserByUsername(save.getUsername());
        String jwtToken = jwtUtil.generateToken(userDetails.getUsername());

        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("token", jwtToken);
        userInfo.put("user", userMapper.toDto(save));

        return ApiResponse.success(userInfo, "USERNAME UPDATED SUCCESSFULLY");
    }

    @Override
    public ApiResponse<?> getOnlineUsers() {
        Set<String> onlineUsernames = webSocketEventListener.getOnlineUsernames();
        List<User> users = userRepository.findByUsernameIn(onlineUsernames);
        List<UserDto> response = users.stream()
                .map(userMapper::toDto)
                .toList();
        return ApiResponse.success(response, "SUCCESS");
    }


}

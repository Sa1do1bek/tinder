package com.example.tinder.service.impl;

import com.example.tinder.dto.ApiResponse;
import com.example.tinder.dto.UpdateUserGBIDto;
import com.example.tinder.dto.auth.LoginResponse;
import com.example.tinder.entity.*;
import com.example.tinder.enums.ErrorType;
import com.example.tinder.enums.Gender;
import com.example.tinder.enums.UserRole;
import com.example.tinder.enums.UserStatus;
import com.example.tinder.exception.ResourceNotFoundException;
import com.example.tinder.exception.RestException;
import com.example.tinder.mapper.UserMapper;
import com.example.tinder.repository.*;
import com.example.tinder.service.UserService;
import com.example.tinder.utils.JwtUtil;
import com.example.tinder.utils.Utils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final TokenRepository tokenRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final CodeRepository codeRepository;
    private final JwtUtil jwtUtil;
    private final Utils utils;
    private final UserInterestRepository userInterestRepository;
    private final SwipeRepository  swipeRepository;

    @Override
    public ApiResponse<?> updateUserNameAndFullName(Long userId, String username, String fullname) {
        User user = this.userRepository.findById(userId).
                orElseThrow(() -> new ResourceNotFoundException("User not found"));
        if (utils.existUsername(username)) {
            throw RestException.restThrow(ErrorType.USER_ALREADY_EXISTS);
        }
        user.setUsername(username);
        user.setFullname(fullname);
        this.userRepository.save(user);
        return ApiResponse.success("User updated successfully");
    }

//    @Override
//    public ApiResponse<?> addRoleToUser(Long userId, Long roleId) {
//        User user = userRepository.findById(userId)
//                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
//
//        if (user.getRole() != null) {
//            user.getRole().add(role);
//        } else {
//            user.setRole(new HashSet<>(Set.of(role)));
//        }
//
//        userRepository.save(user);
//
//        return ApiResponse.success("Role added to user successfully");
//    }

//    @Override
//    public ApiResponse<?> deleteRoleFromUser(Long userId, Long roleId) {
//        this.userRepository.findById(userId)
//                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
//        this.roleRepository.findById(roleId)
//                .orElseThrow(() -> new ResourceNotFoundException("Role not found"));
//        this.userRepository.deleteRoleFromUser(userId, roleId);
//        return ApiResponse.success("Delete role from user");
//    }

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

            return ApiResponse.success(this.userMapper.dtoList(page.getContent()), "SUCCESS");
        }
        return ApiResponse.success(new ArrayList<>(), "SUCCESS");
    }


    @Override
    public ApiResponse<?> getUserByToken(String token) {
        Token t = this.tokenRepository.findByToken(token)
                .orElseThrow(() -> new ResourceNotFoundException("Token not found"));
        User user = this.userRepository.findById(t.getUser().getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));


        return ApiResponse.success(this.userMapper.toDto(user), "SUCCESS");
    }

    @Override
    public ApiResponse<?> updateEmail(String email, String newEmail) {
        User user = this.userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Optional<User> optional = this.userRepository.findByEmail(newEmail);
        if (optional.isPresent()) {
            throw RestException.restThrow(ErrorType.INVALID_EMAIL);
        }

        String code = utils.getCode();
        if (utils.sendCodeToMail(newEmail, code)) {
            Optional<Code> codeOptional = this.codeRepository.findByUserId(user.getId());
            if (codeOptional.isPresent()) {
                Code c = codeOptional.get();
                c.setCode(code);
                c.setCreatedAt(LocalDateTime.now());
                this.codeRepository.save(c);
                this.userRepository.save(user);

                return ApiResponse.success("Authentication code sent");
            }
        }

        throw RestException.restThrow(ErrorType.AUTHENTICATION_CODE_ERROR);
    }

    @Override
    public ApiResponse<?> checkUpdatedEmail(String oldEmail, String newEmail, String code) {
        User user = this.userRepository.findByEmail(oldEmail)
                .orElseThrow(() -> RestException.restThrow(ErrorType.USER_NOT_FOUND));

        Code co = this.codeRepository.findByUserId(user.getId())
                .orElseThrow(() -> RestException.restThrow(ErrorType.AUTHENTICATION_CODE_ERROR));

        if (co.getCode().equals(code)) {
            user.setEmail(newEmail);
            co.setApprovedAt(LocalDateTime.now());

            this.userRepository.save(user);
            this.codeRepository.save(co);

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
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Code co = this.codeRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Code not found"));
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
                // 1. Mavjud barcha tokenlarni o'chirish
                tokenRepository.deleteByUserId(user.getId());

                // 2. Yangi token yaratish
                token = jwtUtil.generateToken(user.getUsername());
                Token newToken = Token.builder()
                        .user(user)
                        .token(token)
                        .type("Bearer")
                        .createdAt(LocalDateTime.now())
                        .expiredAt(LocalDateTime.now().plusDays(10))
                        .build();
                tokenRepository.save(newToken);

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
                // Username'ni ham o'rnatish kerak, chunki JWT uni ishlatadi
                user.setUsername(email); // Yoki boshqa noyob qiymat
                user.setStatus(UserStatus.ACTIVE); // Google orqali ro'yxatdan o'tganlar faol bo'lishi kerak
                user.setActive(true);

                user.setRole(UserRole.USER);
                user.setProfilePic(picPath);
                User save = userRepository.save(user);

                // Yangi foydalanuvchi uchun token yaratish
                token = jwtUtil.generateToken(save.getUsername());
                Token t = new Token();
                t.setToken(token);
                t.setUser(save);
                t.setType("Bearer");
                t.setCreatedAt(LocalDateTime.now());
                t.setExpiredAt(LocalDateTime.now().plusDays(10));
                tokenRepository.save(t);

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

        if (dto.getInterests() != null) {
            user.getUserInterests().clear();

            List<UserInterest> newInterests = dto.getInterests().stream()
                    .map(interest -> UserInterest.builder()
                            .user(user)
                            .name(interest)
                            .build())
                    .toList();

            user.getUserInterests().addAll(newInterests);
        }
        if (dto.getBio() != null){
            user.setBio(dto.getBio());
        }

        userRepository.save(user);

        return ApiResponse.success("User updated successfully");
    }

    @Override
    public ApiResponse<?> getAllRejectUsers(PageRequest pageRequest, Long userId) {
        Page<User> rejectedUsers = swipeRepository.findAllRejectedUsers(userId, pageRequest);

        if (!rejectedUsers.isEmpty()) {
            return ApiResponse.success(userMapper.dtoList(rejectedUsers.getContent()), "SUCCESS");
        }

        return ApiResponse.success(new ArrayList<>(), "SUCCESS");
    }
    @Override
    public ApiResponse<?> getAllAcceptUsers(PageRequest pageRequest, Long userId) {
        Page<User> rejectedUsers = swipeRepository.findAllAcceptedUsers(userId, pageRequest);

        if (!rejectedUsers.isEmpty()) {

            return ApiResponse.success(userMapper.dtoList(rejectedUsers.getContent()), "SUCCESS");
//            return ApiResponse.builder()
//                    .status(HttpStatus.OK)
//                    .data(userMapper.dtoList(rejectedUsers.getContent()))
//                    .elements(rejectedUsers.getTotalElements())
//                    .pages(rejectedUsers.getTotalPages())
//                    .build();
        }

        return ApiResponse.success(new ArrayList<>(), "SUCCESS");
    }

    @Override
    public ApiResponse<?> deleteUser(User user) {
        user.setActive(false);
        tokenRepository.deleteByUserId(user.getId());
        userRepository.save(user);
        return ApiResponse.success("User deleted successfully");
    }


}

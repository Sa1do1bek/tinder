package com.example.tinder.service.impl;

import com.example.tinder.dto.ApiResponse;
import com.example.tinder.entity.*;
import com.example.tinder.exception.ResourceNotFoundException;
import com.example.tinder.mapper.UserMapper;
import com.example.tinder.repository.*;
import com.example.tinder.service.UserService;
import com.example.tinder.utils.JwtUtil;
import com.example.tinder.utils.Utils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final TokenRepository tokenRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final CodeRepository codeRepository;
    private final JwtUtil jwtUtil;
    private final Utils utils;
    @Override
    public ApiResponse updateUserNameAndFullName(Long userId, String username, String fullname) {
        User user = this.userRepository.findById(userId).
                orElseThrow(() -> new ResourceNotFoundException("User not found"));
        if (utils.existUsername(username)) {
            return ApiResponse.builder()
                    .status(HttpStatus.BAD_REQUEST)
                    .message("username already exists")
                    .build();
        }
        user.setUsername(username);
        user.setFullname(fullname);
        this.userRepository.save(user);
        return ApiResponse.builder()
                .status(HttpStatus.OK)
                .message("User updated successfully")
                .build();
    }

    @Override
    public ApiResponse addRoleToUser(Long userId, Long roleId) {
        User user = this.userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        UserRole role = this.roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found"));
        if (user.getRole() != null && !user.getRole().isEmpty()) {
            user.getRole().add(role);
        } else {
            user.setRole(new HashSet<>(Set.of(role)));
        }
        this.userRepository.save(user);
        return ApiResponse.builder()
                .status(HttpStatus.OK)
                .message("Role added to user successfully")
                .build();
    }

    @Override
    public ApiResponse deleteRoleFromUser(Long userId, Long roleId) {
        this.userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        this.roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found"));
        this.userRepository.deleteRoleFromUser(userId, roleId);
        return ApiResponse.builder()
                .status(HttpStatus.OK)
                .message("Delete role from user")
                .build();
    }

    @Override
    public ApiResponse getUserById(Long id) {
        User user = this.userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return ApiResponse.builder()
                .status(HttpStatus.OK)
                .message("SUCCESS")
                .data(this.userMapper.toDto(user))
                .build();
    }


    @Override
    public ApiResponse getAllActiveUser(Pageable pageable) {
        Page<User> page = this.userRepository.getAllActiveUsers(pageable);
        if (page != null && !page.isEmpty()) {
            return ApiResponse.builder()
                    .status(HttpStatus.OK)
                    .data(this.userMapper.dtoList(page.getContent()))
                    .elements(page.getTotalElements())
                    .pages(page.getTotalPages())
                    .build();
        }
        return ApiResponse.builder()
                .status(HttpStatus.OK)
                .data(new ArrayList<>())
                .build();
    }

    @Override
    public ApiResponse getUserByToken(String token) {
        Token t = this.tokenRepository.findByToken(token)
                .orElseThrow(() -> new ResourceNotFoundException("Token not found"));
        User user = this.userRepository.findById(t.getUser().getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return ApiResponse.builder()
                .status(HttpStatus.OK)
                .data(this.userMapper.toDto(user))
                .build();
    }

    @Override
    public ApiResponse updateEmail(String email, String newEmail) {
        User user = this.userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Optional<User> optional = this.userRepository.findByEmail(newEmail);
        if (optional.isPresent()) {
            return ApiResponse.builder()
                    .status(HttpStatus.BAD_REQUEST)
                    .message("Invalid user email")
                    .build();
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

                return ApiResponse.builder()
                        .status(HttpStatus.OK)
                        .message("Authentication code sent")
                        .build();
            }
        }
        return ApiResponse.builder()
                .status(HttpStatus.BAD_REQUEST)
                .message("Authentication code error")
                .build();
    }

    @Override
    public ApiResponse checkUpdatedEmail(String oldEmail, String newEmail, String code) {
        User user = this.userRepository.findByEmail(oldEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Code co = this.codeRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Authentication error"));
        if (co.getCode().equals(code)) {
            user.setEmail(newEmail);
            co.setApprovedAt(LocalDateTime.now());

            this.userRepository.save(user);
            this.codeRepository.save(co);

            return ApiResponse.builder()
                    .status(HttpStatus.OK)
                    .message("User updated successfully")
                    .build();
        }
        return ApiResponse.builder()
                .status(HttpStatus.BAD_REQUEST)
                .message("Authentication error")
                .build();
    }

    @Override
    public ApiResponse updatePassword(String email, String newPassword) {
        User user = this.userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        String code = utils.getCode();
        if (utils.sendCodeToMail(email, code)) {
            Optional<Code> optional = this.codeRepository.findByUserId(user.getId());
            if (optional.isPresent()) {
                Code c = optional.get();
                c.setCode(code);
                c.setCreatedAt(LocalDateTime.now());
                this.codeRepository.save(c);
                this.userRepository.save(user);
                return ApiResponse.builder()
                        .status(HttpStatus.OK)
                        .message("User updated")
                        .build();
            }
        }
        return ApiResponse.builder()
                .status(HttpStatus.BAD_REQUEST)
                .message("Auth code error")
                .build();
    }

    @Override
    public ApiResponse checkUpdatedPassword(String email, String password, String code) {
        User user = this.userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Code co = this.codeRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Code not found"));
        if (co.getCode().equals(code)) {
            user.setPassword(passwordEncoder.encode(password));
            this.userRepository.save(user);

            co.setApprovedAt(LocalDateTime.now());
            this.codeRepository.save(co);

            return ApiResponse.builder()
                    .status(HttpStatus.OK)
                    .message("User updated")
                    .build();
        }
        return ApiResponse.builder()
                .status(HttpStatus.BAD_REQUEST)
                .message("Auth code error")
                .build();
    }

    @Override
    public ApiResponse checkUsername(String username) {
        if (this.utils.existUsername(username)) {
            return ApiResponse.builder()
                    .status(HttpStatus.BAD_REQUEST)
                    .message("Username already exists")
                    .build();
        }
        return ApiResponse.builder()
                .status(HttpStatus.OK)
                .message("success")
                .build();
    }

    @Override
    public ApiResponse updateProfilePhoto(Long attachmentId, Long userId) {
        return null;
    }

    @Override
    public ApiResponse googleAuth(String email, String fullName, String picPath) {
        if (utils.checkEmail(email)) {
            User user = this.userRepository.findByEmail(email).orElse(null);
            if (user != null) {
                String token = jwtUtil.generateToken(user.getUsername());
                tokenRepository.findByUserId(user.getId())
                        .ifPresent(t -> {
                            t.setToken(token);
                            tokenRepository.save(t);
                        });

                return ApiResponse.builder()
                        .status(HttpStatus.OK)
                        .meta(Map.of("user", this.userMapper.toDto(user)))
                        .data(token)
                        .build();
            }
            user = new User();
            user.setEmail(email);
            user.setFullname(fullName);
            UserRole userRole = roleRepository.findByName("USER")
                    .orElseThrow(() -> new ResourceNotFoundException("ROLE NOT FOUND"));
            user.setRole(new HashSet<>(Set.of(userRole)));
            user.setProfilePic(picPath);
            User save = userRepository.save(user);

            String token = jwtUtil.generateToken(save.getUsername());
            Token t = new Token();
            t.setToken(token);
            t.setUser(save);
            t.setType("Bearer");
            t.setCreatedAt(LocalDateTime.now());
            t.setExpiredAt(LocalDateTime.now().plusDays(10));
            tokenRepository.save(t);

            return ApiResponse.builder()
                    .status(HttpStatus.OK)
                    .message("Google authorized")
                    .data(token)
                    .meta(Map.of("user", this.userMapper.toDto(user)))
                    .build();
        }
        return ApiResponse.builder()
                .status(HttpStatus.BAD_REQUEST)
                .message("User email is invalid")
                .build();
    }

//    @Override
//    public ApiResponse updateProfilePhoto(Long attachmentId,Long userId) {
//        Attachment attachment = this.attachmentRepository.findById(attachmentId)
//                .orElseThrow(()->new ResourceNotFoundException("Attachment not found"));
//        User user = this.userRepository.findById(userId)
//                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
//        user.setProfilePhoto(attachment);
//        this.userRepository.save(user);
//        return ApiResponse.builder()
//                .status(HttpStatus.OK)
//                .message("success")
//                .build();
//    }
}

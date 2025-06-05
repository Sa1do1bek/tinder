package com.example.tinder.service;

import com.example.tinder.dto.ApiResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Component
public interface UserService {
    ApiResponse updateUserNameAndFullName(Long userId, String username, String fullname);
    ApiResponse addRoleToUser(Long userId, Long roleId);
    ApiResponse deleteRoleFromUser(Long userId, Long roleId);
    ApiResponse getUserById(Long id);
    ApiResponse getAllActiveUser(Pageable pageable);
    ApiResponse getUserByToken(String token);


    ApiResponse updateEmail(String email, String newEmail);

    ApiResponse checkUpdatedEmail(String oldEmail, String newEmail, String code);

    ApiResponse updatePassword(String email, String newPassword);

    ApiResponse checkUpdatedPassword(String email, String password, String code);

    ApiResponse checkUsername(String username);

    ApiResponse updateProfilePhoto(Long attachmentId,Long userId);

    ApiResponse googleAuth(String email, String fullName, String picPath);
}

package com.example.tinder.service;

import com.example.tinder.dto.base.ApiResponse;
import com.example.tinder.dto.UpdateUserGBIDto;
import com.example.tinder.entity.User;
import com.example.tinder.entity.UserInterest;
import com.example.tinder.enums.Gender;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Component
public interface UserService {
    ApiResponse<?> updateUserNameAndFullName(Long userId, String username, String fullname);

    ApiResponse<?> getUserById(Long id);

    ApiResponse<?> getAllActiveUser(Pageable pageable, Long userId);

    ApiResponse<?> updateEmail(String email, String newEmail);

    ApiResponse<?> checkUpdatedEmail(String oldEmail, String newEmail, String code);

    ApiResponse<?> updatePassword(User user, String email, String newPassword);

    ApiResponse<?> checkUpdatedPassword(String email, String password, String code);

    ApiResponse<?> checkUsername(String username);

    ApiResponse<?> updateProfilePhoto(Long attachmentId,Long userId);

    ApiResponse<?> googleAuth(String email, String fullName, String picPath);

    ApiResponse<?> search(String username, Gender gender);

    ApiResponse<?> updateUserGBI(Long userId, UpdateUserGBIDto dto);

    ApiResponse<?> getAllRejectUsers(PageRequest pageRequest, Long userId);

    ApiResponse<?> getAllAcceptUsers(PageRequest pageRequest, Long userId);

    ApiResponse<?> deleteUser(String userId);

    ApiResponse<?> me(User user);

    ApiResponse<?> addInterest(User user, UserInterest userInterest);

    ApiResponse<?> updateUsername(User user, String username);
}

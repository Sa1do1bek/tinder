package com.example.tinder.service;

import com.example.tinder.dto.base.ApiResponse;
import com.example.tinder.entity.User;
import org.springframework.data.domain.Page;

public interface AdminService {

    ApiResponse<Page<User>> getAllUsers(int page, int size);

    ApiResponse<User> toggleUserActive(Long userId);

}

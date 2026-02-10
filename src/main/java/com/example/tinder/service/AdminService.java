package com.example.tinder.service;

import com.example.tinder.dto.UserDto;
import com.example.tinder.dto.base.ApiResponse;
import org.springframework.data.domain.Page;

public interface AdminService {

    ApiResponse<Page<UserDto>> getAllUsers(int page, int size);

    ApiResponse<UserDto> toggleUserActive(Long userId);

}

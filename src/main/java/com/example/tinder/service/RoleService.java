package com.example.tinder.service;

import com.example.tinder.dto.ApiResponse;
import com.example.tinder.dto.RoleDto;
import lombok.NonNull;

public interface RoleService {
    ApiResponse<?> create(RoleDto.@NonNull CreateRole dto);

    ApiResponse<?> getAll();
}

package com.example.tinder.service.impl;

import com.example.tinder.dto.UserDto;
import com.example.tinder.dto.base.ApiResponse;
import com.example.tinder.entity.User;
import com.example.tinder.enums.ErrorType;
import com.example.tinder.enums.SuccessType;
import com.example.tinder.exception.RestException;
import com.example.tinder.mapper.UserMapper;
import com.example.tinder.repository.UserRepository;
import com.example.tinder.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;



    @Override
    public ApiResponse<Page<UserDto>> getAllUsers(int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size);
        Page<User> users = userRepository.findAllCustom(pageRequest);

        return ApiResponse.success(users.map(userMapper::toDto), SuccessType.USER_FOUND_SUCCESSFULLY);
    }

    @Override
    @Transactional
    public ApiResponse<UserDto> toggleUserActive(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> RestException.restThrow(ErrorType.USER_NOT_FOUND));

        user.setActive(!user.isActive());

        User savedUser = userRepository.save(user);


        return ApiResponse.success(userMapper.toDto(savedUser), SuccessType.SUCCESS);
    }
}

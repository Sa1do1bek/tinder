package com.example.tinder.service.impl;

import com.example.tinder.dto.base.ApiResponse;
import com.example.tinder.entity.User;
import com.example.tinder.enums.ErrorType;
import com.example.tinder.exception.RestException;
import com.example.tinder.mapper.UserMapper;
import com.example.tinder.repository.SwipeRepository;
import com.example.tinder.repository.UserRepository;
import com.example.tinder.service.LikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class LikeServiceImpl implements LikeService {
    private final SwipeRepository swipeRepository;
    private final UserMapper userMapper;

    @Override
    public ApiResponse<?> getAllRejectUsers(PageRequest pageRequest, Long userId) {
        Page<User> rejectedUsers = swipeRepository.findAllRejectedUsers(userId, pageRequest);

        if (!rejectedUsers.isEmpty()) {
            return ApiResponse.success(userMapper.dtoList(rejectedUsers.getContent()), "SUCCESS");
        }

        return ApiResponse.success(new ArrayList<>(), "SUCCESS");
    }


    @Override
    public ApiResponse<?> getMyLikedUsers(PageRequest pageRequest, Long userId) {
        Page<User> rejectedUsers = swipeRepository.findAllAcceptedUsers(userId, pageRequest);

        if (!rejectedUsers.isEmpty()) {
            return ApiResponse.success(userMapper.dtoList(rejectedUsers.getContent()), "SUCCESS");
        }

        return ApiResponse.success(new ArrayList<>(), "SUCCESS");
    }

    @Override
    public ApiResponse<?> getMyLovers(PageRequest pageRequest, Long userId) {

        Page<User> rejectedUsers = swipeRepository.findAllUsersWhoLikeMe(userId, pageRequest);

        if (!rejectedUsers.isEmpty()) {
            return ApiResponse.success(userMapper.dtoList(rejectedUsers.getContent()), "SUCCESS");
        }

        return ApiResponse.success(new ArrayList<>(), "SUCCESS");
    }

}

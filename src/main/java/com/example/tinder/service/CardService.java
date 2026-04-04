package com.example.tinder.service;

import com.example.tinder.dto.base.ApiResponse;
import com.example.tinder.entity.User;

public interface CardService {
    ApiResponse<?> getBalance(User user);
}

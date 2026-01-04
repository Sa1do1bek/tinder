package com.example.tinder.service;

import com.example.tinder.dto.CardDto;
import com.example.tinder.dto.base.ApiResponse;
import com.example.tinder.entity.User;
import com.google.protobuf.Api;

public interface CardService {
    ApiResponse<?> getBalance(User user);
}

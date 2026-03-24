package com.example.tinder.service;

import com.example.tinder.dto.base.ApiResponse;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
@Service
public interface ProfileViewService {
    ApiResponse<?> getProfileViewCount();
    ApiResponse<?> getProfileViewStats(LocalDate start, LocalDate end);
}

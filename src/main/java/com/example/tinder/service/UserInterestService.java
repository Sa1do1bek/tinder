package com.example.tinder.service;

import com.example.tinder.dto.ApiResponse;
import com.example.tinder.dto.interest.UserInterestDto;
import org.springframework.stereotype.Service;

@Service
public interface UserInterestService {

    ApiResponse<?> add(UserInterestDto.UserInterestCreateDto dto);
    ApiResponse<?> delete(Long id);

    ApiResponse<?> getAllCategoryTypes();

}

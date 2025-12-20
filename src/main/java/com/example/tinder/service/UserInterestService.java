package com.example.tinder.service;

import com.example.tinder.dto.ApiResponse;
import com.example.tinder.dto.interest.UserInterestDto;
import com.example.tinder.entity.User;
import com.example.tinder.enums.CategoryType;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface UserInterestService {

    ApiResponse<?> addCategory(UserInterestDto.UserInterestCreateDto dto);

    ApiResponse<?> delete(Long id);

    ApiResponse<?> getAllCategoryTypes();

    ApiResponse<?> getAllCategoriesByType(CategoryType type);

    ApiResponse<?> addCategoryToUser(Long userId, List<Long> categoryIds);

    ApiResponse<?> getUserCategories(Long userId);

    ApiResponse<?> getUserCategoriesByType(Long userId, CategoryType type);


}

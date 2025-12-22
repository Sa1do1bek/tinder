package com.example.tinder.service.impl;

import com.example.tinder.dto.base.ApiResponse;
import com.example.tinder.dto.UserInterestDto;
import com.example.tinder.entity.User;
import com.example.tinder.entity.UserInterest;
import com.example.tinder.enums.CategoryType;
import com.example.tinder.enums.ErrorType;
import com.example.tinder.exception.RestException;
import com.example.tinder.mapper.UserInterestMapper;
import com.example.tinder.repository.UserInterestRepository;
import com.example.tinder.repository.UserRepository;
import com.example.tinder.service.UserInterestService;
import com.example.tinder.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserInterestServiceImpl implements UserInterestService {
    private final UserInterestRepository repository;
    private final UserInterestMapper mapper;
    private final UserRepository userRepository;
    private final UserService userService;

    @Override
    public ApiResponse<?> addCategory(UserInterestDto.UserInterestCreateDto dto ) {

        CategoryType categoryType;
        try {
            categoryType = CategoryType.valueOf(dto.getCategoryType().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw RestException.restThrow(ErrorType.INVALID_CATEGORY_TYPE);
        }


        Optional<UserInterest> byCode = repository.findByCode(dto.getCode());
        if (byCode.isPresent()) {
            throw RestException.restThrow(ErrorType.INTEREST_ALREADY_EXISTS);
        }

        UserInterest entity = mapper.toCreateEntity(dto);

        repository.save(entity);

        return ApiResponse.success("INTEREST_CREATED_SUCCESSFULLY");
    }

    @Override
    public ApiResponse<?> delete(Long id) {
        Optional<UserInterest> byId = repository.findById(id);
        if (byId.isEmpty()) {
            throw RestException.restThrow(ErrorType.INTEREST_NOT_FOUND);
        }
        UserInterest entity = byId.get();
        entity.setVisibility(false);
        repository.save(entity);

        return ApiResponse.success("INTEREST_DELETED_SUCCESSFULLY");
    }

    @Override
    public ApiResponse<?> getAllCategoryTypes() {
        CategoryType[] values = CategoryType.values();
        List<CategoryType> types = new ArrayList<>(Arrays.asList(values));
        return ApiResponse.success(types);
    }

    @Override
    public ApiResponse<?> getAllCategoriesByType(CategoryType type) {
        List<UserInterest> byType = repository.findByCategoryType(type);
        return ApiResponse.success(mapper.toListDto(byType), "INTEREST_FETCHED_SUCCESSFULLY");
    }

    @Override
    public ApiResponse<?> addCategoryToUser(Long userId, List<Long> categoryIds) {

        Optional<User> byId = userRepository.findById(userId);
        if (byId.isEmpty()) {
            throw RestException.restThrow(ErrorType.USER_NOT_FOUND);
        }
        User user = byId.get();

        if (categoryIds.isEmpty()){
            throw RestException.restThrow(ErrorType.NULL_REQUEST_BODY);
        }

        List<UserInterest> interest = new ArrayList<>();
        for (Long id : categoryIds) {
            Optional<UserInterest> c = repository.findById(id);
            if (c.isEmpty()) {
                throw RestException.restThrow(ErrorType.USER_NOT_FOUND);
            }
            interest.add(c.get());
            userService.addInterest(user, c.get());
        }

        return ApiResponse.success(mapper.toListDto(interest), "INTEREST_FETCHED_SUCCESSFULLY");
    }

    @Override
    public ApiResponse<?> getUserCategories(Long userId) {
        List<UserInterest> list = repository.findByUserId(userId);
        if (list.isEmpty()) {
            throw RestException.restThrow(ErrorType.INTEREST_NOT_FOUND);
        }

        return ApiResponse.success(mapper.toListDto(list), "INTEREST_FETCHED_SUCCESSFULLY");
    }

    @Override
    public ApiResponse<?> getUserCategoriesByType(Long userId, CategoryType type) {
        Optional<User> optionalUser = userRepository.findById(userId);
        if (optionalUser.isEmpty()) {
            throw RestException.restThrow(ErrorType.USER_NOT_FOUND);
        }

        List<UserInterest> list = repository.findByUserIdAndType(userId, type);
        if (list.isEmpty()) {
            throw RestException.restThrow(ErrorType.INTEREST_NOT_FOUND);
        }

        return ApiResponse.success(mapper.toListDto(list), "INTEREST_FETCHED_SUCCESSFULLY");
    }




}

package com.example.tinder.service.impl;

import com.example.tinder.dto.ApiResponse;
import com.example.tinder.dto.interest.UserInterestDto;
import com.example.tinder.entity.User;
import com.example.tinder.entity.UserInterest;
import com.example.tinder.enums.CategoryType;
import com.example.tinder.enums.ErrorType;
import com.example.tinder.exception.RestException;
import com.example.tinder.mapper.UserInterestMapper;
import com.example.tinder.repository.UserInterestRepository;
import com.example.tinder.repository.UserRepository;
import com.example.tinder.service.UserInterestService;
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

    @Override
    public ApiResponse<?> add(UserInterestDto.UserInterestCreateDto dto ) {

        Optional<UserInterest> byCode = repository.findByCode(dto.getCode());
        if (byCode.isPresent()) {
            throw RestException.restThrow(ErrorType.INTEREST_ALREADY_EXISTS);
        }

        Optional<User> optionalUser = userRepository.findById(dto.getUserId());
        if (optionalUser.isEmpty()) {
            throw RestException.restThrow(ErrorType.USER_NOT_FOUND);
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


}

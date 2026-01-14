package com.example.tinder.service;

import com.example.tinder.dto.base.ApiResponse;
import com.example.tinder.dto.superLike.SuperLikePackageDto;
import org.springframework.stereotype.Service;

@Service
public interface SuperLikePackageService {

    ApiResponse<?> createPackage(SuperLikePackageDto.SuperLikePackageCreateDto dto);

    ApiResponse<?> deletePackage(Long packageId);

}

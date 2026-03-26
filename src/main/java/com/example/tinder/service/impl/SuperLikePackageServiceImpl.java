package com.example.tinder.service.impl;

import com.example.tinder.dto.base.ApiResponse;
import com.example.tinder.dto.superLike.SuperLikePackageDto;
import com.example.tinder.entity.SuperLikePackage;
import com.example.tinder.enums.ErrorType;
import com.example.tinder.enums.SuccessType;
import com.example.tinder.exception.RestException;
import com.example.tinder.mapper.SuperLikePackageMapper;
import com.example.tinder.repository.SuperLikePackageRepository;
import com.example.tinder.service.SuperLikePackageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SuperLikePackageServiceImpl implements SuperLikePackageService {

    private final SuperLikePackageRepository superLikePackageRepository;
    private final SuperLikePackageMapper superLikePackageMapper;

    @Override
    public ApiResponse<?> createPackage(SuperLikePackageDto.SuperLikePackageCreateDto dto) {
        if (dto.getQuantity() == null || dto.getQuantity() <= 0)
            throw RestException.restThrow(ErrorType.INVALID_PACKAGE_QUANTITY);
        if (dto.getPriceUZS() == null || dto.getPriceUZS().compareTo(BigDecimal.ZERO) <= 0)
            throw RestException.restThrow(ErrorType.INVALID_PACKAGE_PRICE);
        if (dto.getPricePerUnit() == null || dto.getPricePerUnit().compareTo(BigDecimal.ZERO) <= 0)
            throw RestException.restThrow(ErrorType.INVALID_PACKAGE_PRICE);
        if (dto.getPlatformCommission() == null || dto.getPlatformCommission().compareTo(BigDecimal.ZERO) < 0)
            throw RestException.restThrow(ErrorType.INVALID_PLATFORM_COMMISSION);
        SuperLikePackage entity = superLikePackageMapper.fromCreateDto(dto);

        //  agar bu discount (parent bor) bo‘lsa
        if (dto.getParentPackageId() != null) {
            SuperLikePackage parent = superLikePackageRepository.findById(dto.getParentPackageId())
                    .orElseThrow(() -> RestException.restThrow(ErrorType.PACKAGE_NOT_FOUND));
            entity.setParentPackage(parent);

            //  eski package ni o‘chiramiz (history saqlanadi)
            parent.setVisibility(false);
            superLikePackageRepository.save(parent);
        }
        SuperLikePackage saved = superLikePackageRepository.save(entity);
        return ApiResponse.success(superLikePackageMapper.toDto(saved),
                SuccessType.PACKAGE_CREATED_SUCCESSFULLY);
    }

    @Override
    public ApiResponse<?> deletePackage(Long packageId) {
        Optional<SuperLikePackage> optional = superLikePackageRepository.findByIdAndVisibilityTrue(packageId);
        if (optional.isEmpty()) throw RestException.restThrow(ErrorType.PACKAGE_NOT_FOUND);

        SuperLikePackage entity = optional.get();
        entity.setVisibility(false);
        superLikePackageRepository.save(entity);

        return ApiResponse.success(superLikePackageMapper.toDto(entity), SuccessType.PACKAGE_DELETED_SUCCESSFULLY);
    }
}

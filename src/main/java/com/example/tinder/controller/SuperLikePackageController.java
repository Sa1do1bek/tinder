package com.example.tinder.controller;

import com.example.tinder.dto.base.ApiResponse;
import com.example.tinder.dto.superLike.SuperLikePackageDto;
import com.example.tinder.service.SuperLikePackageService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/superlikePackage")
@RequiredArgsConstructor
public class SuperLikePackageController {

    private final SuperLikePackageService superLikePackageService;

    @PostMapping("superlikePackageCreate")
    @PreAuthorize("hasAnyRole('ADMIN')")
    @Operation(summary = "Super Like paketini yaratish")
    public ApiResponse<?> superLikePackageCreate(@RequestBody SuperLikePackageDto.SuperLikePackageCreateDto dto){
        return superLikePackageService.createPackage(dto);
    }

    @DeleteMapping("superlikePackageDelete")
    @PreAuthorize("hasAnyRole('ADMIN')")
    @Operation(summary = "Super Like paketini O'chirish")
    public ApiResponse<?>  superLikePackageDelete(@RequestParam("packageId") Long packageId){
        return superLikePackageService.deletePackage(packageId);
    }
}

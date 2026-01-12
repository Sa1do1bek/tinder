package com.example.tinder.controller;

import com.example.tinder.dto.VerificationDto;
import com.example.tinder.dto.base.ApiResponse;
import com.example.tinder.entity.User;
import com.example.tinder.service.VerificationService;
import com.example.tinder.utils.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/verification")
@RequiredArgsConstructor
public class VerificationController {
    private final VerificationService verificationService;


    @PutMapping("/verify")
    public ApiResponse<?> verify(@CurrentUser User user,
                                 @RequestBody VerificationDto.VerificationUpdateDto dto) {
        return verificationService.update(dto, user);
    }

    @PostMapping(value = "/uploadApproveImage", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<?> uploadApproveImage(@CurrentUser User user,
                                 @RequestParam("files") List<MultipartFile> files ) {
        return verificationService.uploadApproveImage(user, files);
    }

    @GetMapping("/get")
    public ApiResponse<?> get(@CurrentUser User user) {
        return verificationService.get(user);
    }
}

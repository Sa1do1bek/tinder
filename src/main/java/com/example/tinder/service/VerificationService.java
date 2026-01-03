package com.example.tinder.service;

import com.example.tinder.dto.VerificationDto;
import com.example.tinder.dto.base.ApiResponse;
import com.example.tinder.entity.User;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
public interface VerificationService {

    ApiResponse<?> update(VerificationDto.VerificationUpdateDto dto,  User user);

    ApiResponse<?> get(User user);

    ApiResponse<?> uploadApproveImage(User user, List<MultipartFile> files);
}

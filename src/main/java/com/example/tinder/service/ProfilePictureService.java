package com.example.tinder.service;

import com.example.tinder.dto.ApiResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public interface ProfilePictureService {

    ApiResponse uploadPicture(Long userId, MultipartFile file);

    ApiResponse getUserPictures(Long userId);

    ApiResponse deletePicture(Long id);

    ApiResponse setAsMainPicture(Long userId, Long pictureId);
}

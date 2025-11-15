package com.example.tinder.service;

import com.example.tinder.dto.ApiResponse;
import com.example.tinder.entity.User;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public interface ProfilePictureService {

    ApiResponse uploadPicture(User user, MultipartFile file);

    ApiResponse getUserPictures(Long userId);

    ApiResponse deletePicture(Long id, Long userId);

    ApiResponse setAsMainPicture(Long userId, Long pictureId);
}

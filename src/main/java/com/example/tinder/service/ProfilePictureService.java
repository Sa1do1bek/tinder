package com.example.tinder.service;

import com.example.tinder.dto.base.ApiResponse;
import com.example.tinder.entity.User;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Component
public interface ProfilePictureService {

    ApiResponse<?> getUserPictures(Long userId);

    ApiResponse<?> deletePicture(Long userId, Long imageId);

    ApiResponse<?> setAsMainPicture(Long userId, Long pictureId);

    ApiResponse<?> uploadPictures(User user, List<MultipartFile> files);
}

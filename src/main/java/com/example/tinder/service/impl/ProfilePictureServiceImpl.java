package com.example.tinder.service.impl;

import com.example.tinder.dto.ApiResponse;
import com.example.tinder.entity.User;
import com.example.tinder.repository.UserRepository;
import com.example.tinder.service.ProfilePictureService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProfilePictureServiceImpl implements ProfilePictureService {
    private final UserRepository userRepository;


    @Override
    public ApiResponse uploadPicture(Long userId, MultipartFile file) {
        Optional<User> user = userRepository.findById(userId);
        if(user.isEmpty()){
            return ApiResponse.builder()
                    .status(HttpStatus.BAD_REQUEST)
                    .message("User not found")
                    .build();
        }


    }
}

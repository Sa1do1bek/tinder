package com.example.tinder.service.impl;

import com.example.tinder.dto.ApiResponse;
import com.example.tinder.dto.ProfilePictureDto;
import com.example.tinder.entity.ProfilePicture;
import com.example.tinder.entity.User;
import com.example.tinder.repository.ProfilePictureRepository;
import com.example.tinder.repository.UserRepository;
import com.example.tinder.service.ProfilePictureService;
import com.example.tinder.service.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProfilePictureServiceImpl implements ProfilePictureService {
    private final UserRepository userRepository;
    private final S3Service s3Service;
    private final ProfilePictureRepository profilePictureRepository;

    @Override
    public ApiResponse uploadPicture(User user, MultipartFile file) {
        try {
            String cloudPath = s3Service.uploadFile(file);

            ProfilePicture picture = new ProfilePicture();
            picture.setCloudPath(cloudPath);
            picture.setUser(user);

            if (user.getProfilePictures() == null) {
                user.setProfilePictures(new ArrayList<>());
            }
            user.getProfilePictures().add(picture);

            if (user.getProfilePic() == null) {
                user.setProfilePic(cloudPath); // main picture sifatida
            }

            profilePictureRepository.save(picture);
            userRepository.save(user);

            return ApiResponse.builder()
                    .status(HttpStatus.OK)
                    .message("Rasm muvaffaqiyatli yuklandi")
                    .build();

        } catch (Exception e) {
            throw new RuntimeException("Rasm yuklashda xatolik: " + e.getMessage());
        }
    }



    @Override
    public ApiResponse getUserPictures(Long userId) {
        List<ProfilePicture> pictures = profilePictureRepository.findAllByUserId(userId);
        List<ProfilePictureDto> dtoList = pictures.stream()
                .map(p -> ProfilePictureDto.builder()
                        .id(p.getId())
                        .cloudPath(p.getCloudPath())
                        .build())
                .toList();
        return ApiResponse.builder()
                .status(HttpStatus.OK)
                .data(dtoList)
                .build();
    }

    @Override
    public ApiResponse deletePicture(Long pictureId, Long userId) {
        Optional<ProfilePicture> optionalPicture = profilePictureRepository.findById(pictureId);

        if (optionalPicture.isEmpty()) {
            return ApiResponse.builder()
                    .status(HttpStatus.NOT_FOUND)
                    .message("Profile picture not found")
                    .build();
        }

        ProfilePicture picture = optionalPicture.get();

        if (!picture.getUser().getId().equals(userId)) {
            return ApiResponse.builder()
                    .status(HttpStatus.FORBIDDEN)
                    .message("You are not allowed to delete this picture")
                    .build();
        }

        User user = picture.getUser();
        if (picture.getCloudPath().equals(user.getProfilePic())) {
            user.setProfilePic(null);
            userRepository.save(user);
        }

        profilePictureRepository.delete(picture);

        return ApiResponse.builder()
                .status(HttpStatus.OK)
                .message("Profile picture deleted successfully")
                .build();
    }



    @Override
    public ApiResponse setAsMainPicture(Long userId, Long pictureId) {
        Optional<User> optionalUser = userRepository.findById(userId);
        if (optionalUser.isEmpty()) {
            return ApiResponse.builder()
                    .status(HttpStatus.NOT_FOUND)
                    .message("User not found")
                    .build();
        }

        Optional<ProfilePicture> optionalPicture = profilePictureRepository.findById(pictureId);
        if (optionalPicture.isEmpty()) {
            return ApiResponse.builder()
                    .status(HttpStatus.NOT_FOUND)
                    .message("Picture not found")
                    .build();
        }

        User user = optionalUser.get();
        ProfilePicture profilePicture = optionalPicture.get();

        if (!profilePicture.getUser().getId().equals(userId)) {
            return ApiResponse.builder()
                    .status(HttpStatus.BAD_REQUEST)
                    .message("Picture does not belong to this user")
                    .build();
        }

        user.setProfilePic(profilePicture.getCloudPath());
        userRepository.save(user);

        return ApiResponse.builder()
                .status(HttpStatus.OK)
                .message("Main picture updated successfully")
                .data(profilePicture.getCloudPath())
                .build();
    }


}

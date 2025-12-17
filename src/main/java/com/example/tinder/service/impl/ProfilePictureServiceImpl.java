package com.example.tinder.service.impl;

import com.example.tinder.dto.ApiResponse;
import com.example.tinder.dto.ProfilePictureDto;
import com.example.tinder.entity.ProfilePicture;
import com.example.tinder.entity.User;
import com.example.tinder.enums.ErrorType;
import com.example.tinder.exception.ResourceNotFoundException;
import com.example.tinder.exception.RestException;
import com.example.tinder.repository.ProfilePictureRepository;
import com.example.tinder.repository.UserRepository;
import com.example.tinder.service.ProfilePictureService;
import com.example.tinder.service.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
    @Transactional
    public ApiResponse<?> uploadPicture(User user, MultipartFile file) {
        if (!user.isActive()) {
            throw RestException.restThrow(ErrorType.USER_INACTIVE);
        }
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
                user.setProfilePic(cloudPath);
            }

            profilePictureRepository.save(picture);
            userRepository.save(user);

            return ApiResponse.success("Rasm muvaffaqiyatli yuklandi");

        } catch (Exception e) {
            throw new RuntimeException("Rasm yuklashda xatolik: " + e.getMessage());
        }
    }

    @Override
    public ApiResponse<?> getUserPictures(Long userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty() || !userOpt.get().isActive()) {
            throw RestException.restThrow(ErrorType.USER_INACTIVE);
        }
        List<ProfilePicture> pictures = profilePictureRepository.findAllByUserId(userId);

        List<ProfilePictureDto> dtoList = pictures.stream()
                .map(p -> ProfilePictureDto.builder()
                        .id(p.getId())
                        .cloudPath(p.getCloudPath())
                        .build())
                .toList();
        return ApiResponse.success(dtoList, "SUCCESS");

    }

    @Override
    @Transactional
    public ApiResponse<?> deletePicture(Long pictureId, Long userId) {
        Optional<User> optionalUser = userRepository.findById(userId);
        if (optionalUser.isEmpty()) {
            throw RestException.restThrow(ErrorType.USER_NOT_FOUND);
        }
        User user = optionalUser.get();
        if (!user.isActive()) {
            throw RestException.restThrow(ErrorType.USER_INACTIVE);

        }

        Optional<ProfilePicture> optionalPicture = profilePictureRepository.findById(pictureId);
        if (optionalPicture.isEmpty()) {
            throw RestException.restThrow(ErrorType.PROFILE_PICTURE_NOT_FOUND);
        }
        ProfilePicture picture = optionalPicture.get();

        if (!picture.getUser().getId().equals(userId)) {
            throw RestException.restThrow(ErrorType.CANNOT_DELETE_OTHER_USERS_PICTURE);

        }

        // Agar o'chirilayotgan rasm foydalanuvchining asosiy rasmi bo'lsa, asosiy rasmni null qilish
        if (user.getProfilePic() != null && picture.getCloudPath().equals(user.getProfilePic())) {
            user.setProfilePic(null);
            userRepository.save(user);
        }

        // Foydalanuvchining rasmlar ro'yxatidan ham olib tashlash
        if (user.getProfilePictures() != null) {
            user.getProfilePictures().removeIf(p -> p.getId().equals(picture.getId()));
            userRepository.save(user); // O'zgarishlarni saqlash
        }

        // S3 dan faylni o'chirish
        try {
            s3Service.deleteFile(picture.getCloudPath());
        } catch (RuntimeException e) {
            // S3 dan o'chirishda xato bo'lsa ham, DB yozuvini o'chirishni davom ettirish
            // Yoki bu yerda tranzaksiyani bekor qilishni tanlashingiz mumkin
            // Hozircha, faqat logga yozamiz va davom etamiz
            System.err.println("S3 dan rasmni o'chirishda xato: " + e.getMessage());
        }

        // Ma'lumotlar bazasidan rasmni o'chirish
        profilePictureRepository.delete(picture);

        return ApiResponse.success("PICTURE DELETED SUCCESSFULLY");

    }

    @Override
    @Transactional
    public ApiResponse<?> setAsMainPicture(Long userId, Long pictureId) {
        Optional<User> optionalUser = userRepository.findById(userId);
        if (optionalUser.isEmpty()) {
            throw RestException.restThrow(ErrorType.USER_NOT_FOUND);
        }
        User user = optionalUser.get();

        Optional<ProfilePicture> optionalPicture = profilePictureRepository.findById(pictureId);
        if (optionalPicture.isEmpty()) {
            throw RestException.restThrow(ErrorType.PICTURE_NOT_FOUND);
        }
        ProfilePicture profilePicture = optionalPicture.get();

        if (!user.isActive()) {
            throw RestException.restThrow(ErrorType.USER_INACTIVE);
        }
        if (!profilePicture.getUser().getId().equals(userId)) {
            throw RestException.restThrow(ErrorType.CANNOT_SET_OTHER_USERS_PICTURE);

        }
        user.setProfilePic(profilePicture.getCloudPath());
        userRepository.save(user);
        return ApiResponse.success(profilePicture.getCloudPath() ,"Asosiy rasm muvaffaqiyatli o'zgartirildi.");
    }
}

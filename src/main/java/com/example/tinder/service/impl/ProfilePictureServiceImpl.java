package com.example.tinder.service.impl;

import com.example.tinder.dto.base.ApiResponse;
import com.example.tinder.dto.ProfilePictureDto;
import com.example.tinder.entity.ProfilePicture;
import com.example.tinder.entity.User;
import com.example.tinder.enums.ErrorType;
import com.example.tinder.exception.RestException;
import com.example.tinder.repository.ProfilePictureRepository;
import com.example.tinder.repository.UserRepository;
import com.example.tinder.service.ProfilePictureService;
import com.example.tinder.service.S3Service;
import lombok.RequiredArgsConstructor;
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
    public ApiResponse<?> uploadPictures(User user, List<MultipartFile> files) {
        if (!user.isActive()) {
            throw RestException.restThrow(ErrorType.USER_INACTIVE);
        }

        if (files == null || files.isEmpty()) {
            throw new RuntimeException("Hech qanday rasm tanlanmagan");
        }

        try {
            // 1. S3 ga barcha rasmlarni list sifatida yuklash
            List<String> cloudPaths = s3Service.uploadFiles(files);

            List<ProfilePicture> newPictures = new ArrayList<>();

            for (String path : cloudPaths) {
                ProfilePicture picture = new ProfilePicture();
                picture.setCloudPath(path);
                picture.setUser(user);
                newPictures.add(picture);

                // Agar foydalanuvchining asosiy rasmi hali yo'q bo'lsa, birinchisini o'rnatamiz
                if (user.getProfilePic() == null) {
                    user.setProfilePic(path);
                }
            }

            // 2. Bazaga yangi ob'ektlarni saqlash
            profilePictureRepository.saveAll(newPictures);

            // 3. Userning profilePictures listini yangilash
            if (user.getProfilePictures() == null) {
                user.setProfilePictures(new ArrayList<>());
            }
            user.getProfilePictures().addAll(newPictures);
            userRepository.save(user);

            return ApiResponse.success("Rasmlar muvaffaqiyatli yuklandi. Soni: " + cloudPaths.size());

        } catch (Exception e) {
            throw new RuntimeException("Rasmlarni yuklashda xatolik: " + e.getMessage());
        }
    }

    // Qolgan metodlar (getUserPictures, deletePicture, setAsMainPicture) o'zgarishsiz qoladi
    // Chunki ular bitta ID bilan ishlashda davom etadi.

    @Override
    public ApiResponse<?> getUserPictures(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> RestException.restThrow(ErrorType.USER_NOT_FOUND));

        if (!user.isActive()) throw RestException.restThrow(ErrorType.USER_INACTIVE);

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
        User user = userRepository.findById(userId)
                .orElseThrow(() -> RestException.restThrow(ErrorType.USER_NOT_FOUND));

        ProfilePicture picture = profilePictureRepository.findById(pictureId)
                .orElseThrow(() -> RestException.restThrow(ErrorType.PROFILE_PICTURE_NOT_FOUND));

        if (!picture.getUser().getId().equals(userId)) {
            throw RestException.restThrow(ErrorType.CANNOT_DELETE_OTHER_USERS_PICTURE);
        }

        // Asosiy rasm bo'lsa, uni null qilish
        if (picture.getCloudPath().equals(user.getProfilePic())) {
            user.setProfilePic(null);
        }

        // Listdan o'chirish
        user.getProfilePictures().removeIf(p -> p.getId().equals(pictureId));
        userRepository.save(user);

        // S3 dan o'chirish
        s3Service.deleteFile(picture.getCloudPath());

        // DB dan o'chirish
        profilePictureRepository.delete(picture);

        return ApiResponse.success("PICTURE DELETED SUCCESSFULLY");
    }

    @Override
    @Transactional
    public ApiResponse<?> setAsMainPicture(Long userId, Long pictureId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> RestException.restThrow(ErrorType.USER_NOT_FOUND));

        ProfilePicture profilePicture = profilePictureRepository.findById(pictureId)
                .orElseThrow(() -> RestException.restThrow(ErrorType.PICTURE_NOT_FOUND));

        if (!profilePicture.getUser().getId().equals(userId)) {
            throw RestException.restThrow(ErrorType.CANNOT_SET_OTHER_USERS_PICTURE);
        }

        user.setProfilePic(profilePicture.getCloudPath());
        userRepository.save(user);
        return ApiResponse.success(profilePicture.getCloudPath() ,"Asosiy rasm o'zgartirildi.");
    }
}

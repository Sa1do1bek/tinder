package com.example.tinder.service.impl;

import com.example.tinder.dto.ApproveImageDto;
import com.example.tinder.dto.VerificationDto;
import com.example.tinder.dto.base.ApiResponse;
import com.example.tinder.entity.ApproveImage;
import com.example.tinder.entity.User;
import com.example.tinder.entity.Verification;
import com.example.tinder.enums.ErrorType;
import com.example.tinder.exception.RestException;
import com.example.tinder.mapper.ApproveImageMapper;
import com.example.tinder.mapper.VerificationMapper;
import com.example.tinder.repository.ApproveImageRepository;
import com.example.tinder.repository.UserRepository;
import com.example.tinder.repository.VerificationRepository;
import com.example.tinder.service.S3Service;
import com.example.tinder.service.VerificationService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class VerificationServiceImpl implements VerificationService {
    private final VerificationRepository verificationRepository;
    private final VerificationMapper verificationMapper;
    private final UserRepository userRepository;
    private final S3Service s3Service;
    private final ApproveImageRepository approveImageRepository;
    private final ApproveImageMapper approveImageMapper;


    @Override
    public ApiResponse<?> update(VerificationDto.VerificationUpdateDto dto, User currentuser) {

        User user = userRepository.findById(currentuser.getId()).orElse(null);
        if (user == null) throw RestException.restThrow(ErrorType.USER_NOT_FOUND);
        if (user.getIsApproved()) throw RestException.restThrow(ErrorType.USER_ALREADY_APPROVED);

        Verification verification = verificationRepository.findByUserId(user.getId()).orElse(null);
        if (verification == null) throw RestException.restThrow(ErrorType.VERIFICATION_NOT_FOUND);


        Verification newVerification = verificationMapper.fromUpdate(dto, user, verification.getId());
        if (newVerification.getSimilarityScore() >= 80.0) {
            user.setIsApproved(true);
        }
        Verification save = verificationRepository.save(newVerification);

        return ApiResponse.success(verificationMapper.toDto(save), "Verification updated successfully");
    }

    @Override
    public ApiResponse<?> get(User currentUser) {
        User user = userRepository.findById(currentUser.getId()).orElse(null);
        if (user == null) throw RestException.restThrow(ErrorType.USER_NOT_FOUND);

        Verification verification = verificationRepository.findByUserId(user.getId()).orElse(null);
        if (verification == null) throw RestException.restThrow(ErrorType.VERIFICATION_NOT_FOUND);

        return ApiResponse.success(verificationMapper.toDto(verification), "Verification fetched successfully");
    }


    @Override
    @Transactional
    public ApiResponse<?> uploadApproveImage(User user, List<MultipartFile> files) {
        if (!user.isActive()) {
            throw RestException.restThrow(ErrorType.USER_INACTIVE);
        }

        if (files == null || files.isEmpty()) {
            throw new RuntimeException("Hech qanday rasm tanlanmagan");
        }

        try {
            List<String> cloudPaths = s3Service.uploadFiles(files);

            List<ApproveImage> approveImages = new ArrayList<>();

            for (String path : cloudPaths) {
                ApproveImage approve = new ApproveImage();
                approve.setCloudPath(path);
                approve.setUser(user);
                approveImages.add(approve);

                if (user.getProfilePic() == null) {
                    user.setProfilePic(path);
                }
            }

            List<ApproveImage> list = approveImageRepository.saveAll(approveImages);
            List<ApproveImageDto> listDto = approveImageMapper.toListDto(list);
            return ApiResponse.success(listDto, "Rasmlar muvaffaqiyatli yuklandi. Soni: " + cloudPaths.size());

        } catch (Exception e) {
            throw new RuntimeException("Rasmlarni yuklashda xatolik: " + e.getMessage());
        }
    }
}

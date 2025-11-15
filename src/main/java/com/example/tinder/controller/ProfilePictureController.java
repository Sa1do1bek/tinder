package com.example.tinder.controller;

import com.example.tinder.annotation.LogUserAction;
import com.example.tinder.dto.ApiResponse;
import com.example.tinder.entity.User;
import com.example.tinder.enums.UserAction;
import com.example.tinder.service.ProfilePictureService;
import com.example.tinder.service.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourceRegion;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/picture")
@RequiredArgsConstructor
public class ProfilePictureController {
   private final ProfilePictureService profilePictureService;
    private final S3Service s3Service;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("isAuthenticated()")
    @LogUserAction(action = UserAction.UPDATE_PROFILE)
    public ApiResponse upload(@RequestParam MultipartFile file,
                              @AuthenticationPrincipal User user) {
        return profilePictureService.uploadPicture(user, file);
    }

    @GetMapping("/user")
    public ApiResponse getAllPictures(@AuthenticationPrincipal User user) {
        return profilePictureService.getUserPictures(user.getId());
    }

    @DeleteMapping("/{id}")
    public ApiResponse delete(@PathVariable Long id,@AuthenticationPrincipal User user) {
        return profilePictureService.deletePicture(id,user.getId());
    }

    @PutMapping("/set-main")
    public ApiResponse setMain(@AuthenticationPrincipal User user, @RequestParam Long pictureId) {
        return profilePictureService.setAsMainPicture(user.getId(), pictureId);
    }
}

package com.example.tinder.controller;

import com.example.tinder.dto.base.ApiResponse;
import com.example.tinder.entity.User;
import com.example.tinder.service.ProfilePictureService;
import com.example.tinder.utils.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/picture")
@RequiredArgsConstructor
public class ProfilePictureController {
    private final ProfilePictureService profilePictureService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<?> upload(@RequestParam MultipartFile file,
                              @CurrentUser User user) {
        return profilePictureService.uploadPicture(user, file);
    }

    @GetMapping("/user")
    public ApiResponse<?> getAllPictures(@CurrentUser User user) {
        return profilePictureService.getUserPictures(user.getId());
    }

    @DeleteMapping("/{id}")
    public ApiResponse<?> delete(@PathVariable Long id,@CurrentUser User user) {
        return profilePictureService.deletePicture(id,user.getId());
    }

    @PutMapping("/set-main")
    public ApiResponse<?> setMain(@CurrentUser User user, @RequestParam Long pictureId) {
        return profilePictureService.setAsMainPicture(user.getId(), pictureId);
    }
}

package com.example.tinder.controller;

import com.example.tinder.dto.ApiResponse;
import com.example.tinder.service.ProfilePictureService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/picture")
@RequiredArgsConstructor
public class ProfilePictureController {
   private final ProfilePictureService profilePictureService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse upload(@RequestParam Long userId,
                              @RequestParam MultipartFile file) {
        return profilePictureService.uploadPicture(userId, file);
    }


    @GetMapping("/user")
    public ApiResponse getAllPictures(@RequestParam Long userId) {
        return profilePictureService.getUserPictures(userId);
    }

    @DeleteMapping("/{id}")
    public ApiResponse delete(@PathVariable Long id) {
        return profilePictureService.deletePicture(id);
    }

    @PutMapping("/set-main")
    public ApiResponse setMain(@RequestParam Long userId, @RequestParam Long pictureId) {
        return profilePictureService.setAsMainPicture(userId, pictureId);
    }
}

package com.example.tinder.controller;

import com.example.tinder.dto.ApiResponse;
import com.example.tinder.service.ProfilePictureService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/picture")
@RequiredArgsConstructor
public class ProfilePictureController {
   private final ProfilePictureService profilePictureService;

   @PostMapping("/upload")
    public ApiResponse upload(@RequestParam Long userId, @RequestParam MultipartFile file) {
       return profilePictureService.uploadPicture(userId,file);
   }
}

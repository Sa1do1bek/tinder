package com.example.tinder.service;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public interface S3Service {
    String uploadFile(MultipartFile file);
    String uploadFile(byte[] bytes);
}

package com.example.tinder.service;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Component
public interface S3Service {
    String uploadFile(MultipartFile file);
    List<String> uploadFiles(List<MultipartFile> files);
    void deleteFile(String fileUrl);
}

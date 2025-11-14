package com.example.tinder.service;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Component
public interface S3Service {
    void deleteFile(String fileUrl);
    String uploadFile(MultipartFile file);
    Map<String, Object> streamVideoChunk(String objectName, HttpHeaders headers);
    String uploadFile(byte[] bytes);

    ResponseEntity<Resource> loadVideoResource(String cloudPath, String rangeHeader);

    ResponseEntity<byte[]> loadVideoChunkBytes(String cloudPath, String first);

}

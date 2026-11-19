package com.example.tinder.service.impl;

import com.example.tinder.service.S3Service;
import io.minio.*;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.multipart.MultipartFile;
import java.io.*;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3ServiceImpl implements S3Service {

    @Value("${app.storage.bucket}")
    private String bucketName;
    @Value("${app.storage.endpoint}")
    private String endpoint;
    @Value("${app.storage.access-key}")
    private String accessKey;
    @Value("${app.storage.secret-key}")
    private String secretKey;
    private MinioClient minioClient;

    @PostConstruct
    public void init() {
        minioClient = MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .build();
        try {
            boolean found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
            if (!found) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
            }

            String policy = """
                    {
                      "Version": "2012-10-17",
                      "Statement": [
                        {
                          "Effect": "Allow",
                          "Principal": "*",
                          "Action": ["s3:GetObject"],
                          "Resource": ["arn:aws:s3:::%s/*"]
                        }
                      ]
                    }
                    """.formatted(bucketName);
            minioClient.setBucketPolicy(
                    SetBucketPolicyArgs.builder()
                            .bucket(bucketName)
                            .config(policy)
                            .build()
            );
            log.info("MinIO bucket '{}' is ready and public.", bucketName);
        } catch (Exception e) {
            log.error("MinIO initialization error: {}", e.getMessage());
        }
    }

    @Override
    public String uploadFile(MultipartFile file) {
        try {
            String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(fileName)
                            .stream(file.getInputStream(), file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );
            return endpoint + "/" + bucketName + "/" + fileName;
        } catch (Exception e) {
            log.error("MinIO upload failed: {}", e.getMessage());
            throw new RuntimeException("Fayl yuklashda xatolik: " + e.getMessage());
        }
    }

    @Override
    public List<String> uploadFiles(List<MultipartFile> files) {
        if (files == null || files.isEmpty()) {
            return Collections.emptyList();
        }

        List<String> uploadedUrls = new ArrayList<>();
        try {
            for (MultipartFile file : files) {
                if (file != null && !file.isEmpty()) {
                    uploadedUrls.add(uploadFile(file));
                }
            }
            return uploadedUrls;
        } catch (Exception e) {
            log.error("Multiple upload failed, rolling back... Error: {}", e.getMessage());
            uploadedUrls.forEach(this::deleteFile);
            throw new RuntimeException("Rasmlar guruhini yuklashda xatolik yuz berdi.");
        }
    }

    // --- FAYLNI O'CHIRISH ---
    @Override
    public void deleteFile(String fileUrl) {
        if (fileUrl == null || !fileUrl.contains("/")) return;
        try {
            String fileName = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(fileName)
                            .build()
            );
            log.info("File deleted: {}", fileName);
        } catch (Exception e) {
            log.error("Error deleting file: {}", e.getMessage());
        }
    }

    private String detectMimeType(byte[] bytes) {
        try (InputStream is = new ByteArrayInputStream(bytes)) {
            String mimeType = URLConnection.guessContentTypeFromStream(is);
            return mimeType != null ? mimeType : "image/jpeg";
        } catch (IOException e) {
            return "application/octet-stream";
        }
    }
}

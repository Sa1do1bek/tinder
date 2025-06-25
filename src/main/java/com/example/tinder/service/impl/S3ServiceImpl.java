package com.example.tinder.service.impl;

import com.example.tinder.service.S3Service;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.SetBucketPolicyArgs;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.net.URLConnection;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class S3ServiceImpl implements S3Service {

    private final String bucketName = "s3-tenzorsoft";
    private final String endpoint = "https://s3.tenzorsoft.uz";
    private final String accessKey = "REMOVED";
    private final String secretKey = "REMOVED";

    private MinioClient minioClient;

    @PostConstruct
    public void init() {
        minioClient = MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .build();

        try {
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
            System.out.println("MinIO bucket made public.");
        } catch (Exception e) {
            System.err.println("MinIO policy error: " + e.getMessage());
        }
    }

    @Override
    public String uploadFile(MultipartFile file) {

        try {
            String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
            File tempFile = File.createTempFile("upload-", ".tmp");

            try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                fos.write(file.getBytes());
            }

            try (InputStream is = new FileInputStream(tempFile)) {
                minioClient.putObject(
                        PutObjectArgs.builder()
                                .bucket(bucketName)
                                .object(fileName)
                                .stream(is, tempFile.length(), -1)
                                .contentType(file.getContentType())
                                .build()
                );
            }

            return endpoint + "/" + bucketName + "/" + fileName;

        } catch (Exception e) {
            throw new RuntimeException("MinIO upload failed: " + e.getMessage());
        }
    }

    @Override
    public String uploadFile(byte[] bytes) {
        try {
            String fileName = UUID.randomUUID() + ".jpg";
            String mimeType = detectMimeType(bytes);

            File tempFile = File.createTempFile("upload-", ".tmp");
            try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                fos.write(bytes);
            }

            try (InputStream is = new FileInputStream(tempFile)) {
                minioClient.putObject(
                        PutObjectArgs.builder()
                                .bucket(bucketName)
                                .object(fileName)
                                .stream(is, tempFile.length(), -1)
                                .contentType(mimeType)
                                .build()
                );
            }

            return endpoint + "/" + bucketName + "/" + fileName;

        } catch (Exception e) {
            throw new RuntimeException("MinIO upload failed: " + e.getMessage());
        }
    }

    private String detectMimeType(byte[] bytes) {
        try (InputStream is = new ByteArrayInputStream(bytes)) {
            return URLConnection.guessContentTypeFromStream(is);
        } catch (IOException e) {
            return "application/octet-stream";
        }
    }
}

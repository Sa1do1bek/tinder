package com.example.tinder.service.impl;

import com.example.tinder.service.S3Service;
import io.minio.*;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourceRegion;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.*;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3ServiceImpl implements S3Service {
        private final String bucketName = "tinder";
        private final String endpoint = "http://localhost:9000";
        private final String accessKey = "minioadmin";
        private final String secretKey = "minio_strong_password";
        private MinioClient minioClient;

    @PostConstruct
    public void init() {
        minioClient = MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .build();
        try {
            boolean found = minioClient.bucketExists(io.minio.BucketExistsArgs.builder().bucket(bucketName).build());
            if (!found) {
                minioClient.makeBucket(io.minio.MakeBucketArgs.builder().bucket(bucketName).build());
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
            System.out.println("MinIO bucket made public.");
        } catch (Exception e) {
            System.err.println("MinIO policy error or bucket creation error: " + e.getMessage());
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
            throw new RuntimeException("MinIO upload failed: " + e.getMessage());
        }
    }

    @Override
    public String uploadFile(byte[] bytes) {
        try {
            String fileName = UUID.randomUUID() + ".jpg";
            String mimeType = detectMimeType(bytes);

            try (InputStream is = new ByteArrayInputStream(bytes)) {
                minioClient.putObject(
                        PutObjectArgs.builder()
                                .bucket(bucketName)
                                .object(fileName)
                                .stream(is, bytes.length, -1)
                                .contentType(mimeType)
                                .build()
                );
            }
            return endpoint + "/" + bucketName + "/" + fileName;
        } catch (Exception e) {
            throw new RuntimeException("MinIO upload failed: " + e.getMessage());
        }
    }

    @Override
    public ResponseEntity<Resource> loadVideoResource(String cloudPath, String rangeHeader) {
        try {
            StatObjectResponse stat = getObjectStat(cloudPath);
            long totalLength = stat.size();

            // Standart bo'lak hajmi (1MB)
            long partSize = 1024 * 1024;

            // Range tahlili
            long rangeStart = 0;
            long rangeEnd = totalLength - 1;

            // Agar Range Header mavjud bo'lsa, uni tahlil qilamiz
            if (rangeHeader != null) {
                // ... (Avvalgi Range Header tahlili logikasi) ...
                String rangeValue = rangeHeader.replace("bytes=", "");
                String[] ranges = rangeValue.split("-");

                rangeStart = Long.parseLong(ranges[0]);

                if (ranges.length > 1 && !ranges[1].isEmpty()) {
                    rangeEnd = Long.parseLong(ranges[1]);
                }

                // Agar tugash diapazoni juda katta bo'lsa, uni chegara bilan cheklaymiz
                rangeEnd = Math.min(rangeEnd, rangeStart + partSize - 1);
                rangeEnd = Math.min(rangeEnd, totalLength - 1);
            }

            long requestedLength = rangeEnd - rangeStart + 1;

            // MinIO dan aniq so'ralgan diapazondagi ma'lumotni olish
            byte[] content;
            try (InputStream stream = minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucketName)
                            .object(cloudPath)
                            .offset(rangeStart)
                            .length(requestedLength)
                            .build()
            )) {
                // ByteArrayResource ishlatish uchun streamni o'qish
                content = stream.readAllBytes();
            }

            // Response'ni yaratish
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(stat.contentType()));

            if (rangeHeader != null) {
                // Agar Range Header so'ralgan bo'lsa, 206 PARTIAL_CONTENT ni qaytaramiz
                headers.add(HttpHeaders.CONTENT_RANGE,
                        String.format("bytes %d-%d/%d", rangeStart, rangeStart + content.length - 1, totalLength));

                headers.add(HttpHeaders.CONTENT_LENGTH, String.valueOf(content.length));
                headers.add(HttpHeaders.ACCEPT_RANGES, "bytes");

                return ResponseEntity.status(org.springframework.http.HttpStatus.PARTIAL_CONTENT)
                        .headers(headers)
                        .body(new ByteArrayResource(content));
            } else {
                // Agar Range Header bo'lmasa, to'liq faylni qaytaramiz (kamdan kam holat)
                headers.add(HttpHeaders.CONTENT_LENGTH, String.valueOf(totalLength));

                return ResponseEntity.ok()
                        .headers(headers)
                        .body(new ByteArrayResource(content)); // Bu yerda butun fayl xotiraga yuklangan bo'lishi kerak
            }
        }catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public ResponseEntity<byte[]> loadVideoChunkBytes(String cloudPath, String first) {
        try {

            StatObjectResponse stat = getObjectStat(cloudPath);
            long totalLength = stat.size();
            long partSize = 1024 * 1024; // 1MB bo'lak hajmi

            // Range tahlili
            long rangeStart = 0;
            long rangeEnd = totalLength - 1;

            if (first != null) {
                String rangeValue = first.replace("bytes=", "");
                String[] ranges = rangeValue.split("-");

                rangeStart = Long.parseLong(ranges[0]);

                if (ranges.length > 1 && !ranges[1].isEmpty()) {
                    rangeEnd = Long.parseLong(ranges[1]);
                }

                // Agar tugash diapazoni juda katta bo'lsa, uni chegara bilan cheklaymiz
                rangeEnd = Math.min(rangeEnd, rangeStart + partSize - 1);
                rangeEnd = Math.min(rangeEnd, totalLength - 1);
            }

            long requestedLength = rangeEnd - rangeStart + 1;

            // MinIO dan aniq so'ralgan diapazondagi ma'lumotni olish
            byte[] content;
            try (InputStream stream = minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucketName)
                            .object(cloudPath)
                            .offset(rangeStart)
                            .length(requestedLength)
                            .build()
            )) {
                content = stream.readAllBytes();
            }

            // Response'ni yaratish
            HttpHeaders headers = new HttpHeaders();

            // 🔥 Content-Type ni MinIO stat orqali olishni kafolatlash
            String minioContentType = stat.contentType() != null ? stat.contentType() : "video/mp4";
            headers.setContentType(MediaType.parseMediaType(minioContentType));

            // Barcha zarur streaming headerlarini qo'shish
            headers.add(HttpHeaders.CONTENT_LENGTH, String.valueOf(content.length));
            headers.add(HttpHeaders.ACCEPT_RANGES, "bytes");

            // CONTROLLERda ishlashdan oldingi xatolar sabab bo'lgan Content-Range ni majburiy qo'shish
            headers.add(HttpHeaders.CONTENT_RANGE,
                    String.format("bytes %d-%d/%d", rangeStart, rangeStart + content.length - 1, totalLength));


            return ResponseEntity.status(org.springframework.http.HttpStatus.PARTIAL_CONTENT)
                    .headers(headers)
                    .body(content);
        }catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public Map<String, Object> streamVideoChunk(String objectName, HttpHeaders headers) {
        try {
            // 1. Fayl metama'lumotlarini olish
            StatObjectResponse stat = getObjectStat(objectName);
            long totalLength = stat.size();

            // 2. Range Header tahlili va diapazonni hisoblash
            long rangeStart = 0;
            long rangeEnd = totalLength - 1;
            long partSize = 1024 * 1024; // 1MB bo'lak hajmi

            Optional<String> rangeHeader = Optional.ofNullable(headers.getFirst(HttpHeaders.RANGE));

            if (rangeHeader.isPresent()) {
                String rangeValue = rangeHeader.get().replace("bytes=", "");
                String[] ranges = rangeValue.split("-");
                rangeStart = Long.parseLong(ranges[0]);

                if (ranges.length > 1 && !ranges[1].isEmpty()) {
                    rangeEnd = Long.parseLong(ranges[1]);
                }
            }

            // So'ralgan uzunlikni aniqlash
            long requestedLength = Math.min(partSize, rangeEnd - rangeStart + 1);
            if (rangeStart + requestedLength > totalLength) {
                requestedLength = totalLength - rangeStart;
            }

            // 3. MinIO'dan faqat so'ralgan diapazondagi ma'lumotni olish (ByteArrayResource uchun)
            byte[] content;
            try (InputStream stream = minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .offset(rangeStart)
                            .length(requestedLength)
                            .build()
            )) {
                // Streamni xotiraga o'qish - IllegalStateException xatosini yechimi
                content = stream.readAllBytes();
            }

            // 4. Resurs va Regionni yaratish
            Resource resource = new ByteArrayResource(content);

            // ResourceRegion uchun offset 0, chunki bizning "resource"imiz faqat bo'lakni o'z ichiga oladi
            ResourceRegion region = new ResourceRegion(resource, 0, content.length);

            // 5. Natijalarni to'plab qaytarish
            Map<String, Object> result = new HashMap<>();
            result.put("region", region);
            result.put("contentType", MediaType.parseMediaType(stat.contentType()));
            result.put("start", rangeStart);
            result.put("end", rangeStart + content.length - 1);
            result.put("total", totalLength);

            return result;
        }catch (Exception e) {
            e.printStackTrace();
            return Map.of();
        }
    }

    private StatObjectResponse getObjectStat(String objectName) throws Exception {
        return minioClient.statObject(
                StatObjectArgs.builder()
                        .bucket(bucketName)
                        .object(objectName)
                        .build()
        );
    }

    @Override
    public void deleteFile(String fileUrl) {
        try {
            String fileName = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(fileName)
                            .build()
            );
            log.info("File deleted from MinIO: {}", fileName);
        } catch (Exception e) {
            log.error("Error deleting file from MinIO: {}", e.getMessage());
            throw new RuntimeException("Faylni o'chirishda xatolik yuz berdi: " + e.getMessage());
        }
    }

    private String detectMimeType(byte[] bytes) {
        try (InputStream is = new ByteArrayInputStream(bytes)) {
            String mimeType = URLConnection.guessContentTypeFromStream(is);
            return mimeType != null ? mimeType : "application/octet-stream";
        } catch (IOException e) {
            return "application/octet-stream";
        }
    }
}

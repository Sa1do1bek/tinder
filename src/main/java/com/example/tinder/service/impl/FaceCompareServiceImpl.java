package com.example.tinder.service.impl;

import com.example.tinder.service.FaceCompareService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.rekognition.RekognitionClient;
import software.amazon.awssdk.services.rekognition.model.CompareFacesRequest;
import software.amazon.awssdk.services.rekognition.model.CompareFacesResponse;
import software.amazon.awssdk.services.rekognition.model.Image;
import software.amazon.awssdk.services.rekognition.model.S3Object;

import java.net.URI;

@Service
@RequiredArgsConstructor
public class FaceCompareServiceImpl implements FaceCompareService {

    private final RekognitionClient rekognitionClient;

    @Override
    public double compare(String sourceImageUrl, String targetImageUrl) {

        Image source = Image.builder()
                .s3Object(S3Object.builder()
                        .bucket(extractBucket(sourceImageUrl))
                        .name(extractKey(sourceImageUrl))
                        .build())
                .build();

        Image target = Image.builder()
                .s3Object(S3Object.builder()
                        .bucket(extractBucket(targetImageUrl))
                        .name(extractKey(targetImageUrl))
                        .build())
                .build();

        CompareFacesRequest request = CompareFacesRequest.builder()
                .sourceImage(source)
                .targetImage(target)
                .similarityThreshold(0f)
                .build();

        CompareFacesResponse response =
                rekognitionClient.compareFaces(request);

        return response.faceMatches().isEmpty()
                ? 0.0
                : response.faceMatches().get(0).similarity();
    }

    private String extractBucket(String url) {
        return URI.create(url).getHost().split("\\.")[0];
    }

    private String extractKey(String url) {
        return URI.create(url).getPath().substring(1);
    }


}

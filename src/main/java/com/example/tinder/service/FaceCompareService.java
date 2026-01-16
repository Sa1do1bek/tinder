package com.example.tinder.service;

import org.springframework.stereotype.Service;

@Service
public interface FaceCompareService {
    double compareFaces(String profileImageUrl, String faceImageUrl);
}

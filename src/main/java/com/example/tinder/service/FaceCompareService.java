package com.example.tinder.service;

import org.springframework.stereotype.Service;

@Service
public interface FaceCompareService {
    double compare(String sourceImageUrl, String targetImageUrl);
}

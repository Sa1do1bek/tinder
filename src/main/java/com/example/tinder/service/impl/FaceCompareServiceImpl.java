package com.example.tinder.service.impl;

import com.example.tinder.dto.SimilarityResponse;
import com.example.tinder.service.FaceCompareService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;


@Service
@RequiredArgsConstructor
public class FaceCompareServiceImpl implements FaceCompareService {
    private final RestTemplate restTemplate;

    @Value("${open.ai.face-recognition}")
    private String AI_COMPARE_URL;

    @Override
    public double compareFaces(String imageUrl1, String imageUrl2) {

        UriComponentsBuilder uriBuilder = UriComponentsBuilder
                .fromHttpUrl(AI_COMPARE_URL)
                .queryParam("image_url_1", imageUrl1)
                .queryParam("image_url_2", imageUrl2);

        ResponseEntity<SimilarityResponse> response =
                restTemplate.exchange(
                        uriBuilder.toUriString(),
                        HttpMethod.GET,
                        null,
                        SimilarityResponse.class
                );

        SimilarityResponse body = response.getBody();

        if (body == null || body.getSimilarityPercent() == null) {
            throw new IllegalStateException("AI similarity response is empty");
        }

        return body.getSimilarityPercent();
    }

}

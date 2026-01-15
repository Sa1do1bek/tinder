package com.example.tinder.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.rekognition.RekognitionClient;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class AwsConfig {


    private final AwsProperties awsProperties;

    @Bean
    public RekognitionClient rekognitionClient() {

        AwsBasicCredentials credentials =
                AwsBasicCredentials.create(
                        awsProperties.getAccessKey(),
                        awsProperties.getSecretKey()
                );

        return RekognitionClient.builder()
                .region(Region.of(awsProperties.getRegion()))
                .credentialsProvider(
                        StaticCredentialsProvider.create(credentials)
                )
                .build();
    }

    @PostConstruct
    public void testAws() {
        log.info("AWS Rekognition initialized in region: {}", awsProperties.getRegion());
    }
}

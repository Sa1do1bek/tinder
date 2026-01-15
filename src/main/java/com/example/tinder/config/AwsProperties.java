package com.example.tinder.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "aws")
@EnableConfigurationProperties(AwsProperties.class)
@Data
public class AwsProperties {

    private String region;
    private String accessKey;
    private String secretKey;
}
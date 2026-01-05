package com.example.tinder.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;

@Slf4j
@Configuration
public class FirebaseConfig {

    @PostConstruct
    public void init() {
        try {
            if (!FirebaseApp.getApps().isEmpty()) {
                return;
            }

            ClassPathResource resource = new ClassPathResource("firebase-adminsdk.json");

            if (!resource.exists()) {
                throw new IllegalStateException("Firebase service account file not found");
            }

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(resource.getInputStream()))
                    .build();

            FirebaseApp app = FirebaseApp.initializeApp(options);

        } catch (IOException e) {
            throw new IllegalStateException("Firebase initialization failed", e);
        }
    }
}
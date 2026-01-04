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
                log.info("ℹ️ Firebase already initialized");
                log.info("   App name: {}", FirebaseApp.getInstance().getName());
                log.info("   Project ID: {}", FirebaseApp.getInstance().getOptions().getProjectId());
                return;
            }

            log.info("🔥 Initializing Firebase Admin SDK...");

            ClassPathResource resource = new ClassPathResource("firebase-adminsdk.json");

            if (!resource.exists()) {
                log.error("❌ firebase-adminsdk.json NOT FOUND in classpath!");
                throw new IllegalStateException("Firebase service account file not found");
            }

            log.info("✅ Service account file found");

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(resource.getInputStream()))
                    .build();

            FirebaseApp app = FirebaseApp.initializeApp(options);

            log.info("✅ Firebase initialized successfully!");
            log.info("   App name: {}", app.getName());
            log.info("   Project ID: {}", app.getOptions().getProjectId());

        } catch (IOException e) {
            log.error("🔥 Firebase initialization FAILED", e);
            throw new IllegalStateException("Firebase initialization failed", e);
        }
    }
}
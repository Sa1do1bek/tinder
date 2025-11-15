package com.example.tinder.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;

@Configuration
public class FirebaseConfig {

    private final String FIREBASE_CONFIG_PATH = "tinder-7cabb-firebase-adminsdk-fbsvc-675b35d245.json";

    @Bean
    public FirebaseApp initializeFirebase() throws IOException {

        ClassPathResource resource = new ClassPathResource(FIREBASE_CONFIG_PATH);

        FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(resource.getInputStream()))
                .build();

        if (FirebaseApp.getApps().isEmpty()) {
            return FirebaseApp.initializeApp(options);
        } else {
            return FirebaseApp.getInstance();
        }
    }

}

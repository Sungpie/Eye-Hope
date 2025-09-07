package com.newsapp.eyehope.api.config;

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
    public void initialize() {
        try {
            // Check if Firebase app is already initialized
            if (FirebaseApp.getApps().isEmpty()) {
                // Load Firebase service account credentials
                GoogleCredentials googleCredentials = GoogleCredentials
                        .fromStream(new ClassPathResource("firebase/eye-hope-firebase-adminsdk-fbsvc-ee4f3eeaf3.json").getInputStream());
                
                // Configure Firebase options
                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(googleCredentials)
                        .build();
                
                // Initialize Firebase
                FirebaseApp.initializeApp(options);
                log.info("Firebase application has been initialized successfully");
            } else {
                log.info("Firebase application is already initialized");
            }
        } catch (IOException e) {
            log.error("Error initializing Firebase application: {}", e.getMessage(), e);
            throw new RuntimeException("Firebase initialization failed", e);
        }
    }
}
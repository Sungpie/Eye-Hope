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
            // Check if the Firebase app is already initialized
            if (FirebaseApp.getApps().isEmpty()) {
                ClassPathResource resource = new ClassPathResource("firebase/eye-hope-firebase-adminsdk-fbsvc-ee4f3eeaf3.json");

                if (!resource.exists()) {
                    log.warn("Firebase credentials file not found. Firebase services will be disabled.");
                    return; // Skip initialization but don't crash the application
                }

                // Load Firebase service account credentials
                GoogleCredentials googleCredentials = GoogleCredentials
                        .fromStream(resource.getInputStream());

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
            log.error("Error initializing Firebase application: {}. Firebase services will be disabled.", e.getMessage());
            // Don't throw exception, allow application to start without Firebase
        }
    }
}

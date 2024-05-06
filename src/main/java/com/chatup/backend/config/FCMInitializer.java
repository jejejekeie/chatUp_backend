package com.chatup.backend.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import java.io.IOException;
import org.slf4j.Logger;
/*
@Service
public class FCMInitializer {
    private static final Logger logger = LoggerFactory.getLogger(FCMInitializer.class);

    @PostConstruct
    public void initialize() {
        logger.info("Starting Firebase initialization");
        try {
            String firebaseConfigJson =
            ByteArrayInputStream serviceAccount = new ByteArrayInputStream(firebaseConfigJson.getBytes(StandardCharsets.UTF_8));

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();

            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
                logger.info("Firebase application initialized");
            }
        } catch (IOException e) {
            logger.error("Failed to initialize Firebase", e);
        }
    }
}

 */

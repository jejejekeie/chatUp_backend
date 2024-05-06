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
            String firebaseConfigJson = "{\n" +
                    "  \"type\": \"service_account\", \n" +
                    "  \"project_id\": \"chatup-66815\",\n" +
                    "  \"private_key_id\": \"ce30cf1bb17b2402632ff64c5b8e0f8c3f35514a\",\n" +
                    "  \"private_key\": \"-----BEGIN PRIVATE KEY-----\\nMIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQDCH6k8VqWuDkLE\\nLKAWkTI2EMQQRgqDXQHNTmKdOzGyqnORZaTa36/0IBVa4BlDancdaqYwBoiX9Ril\\ntmxr5Pn1ped0DcubizpKBOvZvvLMeegDeKuQLHhuGmOZcOyfnrKtXDueOuFYD7JM\\nD2Xiu95NW6+gE+LIAHPrles7NuTg/EcgoFn0E6bxYMMKEnUOi8DREeqSMK4kg+KQ\\nyM5xf1E01d4cNJZdeEjjwNXpX2h5wCy+XLqpiO+nyzMj8idkdB9xd+k9gtkO8NOg\\n8ex9Ik5qN9Edjez1juncToltYQjc1zcHQ2fOLo+0pL29ZppuFrpTkU+5jLhvaNSy\\n0SDxivrfAgMBAAECggEALMgeA9/fN6BtkZDT23DiWuuSN0jZGwXFKSYIhMoHhHZi\\n06tVlPJeRlxyYLTqzw0L7nrQXKrdLuTpKy1CpL89VIhRtQmLq1W6fCHXgLNWkooa\\n1lYricZgf9HRoS+WPppfELQCwtVFb8mrMXWp5Ny/Ayx//tcnjUjhdPrwOYhH04io\\nSGcIgYAhYbeM16mx9mm5/fyCj44yiPaTyancpkfUfqvqCEsJTiY0sygrogEim5Lq\\nW3YkFVBsrky19dTpMHLOqV+rKNdY0gw4BB\", \n" +
                    "  \"client_email\": \"firebase-adminsdk-k7vzq@chatup-66815.iam.gserviceaccount.com\", \n" +
                    "  \"client_id\": \"116058328229165352500\", \n" +
                    "  \"auth_uri\": \"https://accounts.google.com/o/oauth2/auth\", \n" +
                    "  \"token_uri\": \"https://oauth2.googleapis.com/token\", \n" +
                    "  \"auth_provider_x509_cert_url\": \"https://www.googleapis.com/oauth2/v1/certs\", \n" +
                    "  \"client_x509_cert_url\": \"https://www.googleapis.com/robot/v1/metadata/x509/firebase-adminsdk-k7vzq%40chatup-66815.iam.gserviceaccount.com\", \n" +
                    "  \"universe_domain\": \"googleapis.com\" \n}";

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

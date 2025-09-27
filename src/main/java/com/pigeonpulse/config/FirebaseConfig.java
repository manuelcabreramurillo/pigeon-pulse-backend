package com.pigeonpulse.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Configuration
public class FirebaseConfig {

    @Value("${firebase.service-account-json:}")
    private String serviceAccountJson;

    @Value("${firebase.project-id:}")
    private String projectId;

    @Bean
    public FirebaseApp firebaseApp() throws IOException {
        if (FirebaseApp.getApps().isEmpty()) {
            FirebaseOptions.Builder optionsBuilder = FirebaseOptions.builder();

            // Try to use service account JSON from environment variable first
            if (serviceAccountJson != null && !serviceAccountJson.trim().isEmpty()) {
                try {
                    InputStream serviceAccountStream = new ByteArrayInputStream(
                        serviceAccountJson.getBytes(StandardCharsets.UTF_8)
                    );
                    optionsBuilder.setCredentials(GoogleCredentials.fromStream(serviceAccountStream));
                } catch (Exception e) {
                    throw new RuntimeException("Failed to parse Firebase service account JSON from environment variable", e);
                }
            }
            // Fallback to classpath resource for local development
            else {
                try {
                    InputStream serviceAccount = new ClassPathResource("firebase-service-account.json").getInputStream();
                    optionsBuilder.setCredentials(GoogleCredentials.fromStream(serviceAccount));
                } catch (Exception e) {
                    throw new RuntimeException("Firebase service account not found. Please provide FIREBASE_SERVICE_ACCOUNT_JSON environment variable or place firebase-service-account.json in resources folder", e);
                }
            }

            // Set project ID if provided
            if (projectId != null && !projectId.trim().isEmpty()) {
                optionsBuilder.setProjectId(projectId);
            }

            FirebaseOptions options = optionsBuilder.build();
            return FirebaseApp.initializeApp(options);
        }
        return FirebaseApp.getInstance();
    }

    @Bean
    public Firestore firestore(FirebaseApp firebaseApp) {
        return FirestoreClient.getFirestore(firebaseApp);
    }
}
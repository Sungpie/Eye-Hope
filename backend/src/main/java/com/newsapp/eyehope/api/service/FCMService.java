package com.newsapp.eyehope.api.service;

import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.*;
import com.newsapp.eyehope.api.domain.Topic;
import com.newsapp.eyehope.api.domain.User;
import com.newsapp.eyehope.api.domain.UserTopic;
import com.newsapp.eyehope.api.repository.TopicRepository;
import com.newsapp.eyehope.api.repository.UserRepository;
import com.newsapp.eyehope.api.repository.UserTopicRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

@Slf4j
@Service
@RequiredArgsConstructor
public class FCMService {

    private final UserRepository userRepository;
    private final TopicRepository topicRepository;
    private final UserTopicRepository userTopicRepository;

    /**
     * Check if Firebase is initialized
     * 
     * @return true if Firebase is initialized, false otherwise
     */
    private boolean isFirebaseInitialized() {
        boolean initialized = !FirebaseApp.getApps().isEmpty();
        if (!initialized) {
            log.warn("Firebase is not initialized. FCM services are disabled.");
        }
        return initialized;
    }

    /**
     * Send notification to a specific device
     *
     * @param token Device token
     * @param title Notification title
     * @param body  Notification body
     * @param data  Additional data to send
     * @return Message ID if successful
     */
    public String sendNotificationToDevice(String token, String title, String body, Map<String, String> data) {
        // Check if Firebase is initialized
        if (!isFirebaseInitialized()) {
            log.warn("Skipping notification to device {} because Firebase is not initialized", token);
            return "FIREBASE_NOT_INITIALIZED";
        }

        try {
            // Validate token format (basic validation)
            if (token == null || token.trim().isEmpty()) {
                throw new IllegalArgumentException("Device token cannot be null or empty");
            }

            Message message = createMessage(token, title, body, data);
            // Use send() instead of sendAsync().get() to avoid blocking
            String response = FirebaseMessaging.getInstance().send(message);
            log.info("Successfully sent message to device {}: {}", token, response);
            return response;
        } catch (FirebaseMessagingException e) {
            log.error("Failed to send message to device {}: {}", token, e.getMessage(), e);
            throw new RuntimeException("Failed to send FCM notification", e);
        }
    }

    /**
     * Send notification to multiple devices
     *
     * @param tokens List of device tokens
     * @param title  Notification title
     * @param body   Notification body
     * @param data   Additional data to send
     * @return BatchResponse containing results
     * @throws IllegalArgumentException if tokens list is null/empty
     */
    public BatchResponse sendNotificationToDevices(List<String> tokens, String title, String body, Map<String, String> data) {
        // Check if Firebase is initialized
        if (!isFirebaseInitialized()) {
            log.warn("Skipping notifications to {} devices because Firebase is not initialized", 
                    tokens != null ? tokens.size() : 0);
            return null; // Return null to indicate Firebase is not initialized
        }

        try {
            // Validate tokens list
            if (tokens == null || tokens.isEmpty()) {
                throw new IllegalArgumentException("Device tokens list cannot be null or empty");
            }

            // Remove any null or empty tokens
            List<String> validTokens = tokens.stream()
                    .filter(token -> token != null && !token.trim().isEmpty())
                    .toList();

            if (validTokens.isEmpty()) {
                throw new IllegalArgumentException("No valid device tokens provided");
            }

            List<Message> messages = new ArrayList<>();
            for (String token : validTokens) {
                messages.add(createMessage(token, title, body, data));
            }

            BatchResponse response = FirebaseMessaging.getInstance().sendAll(messages);
            log.info("Successfully sent batch messages: {} successful, {} failed", 
                    response.getSuccessCount(), response.getFailureCount());

            if (response.getFailureCount() > 0) {
                List<SendResponse> responses = response.getResponses();
                for (int i = 0; i < responses.size(); i++) {
                    if (!responses.get(i).isSuccessful()) {
                        log.error("Failed to send message to token {}: {}", 
                                validTokens.get(i), responses.get(i).getException().getMessage());
                    }
                }
            }

            return response;
        } catch (FirebaseMessagingException e) {
            log.error("Failed to send batch messages: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to send FCM batch notifications", e);
        }
    }

    /**
     * Send notification to a topic
     *
     * @param topic Topic name
     * @param title Notification title
     * @param body  Notification body
     * @param data  Additional data to send
     * @return Message ID if successful
     */
    public String sendNotificationToTopic(String topic, String title, String body, Map<String, String> data) {
        // Check if Firebase is initialized
        if (!isFirebaseInitialized()) {
            log.warn("Skipping notification to topic {} because Firebase is not initialized", topic);
            return "FIREBASE_NOT_INITIALIZED";
        }

        try {
            // Validate topic name
            if (topic == null || topic.trim().isEmpty()) {
                throw new IllegalArgumentException("Topic name cannot be null or empty");
            }

            // Ensure topic follows FCM naming convention
            if (!topic.matches("[a-zA-Z0-9-_.~%]+")) {
                throw new IllegalArgumentException("Topic name contains invalid characters. Use only: a-z, A-Z, 0-9, -, _, ., ~, %");
            }

            // Prefix with /topics/ if not already prefixed
            String formattedTopic = topic.startsWith("/topics/") ? topic : "/topics/" + topic;

            Message.Builder messageBuilder = Message.builder()
                    .setNotification(Notification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .build())
                    .setTopic(formattedTopic);

            // Add data only if it's not null
            if (data != null) {
                messageBuilder.putAllData(data);
            }

            String response = FirebaseMessaging.getInstance().send(messageBuilder.build());
            log.info("Successfully sent message to topic {}: {}", formattedTopic, response);
            return response;
        } catch (FirebaseMessagingException e) {
            log.error("Failed to send message to topic {}: {}", topic, e.getMessage(), e);
            throw new RuntimeException("Failed to send FCM notification to topic", e);
        }
    }

    /**
     * Subscribe tokens to a topic
     *
     * @param tokens List of device tokens
     * @param topic  Topic name
     * @return TopicManagementResponse
     * @throws IllegalArgumentException if tokens list is null/empty or topic is invalid
     */
    public TopicManagementResponse subscribeToTopic(List<String> tokens, String topic) {
        // Check if Firebase is initialized
        if (!isFirebaseInitialized()) {
            log.warn("Skipping subscription to topic {} because Firebase is not initialized", topic);
            return null; // Return null to indicate Firebase is not initialized
        }

        try {
            // Validate tokens list
            if (tokens == null || tokens.isEmpty()) {
                throw new IllegalArgumentException("Device tokens list cannot be null or empty");
            }

            // Validate topic name
            if (topic == null || topic.trim().isEmpty()) {
                throw new IllegalArgumentException("Topic name cannot be null or empty");
            }

            // Ensure topic follows FCM naming convention
            if (!topic.matches("[a-zA-Z0-9-_.~%]+")) {
                throw new IllegalArgumentException("Topic name contains invalid characters. Use only: a-z, A-Z, 0-9, -, _, ., ~, %");
            }

            // Prefix with /topics/ if not already prefixed
            String formattedTopic = topic.startsWith("/topics/") ? topic : "/topics/" + topic;

            // Remove any null or empty tokens
            List<String> validTokens = tokens.stream()
                    .filter(token -> token != null && !token.trim().isEmpty())
                    .toList();

            if (validTokens.isEmpty()) {
                throw new IllegalArgumentException("No valid device tokens provided");
            }

            TopicManagementResponse response = FirebaseMessaging.getInstance().subscribeToTopic(validTokens, formattedTopic);
            log.info("Successfully subscribed {} devices to topic {}", 
                    validTokens.size() - response.getFailureCount(), formattedTopic);

            if (response.getFailureCount() > 0) {
                log.error("Failed to subscribe {} devices to topic {}", response.getFailureCount(), formattedTopic);
            }

            return response;
        } catch (FirebaseMessagingException e) {
            log.error("Failed to subscribe to topic {}: {}", topic, e.getMessage(), e);
            throw new RuntimeException("Failed to subscribe to FCM topic", e);
        }
    }

    /**
     * Subscribe a user to a topic and persist the subscription in the database
     *
     * @param deviceId User's device ID
     * @param topic    Topic name
     * @return TopicManagementResponse
     */
    @Transactional
    public TopicManagementResponse subscribeUserToTopic(UUID deviceId, String topic) {
        // Check if Firebase is initialized
        if (!isFirebaseInitialized()) {
            log.warn("Skipping user subscription to topic {} because Firebase is not initialized", topic);

            // Even if Firebase is not available, we can still create the database relationship
            // Find the user
            User user = userRepository.findByDeviceId(deviceId)
                    .orElseThrow(() -> new IllegalArgumentException("User not found with deviceId: " + deviceId));

            // Get or create the topic
            Topic topicEntity = topicRepository.findByTopicName(topic)
                    .orElseGet(() -> {
                        Topic newTopic = Topic.builder()
                                .topicName(topic)
                                .build();
                        return topicRepository.save(newTopic);
                    });

            // Check if subscription already exists
            if (!userTopicRepository.existsByUserAndTopic(user, topicEntity)) {
                // Create and save the user-topic relationship
                UserTopic userTopic = UserTopic.builder()
                        .user(user)
                        .topic(topicEntity)
                        .build();
                userTopicRepository.save(userTopic);
                log.info("Persisted topic subscription for user {} to topic {} (Firebase unavailable)", deviceId, topic);
            }

            return null;
        }

        // Find the user
        User user = userRepository.findByDeviceId(deviceId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with deviceId: " + deviceId));

        // Check if user has FCM token
        if (user.getFcmToken() == null || user.getFcmToken().isEmpty()) {
            throw new IllegalArgumentException("User does not have an FCM token registered");
        }

        // Subscribe to FCM topic
        List<String> tokens = List.of(user.getFcmToken());
        TopicManagementResponse response = subscribeToTopic(tokens, topic);

        // If Firebase is not initialized, response will be null
        if (response == null) {
            return null;
        }

        // If subscription was successful, persist in database
        if (response.getSuccessCount() > 0) {
            // Get or create the topic
            Topic topicEntity = topicRepository.findByTopicName(topic)
                    .orElseGet(() -> {
                        Topic newTopic = Topic.builder()
                                .topicName(topic)
                                .build();
                        return topicRepository.save(newTopic);
                    });

            // Check if subscription already exists
            if (!userTopicRepository.existsByUserAndTopic(user, topicEntity)) {
                // Create and save the user-topic relationship
                UserTopic userTopic = UserTopic.builder()
                        .user(user)
                        .topic(topicEntity)
                        .build();
                userTopicRepository.save(userTopic);
                log.info("Persisted topic subscription for user {} to topic {}", deviceId, topic);
            } else {
                log.info("User {} is already subscribed to topic {}", deviceId, topic);
            }
        }

        return response;
    }

    /**
     * Unsubscribe tokens from a topic
     *
     * @param tokens List of device tokens
     * @param topic  Topic name
     * @return TopicManagementResponse
     * @throws IllegalArgumentException if tokens list is null/empty or topic is invalid
     */
    public TopicManagementResponse unsubscribeFromTopic(List<String> tokens, String topic) {
        try {
            // Validate tokens list
            if (tokens == null || tokens.isEmpty()) {
                throw new IllegalArgumentException("Device tokens list cannot be null or empty");
            }

            // Validate topic name
            if (topic == null || topic.trim().isEmpty()) {
                throw new IllegalArgumentException("Topic name cannot be null or empty");
            }

            // Ensure topic follows FCM naming convention
            if (!topic.matches("[a-zA-Z0-9-_.~%]+")) {
                throw new IllegalArgumentException("Topic name contains invalid characters. Use only: a-z, A-Z, 0-9, -, _, ., ~, %");
            }

            // Prefix with /topics/ if not already prefixed
            String formattedTopic = topic.startsWith("/topics/") ? topic : "/topics/" + topic;

            // Remove any null or empty tokens
            List<String> validTokens = tokens.stream()
                    .filter(token -> token != null && !token.trim().isEmpty())
                    .toList();

            if (validTokens.isEmpty()) {
                throw new IllegalArgumentException("No valid device tokens provided");
            }

            TopicManagementResponse response = FirebaseMessaging.getInstance().unsubscribeFromTopic(validTokens, formattedTopic);
            log.info("Successfully unsubscribed {} devices from topic {}", 
                    validTokens.size() - response.getFailureCount(), formattedTopic);

            if (response.getFailureCount() > 0) {
                log.error("Failed to unsubscribe {} devices from topic {}", response.getFailureCount(), formattedTopic);
            }

            return response;
        } catch (FirebaseMessagingException e) {
            log.error("Failed to unsubscribe from topic {}: {}", topic, e.getMessage(), e);
            throw new RuntimeException("Failed to unsubscribe from FCM topic", e);
        }
    }

    /**
     * Unsubscribe a user from a topic and remove the subscription from the database
     *
     * @param deviceId User's device ID
     * @param topic    Topic name
     * @return TopicManagementResponse
     */
    @Transactional
    public TopicManagementResponse unsubscribeUserFromTopic(UUID deviceId, String topic) {
        // Find the user
        User user = userRepository.findByDeviceId(deviceId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with deviceId: " + deviceId));

        // Check if user has FCM token
        if (user.getFcmToken() == null || user.getFcmToken().isEmpty()) {
            throw new IllegalArgumentException("User does not have an FCM token registered");
        }

        // Unsubscribe from FCM topic
        List<String> tokens = List.of(user.getFcmToken());
        TopicManagementResponse response = unsubscribeFromTopic(tokens, topic);

        // If unsubscription was successful, remove from database
        if (response.getSuccessCount() > 0) {
            // Find the topic
            topicRepository.findByTopicName(topic).ifPresent(topicEntity -> {
                // Find and delete the user-topic relationship
                userTopicRepository.findByUserAndTopic(user, topicEntity).ifPresent(userTopic -> {
                    userTopicRepository.delete(userTopic);
                    log.info("Removed topic subscription for user {} from topic {}", deviceId, topic);
                });
            });
        }

        return response;
    }

    /**
     * Helper method to create a message
     * 
     * @param token Device token
     * @param title Notification title
     * @param body  Notification body
     * @param data  Additional data to send (can be null)
     * @return Message object
     */
    private Message createMessage(String token, String title, String body, Map<String, String> data) {
        Message.Builder messageBuilder = Message.builder()
                .setToken(token)
                .setNotification(Notification.builder()
                        .setTitle(title)
                        .setBody(body)
                        .build())
                .setAndroidConfig(AndroidConfig.builder()
                        .setPriority(AndroidConfig.Priority.HIGH)
                        .build())
                .setApnsConfig(ApnsConfig.builder()
                        .putHeader("apns-priority", "10")
                        .setAps(Aps.builder()
                                .setSound("default")
                                .build())
                        .build());

        // Add data only if it's not null
        if (data != null) {
            messageBuilder.putAllData(data);
        }

        return messageBuilder.build();
    }
}

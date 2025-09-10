package com.newsapp.eyehope.api.service;

import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class FCMService {

    // In-memory storage for device tokens by device ID
    private final Map<UUID, String> deviceTokens = new ConcurrentHashMap<>();

    // In-memory storage for topic subscriptions by device ID
    private final Map<UUID, Set<String>> topicSubscriptions = new ConcurrentHashMap<>();

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
     * Register a device token for a device ID
     * 
     * @param deviceId Device ID
     * @param token FCM token
     */
    public void registerDeviceToken(UUID deviceId, String token) {
        if (deviceId == null) {
            throw new IllegalArgumentException("Device ID cannot be null");
        }
        if (token == null || token.trim().isEmpty()) {
            throw new IllegalArgumentException("Token cannot be null or empty");
        }

        deviceTokens.put(deviceId, token);
        log.info("Registered FCM token for device ID: {}", deviceId);
    }

    /**
     * Get the FCM token for a device ID
     * 
     * @param deviceId Device ID
     * @return FCM token or null if not found
     */
    public String getDeviceToken(UUID deviceId) {
        return deviceTokens.get(deviceId);
    }

    /**
     * Subscribe a user to a topic and store the subscription in memory
     *
     * @param deviceId User's device ID
     * @param topic    Topic name
     * @return TopicManagementResponse
     */
    public TopicManagementResponse subscribeUserToTopic(UUID deviceId, String topic) {
        // Check if device ID is registered
        String token = deviceTokens.get(deviceId);
        if (token == null) {
            throw new IllegalArgumentException("Device ID not registered: " + deviceId);
        }

        // Store subscription in memory
        topicSubscriptions.computeIfAbsent(deviceId, k -> new HashSet<>()).add(topic);
        log.info("Stored topic subscription for device {} to topic {}", deviceId, topic);

        // Subscribe to FCM topic if Firebase is initialized
        if (isFirebaseInitialized()) {
            return subscribeToTopic(List.of(token), topic);
        } else {
            log.warn("Firebase not initialized, subscription stored in memory only");
            return null;
        }
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
     * Unsubscribe a user from a topic and remove the subscription from memory
     *
     * @param deviceId User's device ID
     * @param topic    Topic name
     * @return TopicManagementResponse
     */
    public TopicManagementResponse unsubscribeUserFromTopic(UUID deviceId, String topic) {
        // Check if device ID is registered
        String token = deviceTokens.get(deviceId);
        if (token == null) {
            throw new IllegalArgumentException("Device ID not registered: " + deviceId);
        }

        // Remove subscription from memory
        Set<String> topics = topicSubscriptions.get(deviceId);
        if (topics != null) {
            topics.remove(topic);
            log.info("Removed topic subscription for device {} from topic {}", deviceId, topic);
        }

        // Unsubscribe from FCM topic if Firebase is initialized
        if (isFirebaseInitialized()) {
            return unsubscribeFromTopic(List.of(token), topic);
        } else {
            log.warn("Firebase not initialized, subscription removed from memory only");
            return null;
        }
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

package com.newsapp.eyehope.api.service;

import com.google.firebase.messaging.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Slf4j
@Service
public class FCMService {

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

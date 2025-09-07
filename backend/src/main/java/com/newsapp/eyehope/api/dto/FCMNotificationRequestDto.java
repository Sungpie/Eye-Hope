package com.newsapp.eyehope.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FCMNotificationRequestDto {
    
    // Target type: "token", "tokens", or "topic"
    private String targetType;
    
    // Single device token
    private String token;
    
    // Multiple device tokens
    private List<String> tokens;
    
    // Topic name
    private String topic;
    
    // Notification title
    private String title;
    
    // Notification body
    private String body;
    
    // Additional data to send with the notification
    @Builder.Default
    private Map<String, String> data = new HashMap<>();
}
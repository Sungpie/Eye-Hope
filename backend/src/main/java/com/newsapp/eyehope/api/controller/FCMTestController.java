package com.newsapp.eyehope.api.controller;

import com.newsapp.eyehope.api.dto.ApiResponse;
import com.newsapp.eyehope.api.service.FCMService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/fcm-test")
@RequiredArgsConstructor
@Tag(name = "FCM 테스트", description = "Firebase Cloud Messaging Test API")
public class FCMTestController {

    private final FCMService fcmService;

    @GetMapping("/send-test-notification")
    @Operation(summary = "테스트 FCM 알림 전송", description = "특정 기기 토큰에 테스트 알림 전송")
    public ResponseEntity<ApiResponse<String>> sendTestNotification(
            @RequestParam String token,
            @RequestParam(defaultValue = "Test Notification") String title,
            @RequestParam(defaultValue = "This is a test notification from Eye-Hope") String body) {

        log.info("Sending test notification to token: {}", token);

        Map<String, String> data = new HashMap<>();
        data.put("type", "test");
        data.put("timestamp", String.valueOf(System.currentTimeMillis()));

        String response = fcmService.sendNotificationToDevice(token, title, body, data);

        return ResponseEntity.ok(ApiResponse.success(response, "Test notification sent successfully"));
    }

    @GetMapping("/send-test-topic")
    @Operation(summary = "테스트 FCM 토픽 알림 전송", description = "특정 토픽에 테스트 알림 전송")
    public ResponseEntity<ApiResponse<String>> sendTestTopicNotification(
            @RequestParam String topic,
            @RequestParam(defaultValue = "Test Topic Notification") String title,
            @RequestParam(defaultValue = "This is a test topic notification from Eye-Hope") String body) {

        log.info("Sending test notification to topic: {}", topic);

        Map<String, String> data = new HashMap<>();
        data.put("type", "topic_test");
        data.put("timestamp", String.valueOf(System.currentTimeMillis()));

        String response = fcmService.sendNotificationToTopic(topic, title, body, data);

        return ResponseEntity.ok(ApiResponse.success(response, "Test topic notification sent successfully"));
    }
}

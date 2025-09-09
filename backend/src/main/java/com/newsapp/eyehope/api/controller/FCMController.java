package com.newsapp.eyehope.api.controller;

import com.google.firebase.messaging.BatchResponse;
import com.google.firebase.messaging.TopicManagementResponse;
import com.newsapp.eyehope.api.domain.User;
import com.newsapp.eyehope.api.dto.ApiResponse;
import com.newsapp.eyehope.api.dto.FCMNotificationRequestDto;
import com.newsapp.eyehope.api.dto.FCMTopicRequestDto;
import com.newsapp.eyehope.api.repository.UserRepository;
import com.newsapp.eyehope.api.service.AdminAuthorizationService;
import com.newsapp.eyehope.api.service.FCMService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;

@Slf4j
@RestController
@RequestMapping("/api/v1/fcm")
@RequiredArgsConstructor
@Tag(name = "FCM", description = "Firebase Cloud Messaging API")
public class FCMController {

    private final FCMService fcmService;
    private final UserRepository userRepository;
    private final AdminAuthorizationService adminAuthorizationService;

    @PostMapping("/send")
    @Operation(summary = "Send FCM notification (관리자 전용)", description = "단일 기기, 여러 기기 또는 특정 주제(토픽)에 알림을 보냅니다. 관리자 권한이 필요합니다.")
    @io.swagger.v3.oas.annotations.Parameters({
        @io.swagger.v3.oas.annotations.Parameter(
            name = "X-Device-ID",
            description = "요청자의 디바이스 ID (UUID 형식)",
            required = true,
            in = io.swagger.v3.oas.annotations.enums.ParameterIn.HEADER)
    })
    public ResponseEntity<ApiResponse<Object>> sendNotification(
            @RequestHeader("X-Device-ID") java.util.UUID requesterId,
            @RequestBody FCMNotificationRequestDto requestDto) {
        log.info("Received request to send notification: {}, requester: {}", requestDto, requesterId);

        // 관리자 권한 확인
        adminAuthorizationService.verifyAdminAccess(requesterId);

        Object response;

        switch (requestDto.getTargetType().toLowerCase()) {
            case "token":
                if (requestDto.getToken() == null || requestDto.getToken().isEmpty()) {
                    return ResponseEntity.badRequest().body(
                            ApiResponse.error("Token is required for targetType 'token'"));
                }
                response = fcmService.sendNotificationToDevice(
                        requestDto.getToken(),
                        requestDto.getTitle(),
                        requestDto.getBody(),
                        requestDto.getData());
                break;

            case "tokens":
                if (requestDto.getTokens() == null || requestDto.getTokens().isEmpty()) {
                    return ResponseEntity.badRequest().body(
                            ApiResponse.error("Tokens list is required for targetType 'tokens'"));
                }
                response = fcmService.sendNotificationToDevices(
                        requestDto.getTokens(),
                        requestDto.getTitle(),
                        requestDto.getBody(),
                        requestDto.getData());
                break;

            case "topic":
                if (requestDto.getTopic() == null || requestDto.getTopic().isEmpty()) {
                    return ResponseEntity.badRequest().body(
                            ApiResponse.error("Topic is required for targetType 'topic'"));
                }
                response = fcmService.sendNotificationToTopic(
                        requestDto.getTopic(),
                        requestDto.getTitle(),
                        requestDto.getBody(),
                        requestDto.getData());
                break;

            default:
                return ResponseEntity.badRequest().body(
                        ApiResponse.error("Invalid targetType. Must be one of: 'token', 'tokens', or 'topic'"));
        }

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/topic/subscribe")
    @Operation(summary = "Subscribe to FCM topic (관리자 전용)", description = "특정 기기를 FCM 토픽에 구독시킵니다. device_id와 topic을 입력받아 처리합니다. DB에도 구독 정보가 저장됩니다. 관리자 권한이 필요합니다.")
    @io.swagger.v3.oas.annotations.Parameters({
        @io.swagger.v3.oas.annotations.Parameter(
            name = "X-Device-ID",
            description = "요청자의 디바이스 ID (UUID 형식)",
            required = true,
            in = io.swagger.v3.oas.annotations.enums.ParameterIn.HEADER)
    })
    public ResponseEntity<ApiResponse<TopicManagementResponse>> subscribeToTopic(
            @RequestHeader("X-Device-ID") java.util.UUID requesterId,
            @RequestBody FCMTopicRequestDto requestDto) {
        log.info("Received request to subscribe to topic: {}, requester: {}", requestDto, requesterId);

        // 관리자 권한 확인
        adminAuthorizationService.verifyAdminAccess(requesterId);

        // Topic is always required
        if (requestDto.getTopic() == null || requestDto.getTopic().isEmpty()) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.error("Topic is required"));
        }

        // Device ID is required
        if (requestDto.getDeviceId() == null) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.error("Device ID is required"));
        }

        try {
            // Use the method that updates the database
            TopicManagementResponse response = fcmService.subscribeUserToTopic(
                    requestDto.getDeviceId(),
                    requestDto.getTopic());

            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/topic/unsubscribe")
    @Operation(summary = "Unsubscribe from FCM topic (관리자 전용)", description = "특정 기기를 FCM 토픽에서 구독 취소시킵니다. device_id와 topic을 입력받아 처리합니다. DB에서도 구독 정보가 삭제됩니다. 관리자 권한이 필요합니다.")
    @io.swagger.v3.oas.annotations.Parameters({
        @io.swagger.v3.oas.annotations.Parameter(
            name = "X-Device-ID",
            description = "요청자의 디바이스 ID (UUID 형식)",
            required = true,
            in = io.swagger.v3.oas.annotations.enums.ParameterIn.HEADER)
    })
    public ResponseEntity<ApiResponse<TopicManagementResponse>> unsubscribeFromTopic(
            @RequestHeader("X-Device-ID") java.util.UUID requesterId,
            @RequestBody FCMTopicRequestDto requestDto) {
        log.info("Received request to unsubscribe from topic: {}, requester: {}", requestDto, requesterId);

        // 관리자 권한 확인
        adminAuthorizationService.verifyAdminAccess(requesterId);

        // Topic is always required
        if (requestDto.getTopic() == null || requestDto.getTopic().isEmpty()) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.error("Topic is required"));
        }

        // Device ID is required
        if (requestDto.getDeviceId() == null) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.error("Device ID is required"));
        }

        try {
            // Use the method that updates the database
            TopicManagementResponse response = fcmService.unsubscribeUserFromTopic(
                    requestDto.getDeviceId(),
                    requestDto.getTopic());

            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.error(e.getMessage()));
        }
    }
}

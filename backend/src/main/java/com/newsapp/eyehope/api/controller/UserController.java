package com.newsapp.eyehope.api.controller;

import com.newsapp.eyehope.api.dto.ApiResponse;
import com.newsapp.eyehope.api.dto.NotificationScheduleRequestDto;
import com.newsapp.eyehope.api.dto.NotificationScheduleResponseDto;
import com.newsapp.eyehope.api.dto.UserRequestDto;
import com.newsapp.eyehope.api.dto.UserResponseDto;
import com.newsapp.eyehope.api.service.NotificationScheduleService;
import com.newsapp.eyehope.api.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@io.swagger.v3.oas.annotations.tags.Tag(name = "User API", description = "사용자 관련 API")
public class UserController {

    private final UserService userService;
    private final NotificationScheduleService notificationScheduleService;

    /**
     * 사용자 등록
     */
    @io.swagger.v3.oas.annotations.Operation(
        summary = "사용자 등록",
        description = "새로운 사용자를 등록합니다. 디바이스 ID, 이름, 이메일, 별명, 비밀번호 정보가 필요합니다."
    )
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponseDto>> registerUser(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "사용자 등록 정보",
                required = true
            )
            @RequestBody UserRequestDto requestDto) {
        log.info("사용자 등록 요청: {}", requestDto.getEmail());
        UserResponseDto responseDto = userService.registerUser(requestDto);
        return ResponseEntity.ok(ApiResponse.success("사용자 등록이 완료되었습니다.", responseDto));
    }

    /**
     * Save notification schedules for a device
     * 
     * @param requestDto the request DTO containing device ID and notification times
     * @return the response entity containing the saved notification schedules
     */
    @PostMapping("/schedules")
    public ResponseEntity<ApiResponse<NotificationScheduleResponseDto>> saveNotificationSchedules(
            @Valid @RequestBody NotificationScheduleRequestDto requestDto) {
        NotificationScheduleResponseDto responseDto = notificationScheduleService.saveNotificationSchedules(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Notification schedules saved successfully", responseDto));
    }

    /**
     * Get notification schedules for a device
     * 
     * @param deviceId the device ID
     * @return the response entity containing the notification schedules
     */
    @GetMapping("/schedules/{deviceId}")
    public ResponseEntity<ApiResponse<NotificationScheduleResponseDto>> getNotificationSchedules(
            @PathVariable UUID deviceId) {
        NotificationScheduleResponseDto responseDto = notificationScheduleService.getNotificationSchedules(deviceId);
        return ResponseEntity.ok(ApiResponse.success("Notification schedules retrieved successfully", responseDto));
    }
}

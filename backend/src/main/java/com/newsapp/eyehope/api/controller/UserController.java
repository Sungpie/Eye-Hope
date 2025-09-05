package com.newsapp.eyehope.api.controller;

import com.newsapp.eyehope.api.dto.ApiResponse;
import com.newsapp.eyehope.api.dto.NotificationScheduleRequestDto;
import com.newsapp.eyehope.api.dto.NotificationScheduleResponseDto;
import com.newsapp.eyehope.api.dto.PasswordChangeDto;
import com.newsapp.eyehope.api.dto.UserRequestDto;
import com.newsapp.eyehope.api.dto.UserResponseDto;
import com.newsapp.eyehope.api.dto.UserUpdateDto;
import com.newsapp.eyehope.api.service.NotificationScheduleService;
import com.newsapp.eyehope.api.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.NoSuchElementException;
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
     * 사용자 정보 조회
     */
    @io.swagger.v3.oas.annotations.Operation(
            summary = "사용자 정보 조회",
            description = "디바이스ID를 통해 사용자 정보를 조회합니다."
    )
    @GetMapping("/{deviceId}")
    public ResponseEntity<ApiResponse<UserResponseDto>> getUserInfo(@PathVariable UUID deviceId) {
        try {
            log.info("사용자 정보 조회 요청: {}", deviceId);
            UserResponseDto responseDto = userService.getUserByDeviceId(deviceId);
            return ResponseEntity.ok(ApiResponse.success("사용자 정보 조회가 완료되었습니다.", responseDto));
        } catch (NoSuchElementException e) {
            log.warn("사용자 정보 조회 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("사용자 정보 조회 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("사용자 정보 조회 중 오류가 발생했습니다."));
        }
    }

    /**
     * Save notification schedules for a device
     * 
     * @param requestDto the request DTO containing device ID and notification times
     * @return the response entity containing the saved notification schedules
     */
    @io.swagger.v3.oas.annotations.Operation(
            summary = "사용자 알림 시간 등록",
            description = "사용자별 알림 시간을 등록합니다. UUID와 시간 정보(ex; 14:00)가 필요합니다.  "
    )
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
    @io.swagger.v3.oas.annotations.Operation(
            summary = "사용자 알림 시간 조회",
            description = "사용자별 알림 시간을 조회합니다."
    )
    @GetMapping("/schedules/{deviceId}")
    public ResponseEntity<ApiResponse<NotificationScheduleResponseDto>> getNotificationSchedules(
            @PathVariable UUID deviceId) {
        NotificationScheduleResponseDto responseDto = notificationScheduleService.getNotificationSchedules(deviceId);
        return ResponseEntity.ok(ApiResponse.success("Notification schedules retrieved successfully", responseDto));
    }

    /**
     * 사용자 정보 전체 업데이트 (PUT)
     */
    @io.swagger.v3.oas.annotations.Operation(
            summary = "사용자 정보 전체 업데이트",
            description = "사용자의 이름, 이메일, 별명 정보를 모두 업데이트합니다."
    )
    @PutMapping("/{deviceId}")
    public ResponseEntity<ApiResponse<UserResponseDto>> updateUser(
            @PathVariable UUID deviceId,
            @RequestBody UserUpdateDto updateDto) {
        try {
            log.info("사용자 정보 전체 업데이트 요청: {}", deviceId);
            UserResponseDto responseDto = userService.updateUser(deviceId, updateDto);
            return ResponseEntity.ok(ApiResponse.success("사용자 정보가 업데이트되었습니다.", responseDto));
        } catch (NoSuchElementException e) {
            log.warn("사용자 정보 업데이트 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (IllegalArgumentException e) {
            log.warn("사용자 정보 업데이트 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("사용자 정보 업데이트 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("사용자 정보 업데이트 중 오류가 발생했습니다."));
        }
    }

    /**
     * 사용자 정보 부분 업데이트 (PATCH)
     */
    @io.swagger.v3.oas.annotations.Operation(
            summary = "사용자 정보 부분 업데이트",
            description = "사용자의 이름, 이메일, 별명 정보 중 일부만 업데이트합니다."
    )
    @PatchMapping("/{deviceId}")
    public ResponseEntity<ApiResponse<UserResponseDto>> partialUpdateUser(
            @PathVariable UUID deviceId,
            @RequestBody UserUpdateDto updateDto) {
        try {
            log.info("사용자 정보 부분 업데이트 요청: {}", deviceId);
            UserResponseDto responseDto = userService.updateUser(deviceId, updateDto);
            return ResponseEntity.ok(ApiResponse.success("사용자 정보가 업데이트되었습니다.", responseDto));
        } catch (NoSuchElementException e) {
            log.warn("사용자 정보 업데이트 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (IllegalArgumentException e) {
            log.warn("사용자 정보 업데이트 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("사용자 정보 업데이트 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("사용자 정보 업데이트 중 오류가 발생했습니다."));
        }
    }

    /**
     * 비밀번호 변경
     */
    @io.swagger.v3.oas.annotations.Operation(
            summary = "비밀번호 변경",
            description = "사용자의 비밀번호를 변경합니다. 현재 비밀번호 확인이 필요합니다."
    )
    @PostMapping("/password")
    public ResponseEntity<ApiResponse<UserResponseDto>> changePassword(
            @RequestBody PasswordChangeDto passwordChangeDto) {
        try {
            log.info("비밀번호 변경 요청: {}", passwordChangeDto.getDeviceId());
            UserResponseDto responseDto = userService.changePassword(passwordChangeDto);
            return ResponseEntity.ok(ApiResponse.success("비밀번호가 변경되었습니다.", responseDto));
        } catch (NoSuchElementException e) {
            log.warn("비밀번호 변경 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (IllegalArgumentException e) {
            log.warn("비밀번호 변경 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("비밀번호 변경 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("비밀번호 변경 중 오류가 발생했습니다."));
        }
    }
}

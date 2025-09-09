package com.newsapp.eyehope.api.controller;

import com.newsapp.eyehope.api.domain.User;
import com.newsapp.eyehope.api.dto.ApiResponse;
import com.newsapp.eyehope.api.dto.UserResponseDto;
import com.newsapp.eyehope.api.repository.UserRepository;
import com.newsapp.eyehope.api.service.AdminAuthorizationService;
import com.newsapp.eyehope.api.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@io.swagger.v3.oas.annotations.tags.Tag(name = "Admin API", description = "관리자 기능 API")
public class AdminController {

    private final UserService userService;
    private final AdminAuthorizationService adminAuthorizationService;
    private final UserRepository userRepository;

    /**
     * 사용자에게 관리자 권한 부여 (관리자 전용)
     */
    @io.swagger.v3.oas.annotations.Operation(
        summary = "관리자 권한 부여 (관리자 전용)",
        description = "특정 사용자에게 관리자 권한을 부여합니다. 관리자 권한이 필요합니다."
    )
    @io.swagger.v3.oas.annotations.Parameters({
        @io.swagger.v3.oas.annotations.Parameter(
            name = "X-Device-ID",
            description = "요청자의 디바이스 ID (UUID 형식)",
            required = true,
            in = io.swagger.v3.oas.annotations.enums.ParameterIn.HEADER)
    })
    @PostMapping("/users/{targetDeviceId}/grant")
    public ResponseEntity<ApiResponse<UserResponseDto>> grantAdminPrivilege(
            @RequestHeader("X-Device-ID") UUID deviceId,
            @PathVariable UUID targetDeviceId) {
        log.info("관리자 권한 부여 요청, 요청자={}, 대상={}", deviceId, targetDeviceId);

        // 관리자 권한 확인
        adminAuthorizationService.verifyAdminAccess(deviceId);

        UserResponseDto updatedUser = userService.setAdminStatus(targetDeviceId, true);
        return ResponseEntity.ok(ApiResponse.success("관리자 권한이 부여되었습니다.", updatedUser));
    }

    /**
     * 사용자의 관리자 권한 해제 (관리자 전용)
     */
    @io.swagger.v3.oas.annotations.Operation(
        summary = "관리자 권한 해제 (관리자 전용)",
        description = "특정 사용자의 관리자 권한을 해제합니다. 관리자 권한이 필요합니다."
    )
    @io.swagger.v3.oas.annotations.Parameters({
        @io.swagger.v3.oas.annotations.Parameter(
            name = "X-Device-ID",
            description = "요청자의 디바이스 ID (UUID 형식)",
            required = true,
            in = io.swagger.v3.oas.annotations.enums.ParameterIn.HEADER)
    })
    @PostMapping("/users/{targetDeviceId}/revoke")
    public ResponseEntity<ApiResponse<UserResponseDto>> revokeAdminPrivilege(
            @RequestHeader("X-Device-ID") UUID deviceId,
            @PathVariable UUID targetDeviceId) {
        log.info("관리자 권한 해제 요청, 요청자={}, 대상={}", deviceId, targetDeviceId);

        // 관리자 권한 확인
        adminAuthorizationService.verifyAdminAccess(deviceId);

        // 자기 자신의 권한을 해제하려는 경우 방지
        if (deviceId.equals(targetDeviceId)) {
            throw new IllegalArgumentException("자신의 관리자 권한은 해제할 수 없습니다.");
        }

        UserResponseDto updatedUser = userService.setAdminStatus(targetDeviceId, false);
        return ResponseEntity.ok(ApiResponse.success("관리자 권한이 해제되었습니다.", updatedUser));
    }

    /**
     * 모든 관리자 목록 조회 (관리자 전용)
     */
    @io.swagger.v3.oas.annotations.Operation(
        summary = "관리자 목록 조회 (관리자 전용)",
        description = "시스템의 모든 관리자 목록을 조회합니다. 관리자 권한이 필요합니다."
    )
    @io.swagger.v3.oas.annotations.Parameters({
        @io.swagger.v3.oas.annotations.Parameter(
            name = "X-Device-ID",
            description = "요청자의 디바이스 ID (UUID 형식)",
            required = true,
            in = io.swagger.v3.oas.annotations.enums.ParameterIn.HEADER)
    })
    @GetMapping("/users")
    public ResponseEntity<ApiResponse<List<UserResponseDto>>> listAdminUsers(
            @RequestHeader("X-Device-ID") UUID deviceId) {
        log.info("관리자 목록 조회 요청, 요청자={}", deviceId);

        // 관리자 권한 확인
        adminAuthorizationService.verifyAdminAccess(deviceId);

        List<User> adminUsers = userRepository.findByIsAdmin(true);
        List<UserResponseDto> adminUserDtos = adminUsers.stream()
                .map(UserResponseDto::fromEntity)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success("관리자 목록 조회 성공", adminUserDtos));
    }
}

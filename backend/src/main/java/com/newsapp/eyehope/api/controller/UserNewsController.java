package com.newsapp.eyehope.api.controller;

import com.newsapp.eyehope.api.dto.ApiResponse;
import com.newsapp.eyehope.api.dto.UserNewsRequestDto;
import com.newsapp.eyehope.api.dto.UserNewsResponseDto;
import com.newsapp.eyehope.api.service.UserNewsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.NoSuchElementException;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/users/news")
@RequiredArgsConstructor
@io.swagger.v3.oas.annotations.tags.Tag(name = "User News API", description = "사용자 관심 뉴스 카테고리 관련 API")
public class UserNewsController {

    private final UserNewsService userNewsService;

    /**
     * 사용자 관심 뉴스 카테고리 저장
     */
    @io.swagger.v3.oas.annotations.Operation(
            summary = "사용자 관심 뉴스 카테고리 저장",
            description = "사용자의 관심 뉴스 카테고리를 저장합니다. 기존 카테고리는 삭제되고 새로운 카테고리로 대체됩니다."
    )
    @PostMapping
    public ResponseEntity<ApiResponse<UserNewsResponseDto>> saveUserNewsPreferences(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "사용자 관심 뉴스 카테고리 정보",
                    required = true
            )
            @RequestBody UserNewsRequestDto requestDto) {
        try {
            log.info("사용자 관심 뉴스 카테고리 저장 요청: {}", requestDto.getDeviceId());
            UserNewsResponseDto responseDto = userNewsService.saveUserNewsPreferences(requestDto);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("사용자 관심 뉴스 카테고리 저장이 완료되었습니다.", responseDto));
        } catch (NoSuchElementException e) {
            log.warn("사용자 관심 뉴스 카테고리 저장 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("사용자 관심 뉴스 카테고리 저장 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("사용자 관심 뉴스 카테고리 저장 중 오류가 발생했습니다."));
        }
    }

    /**
     * 사용자 관심 뉴스 카테고리 조회
     */
    @io.swagger.v3.oas.annotations.Operation(
            summary = "사용자 관심 뉴스 카테고리 조회",
            description = "사용자의 관심 뉴스 카테고리를 조회합니다."
    )
    @GetMapping("/{deviceId}")
    public ResponseEntity<ApiResponse<UserNewsResponseDto>> getUserNewsPreferences(
            @PathVariable UUID deviceId) {
        try {
            log.info("사용자 관심 뉴스 카테고리 조회 요청: {}", deviceId);
            UserNewsResponseDto responseDto = userNewsService.getUserNewsPreferences(deviceId);
            return ResponseEntity.ok(ApiResponse.success("사용자 관심 뉴스 카테고리 조회가 완료되었습니다.", responseDto));
        } catch (NoSuchElementException e) {
            log.warn("사용자 관심 뉴스 카테고리 조회 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("사용자 관심 뉴스 카테고리 조회 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("사용자 관심 뉴스 카테고리 조회 중 오류가 발생했습니다."));
        }
    }

    /**
     * 사용자 관심 뉴스 카테고리 삭제
     */
    @io.swagger.v3.oas.annotations.Operation(
            summary = "사용자 관심 뉴스 카테고리 삭제",
            description = "사용자의 모든 관심 뉴스 카테고리를 삭제합니다."
    )
    @DeleteMapping("/{deviceId}")
    public ResponseEntity<ApiResponse<Void>> deleteUserNewsPreferences(
            @PathVariable UUID deviceId) {
        try {
            log.info("사용자 관심 뉴스 카테고리 삭제 요청: {}", deviceId);
            userNewsService.deleteUserNewsPreferences(deviceId);
            return ResponseEntity.ok(ApiResponse.success("사용자 관심 뉴스 카테고리 삭제가 완료되었습니다.", null));
        } catch (NoSuchElementException e) {
            log.warn("사용자 관심 뉴스 카테고리 삭제 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("사용자 관심 뉴스 카테고리 삭제 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("사용자 관심 뉴스 카테고리 삭제 중 오류가 발생했습니다."));
        }
    }
}
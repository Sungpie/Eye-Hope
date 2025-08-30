package com.newsapp.eyehope.api.controller;

import com.newsapp.eyehope.api.dto.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@io.swagger.v3.oas.annotations.tags.Tag(name = "HealthCheck API", description = "상태 검사용 API")
@RestController
@RequestMapping("/api")
public class HealthController {

    @io.swagger.v3.oas.annotations.Operation(
            summary = "로드 밸런서 상태 검사 API",
            description = "AWS ELB 사용을 위한 상태 검사 API입니다."
    )
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<String>> healthCheck(){
        return ResponseEntity.ok(ApiResponse.success("Success Health Check!"));
    }
}

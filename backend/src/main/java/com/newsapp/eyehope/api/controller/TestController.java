package com.newsapp.eyehope.api.controller;

import com.newsapp.eyehope.api.dto.ApiResponse;
import com.newsapp.eyehope.api.service.GeminiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@io.swagger.v3.oas.annotations.tags.Tag(name = "Test API", description = "테스트용 API")
public class TestController {

    private final GeminiService geminiService;

    @Autowired
    public TestController(GeminiService geminiService) {
        this.geminiService = geminiService;
    }

    @io.swagger.v3.oas.annotations.Operation(
        summary = "테스트 엔드포인트",
        description = "애플리케이션이 정상적으로 동작하는지 확인하는 간단한 테스트 엔드포인트입니다."
    )

    @GetMapping("/test")
    public ResponseEntity<ApiResponse<String>> test() {
        return ResponseEntity.ok(ApiResponse.success("test_application"));
    }


    @io.swagger.v3.oas.annotations.Operation(
        summary = "Gemini AI 테스트",
        description = "Gemini AI 모델을 사용하여 텍스트를 생성합니다."
    )
    @GetMapping("/gemini")
    public ResponseEntity<ApiResponse<String>> testGemini(
            @io.swagger.v3.oas.annotations.Parameter(
                    description = "AI에게 전달할 프롬프트",
                    example = "Tell me about artificial intelligence"
            )
            @RequestParam(defaultValue = "Tell me about artificial intelligence") String prompt) {
        String generatedContent = geminiService.generateContent(prompt);
        return ResponseEntity.ok(ApiResponse.success(generatedContent));
    }

}

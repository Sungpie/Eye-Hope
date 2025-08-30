
package com.newsapp.eyehope.api.controller;

import com.newsapp.eyehope.api.dto.ApiResponse;
import com.newsapp.eyehope.api.dto.PostsResponseDto;
import com.newsapp.eyehope.api.service.NewsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/news")
@RequiredArgsConstructor
@io.swagger.v3.oas.annotations.tags.Tag(name = "News API", description = "뉴스 관련 API")
public class NewsController {

    private final NewsService newsService;

    /**
     * 모든 뉴스 조회
     */
    @io.swagger.v3.oas.annotations.Operation(
        summary = "모든 뉴스 조회",
        description = "데이터베이스에 저장된 모든 뉴스 기사를 조회합니다."
    )
    @GetMapping
    public ResponseEntity<ApiResponse<List<PostsResponseDto>>> getAllNews() {
        log.info("모든 뉴스 조회 요청");
        List<PostsResponseDto> news = newsService.getAllNews();
        return ResponseEntity.ok(ApiResponse.success(news));
    }

    /**
     * 최신 뉴스 조회
     */
    @io.swagger.v3.oas.annotations.Operation(
        summary = "최신 뉴스 조회",
        description = "최신 뉴스를 지정된 개수만큼 조회합니다."
    )
    @GetMapping("/latest")
    public ResponseEntity<ApiResponse<List<PostsResponseDto>>> getLatestNews(
            @io.swagger.v3.oas.annotations.Parameter(description = "조회할 뉴스 개수", example = "10")
            @RequestParam(defaultValue = "10") int limit) {
        log.info("최신 뉴스 조회 요청, limit={}", limit);
        List<PostsResponseDto> news = newsService.getLatestNews(limit);
        return ResponseEntity.ok(ApiResponse.success("최신 뉴스 조회 성공", news));
    }

    /**
     * 카테고리별 뉴스 조회
     */
    @io.swagger.v3.oas.annotations.Operation(
        summary = "카테고리별 뉴스 조회",
        description = "특정 카테고리에 속한 뉴스를 페이지네이션하여 조회합니다."
    )
    @GetMapping("/category/{category}")
    public ResponseEntity<ApiResponse<List<PostsResponseDto>>> getNewsByCategory(
            @io.swagger.v3.oas.annotations.Parameter(description = "뉴스 카테고리", example = "politics")
            @PathVariable String category,
            @io.swagger.v3.oas.annotations.Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @io.swagger.v3.oas.annotations.Parameter(description = "페이지 크기", example = "10")
            @RequestParam(defaultValue = "10") int size) {
        log.info("카테고리별 뉴스 조회 요청, category={}, page={}, size={}", category, page, size);
        List<PostsResponseDto> news = newsService.getNewsByCategory(category, page, size);
        return ResponseEntity.ok(ApiResponse.success(category + " 카테고리 뉴스 조회 성공", news));
    }

    /**
     * 키워드로 뉴스 검색
     */
    @io.swagger.v3.oas.annotations.Operation(
        summary = "키워드로 뉴스 검색",
        description = "제목이나 내용에 특정 키워드가 포함된 뉴스를 검색합니다."
    )
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<PostsResponseDto>>> searchNews(
            @io.swagger.v3.oas.annotations.Parameter(description = "검색 키워드", example = "경제")
            @RequestParam String keyword,
            @io.swagger.v3.oas.annotations.Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @io.swagger.v3.oas.annotations.Parameter(description = "페이지 크기", example = "10")
            @RequestParam(defaultValue = "10") int size) {
        log.info("뉴스 검색 요청, keyword={}, page={}, size={}", keyword, page, size);
        List<PostsResponseDto> news = newsService.searchNews(keyword, page, size);
        return ResponseEntity.ok(ApiResponse.success("'" + keyword + "' 검색 결과", news));
    }

    /**
     * 뉴스 상세 조회
     */
    @io.swagger.v3.oas.annotations.Operation(
        summary = "뉴스 상세 조회",
        description = "특정 ID의 뉴스 상세 정보를 조회합니다."
    )
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PostsResponseDto>> getNewsDetail(
            @io.swagger.v3.oas.annotations.Parameter(description = "뉴스 ID", example = "1")
            @PathVariable Long id) {
        log.info("뉴스 상세 조회 요청, id={}", id);
        PostsResponseDto news = newsService.getNewsDetail(id);
        return ResponseEntity.ok(ApiResponse.success("뉴스 상세 조회 성공", news));
    }

    /**
     * 뉴스 수집 트리거
     */
    @io.swagger.v3.oas.annotations.Operation(
        summary = "뉴스 수집 트리거",
        description = "뉴스 수집 프로세스를 수동으로 트리거합니다."
    )
    @PostMapping("/collect")
    public ResponseEntity<ApiResponse<String>> collectNews() {
        log.info("뉴스 수집 요청");
        String result = newsService.collectNews();
        return ResponseEntity.ok(ApiResponse.success("뉴스 수집 결과", result));
    }
}

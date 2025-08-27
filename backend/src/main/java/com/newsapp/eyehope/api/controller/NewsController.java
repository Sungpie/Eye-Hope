
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
public class NewsController {

    private final NewsService newsService;

    /**
     * 모든 뉴스 조회
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<PostsResponseDto>>> getAllNews() {
        log.info("모든 뉴스 조회 요청");
        List<PostsResponseDto> news = newsService.getAllNews();
        return ResponseEntity.ok(ApiResponse.success(news));
    }

    /**
     * 최신 뉴스 조회
     */
    @GetMapping("/latest")
    public ResponseEntity<ApiResponse<List<PostsResponseDto>>> getLatestNews(
            @RequestParam(defaultValue = "10") int limit) {
        log.info("최신 뉴스 조회 요청, limit={}", limit);
        List<PostsResponseDto> news = newsService.getLatestNews(limit);
        return ResponseEntity.ok(ApiResponse.success("최신 뉴스 조회 성공", news));
    }

    /**
     * 카테고리별 뉴스 조회
     */
    @GetMapping("/category/{category}")
    public ResponseEntity<ApiResponse<List<PostsResponseDto>>> getNewsByCategory(
            @PathVariable String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("카테고리별 뉴스 조회 요청, category={}, page={}, size={}", category, page, size);
        List<PostsResponseDto> news = newsService.getNewsByCategory(category, page, size);
        return ResponseEntity.ok(ApiResponse.success(category + " 카테고리 뉴스 조회 성공", news));
    }

    /**
     * 키워드로 뉴스 검색
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<PostsResponseDto>>> searchNews(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("뉴스 검색 요청, keyword={}, page={}, size={}", keyword, page, size);
        List<PostsResponseDto> news = newsService.searchNews(keyword, page, size);
        return ResponseEntity.ok(ApiResponse.success("'" + keyword + "' 검색 결과", news));
    }

    /**
     * 뉴스 상세 조회
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PostsResponseDto>> getNewsDetail(@PathVariable Long id) {
        log.info("뉴스 상세 조회 요청, id={}", id);
        PostsResponseDto news = newsService.getNewsDetail(id);
        return ResponseEntity.ok(ApiResponse.success("뉴스 상세 조회 성공", news));
    }

    /**
     * 뉴스 수집 트리거
     */
    @PostMapping("/collect")
    public ResponseEntity<ApiResponse<String>> collectNews() {
        log.info("뉴스 수집 요청");
        String result = newsService.collectNews();
        return ResponseEntity.ok(ApiResponse.success("뉴스 수집 결과", result));
    }
}


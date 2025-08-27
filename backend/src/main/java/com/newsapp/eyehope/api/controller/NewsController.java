package com.newsapp.eyehope.api.controller;

import com.newsapp.eyehope.api.dto.PostsRequestDto;
import com.newsapp.eyehope.api.dto.PostsResponseDto;
import com.newsapp.eyehope.api.service.NewsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@RestController
@RequestMapping("/api/news")
@RequiredArgsConstructor
public class NewsController {
    private final NewsService newsService;
    /**
     * 수동으로 뉴스 수집 실행
     */
    @PostMapping("/collect")
    public ResponseEntity<String> collectNews() {
        String result = newsService.collectNews();
        return ResponseEntity.ok(result);
    }

    /**
     * 모든 뉴스 조회
     */
    @GetMapping
    public ResponseEntity<List<PostsResponseDto>> getAllNews() {
        List<PostsResponseDto> news = newsService.getAllNews();
        return ResponseEntity.ok(news);
    }

    /**
     * 최신 뉴스 조회

    @GetMapping("/latest")
    public ResponseEntity<List<PostsResponseDto>> getLatestNews(
            @RequestParam(defaultValue = "10") int limit) {
        List<PostsResponseDto> news = newsService.getLatestNews(limit);
        return ResponseEntity.ok(news);
    }
            */
}

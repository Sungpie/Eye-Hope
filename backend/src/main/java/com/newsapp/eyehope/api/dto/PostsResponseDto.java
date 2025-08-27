package com.newsapp.eyehope.api.dto;

import com.newsapp.eyehope.api.domain.Posts;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class PostsResponseDto {
    private long id;
    private String source;
    private String title;
    private String content;
    private LocalDateTime createdAt;
    private String url;
    private String category; // 실제로는 newsId를 변환한 카테고리 문자열
    private LocalDateTime collectedAt;

    public PostsResponseDto(Posts entity) {
        this.id = entity.getId();
        this.source = entity.getSource();
        this.title = entity.getTitle();
        this.content = entity.getContent();
        this.createdAt = entity.getCreatedAt();
        this.url = entity.getUrl();
        this.category = entity.getCategory();
        this.collectedAt = entity.getCollectedAt();
    }
}

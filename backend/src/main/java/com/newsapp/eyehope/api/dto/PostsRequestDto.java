package com.newsapp.eyehope.api.dto;

import com.newsapp.eyehope.api.domain.Posts;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostsRequestDto {
    private String title;
    private String content;
    private String source;
    private String url;
    private LocalDateTime createdAt;
    private Long newsId;


    public Posts toEntity() {
        Posts entity = new Posts();
        entity.setTitle(title);
        entity.setContent(content);
        entity.setSource(source);
        entity.setUrl(url);
        entity.setCreatedAt(createdAt);
        entity.setCollectedAt(LocalDateTime.now()); // 현재 시간으로 collected_at 설정

        // newsId 설정
        if (newsId != null) {
            entity.setNewsId(newsId);
        }

        return entity;
    }
}

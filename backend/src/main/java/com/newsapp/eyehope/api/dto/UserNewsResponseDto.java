package com.newsapp.eyehope.api.dto;

import com.newsapp.eyehope.api.domain.News;
import com.newsapp.eyehope.api.domain.UsersNews;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserNewsResponseDto {
    private UUID deviceId;
    private List<NewsDto> news;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NewsDto {
        private Long id;
        private String category;
        private String pressName;
    }

    public static UserNewsResponseDto fromEntities(UUID deviceId, List<UsersNews> usersNews) {
        List<NewsDto> newsDtos = usersNews.stream()
                .map(un -> {
                    News news = un.getNews();
                    if (news == null) {
                        // Skip this entry if news is null
                        return null;
                    }
                    return NewsDto.builder()
                            .id(news.getId())
                            .category(news.getCategory())
                            .pressName(news.getPressName())
                            .build();
                })
                .filter(newsDto -> newsDto != null)
                .collect(Collectors.toList());

        return UserNewsResponseDto.builder()
                .deviceId(deviceId)
                .news(newsDtos)
                .build();
    }
}

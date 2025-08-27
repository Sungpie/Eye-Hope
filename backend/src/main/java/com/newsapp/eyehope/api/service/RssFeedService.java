package com.newsapp.eyehope.api.service;

import com.newsapp.eyehope.api.config.RssFeedConfig;
import com.newsapp.eyehope.api.dto.PostsRequestDto;
import com.newsapp.eyehope.api.repository.NewsRepository;
import com.newsapp.eyehope.api.domain.News;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.Date;

@Slf4j
@Service
@RequiredArgsConstructor
public class RssFeedService {

    private final RssFeedConfig.FeedProvider feedProvider;
    private final NewsRepository newsRepository;
    /**
     * 모든 RSS 피드에서 기사 수집
     */
    public Map<String, List<PostsRequestDto>> fetchAllFeedsByCategory() {
        Map<String, List<PostsRequestDto>> feedsByCategory = new HashMap<>();

        for (RssFeedConfig.FeedInfo feedInfo : feedProvider.getFeeds()) {
            try {
                List<PostsRequestDto> posts = fetchSingleFeed(feedInfo);

                // 카테고리별로 그룹화
                feedsByCategory.computeIfAbsent(feedInfo.getCategory(), k -> new ArrayList<>())
                        .addAll(posts);

                log.info("[{}] {} - {}개 기사 수집",
                        feedInfo.getSource(), feedInfo.getCategory(), posts.size());

            } catch (Exception e) {
                log.error("[{}] {} 수집 실패: {}",
                        feedInfo.getSource(), feedInfo.getCategory(), e.getMessage());
            }
        }

        // 결과 로깅
        feedsByCategory.forEach((category, posts) ->
                log.info("카테고리 [{}]: 총 {}개 기사", category, posts.size())
        );

        return feedsByCategory;
    }

    /**
     * 특정 카테고리의 RSS 피드만 수집
     */
    public List<PostsRequestDto> fetchFeedsByCategory(String category) {
        List<PostsRequestDto> categoryPosts = new ArrayList<>();

        feedProvider.getFeeds().stream()
                .filter(feedInfo -> feedInfo.getCategory().equals(category))
                .forEach(feedInfo -> {
                    try {
                        List<PostsRequestDto> posts = fetchSingleFeed(feedInfo);
                        categoryPosts.addAll(posts);
                        log.info("[{}] {} - {}개 기사 수집",
                                feedInfo.getSource(), category, posts.size());
                    } catch (Exception e) {
                        log.error("수집 실패 - [{}] {}: {}",
                                feedInfo.getSource(), category, e.getMessage());
                    }
                });

        return categoryPosts;
    }

    /**
     * 단일 RSS 피드 파싱
     */
    private List<PostsRequestDto> fetchSingleFeed(RssFeedConfig.FeedInfo feedInfo)
            throws Exception {
        List<PostsRequestDto> posts = new ArrayList<>();

        // Rome 라이브러리로 RSS 파싱
        SyndFeedInput input = new SyndFeedInput();
        SyndFeed feed = input.build(new XmlReader(new URL(feedInfo.getUrl())));

        for (SyndEntry entry : feed.getEntries()) {
            Long newsId = getCategoryId(feedInfo.getCategory());

            PostsRequestDto dto = PostsRequestDto.builder()
                    .source(feedInfo.getSource())  // 언론사 이름 (press 테이블의 name)
                    .title(cleanText(entry.getTitle()))  // 제목
                    .content(extractDescription(entry))  // 내용 (RSS의 description)
                    .createdAt(convertToLocalDateTime(entry.getPublishedDate()))  // 발행시간을 LocalDateTime으로
                    .url(entry.getLink())  // 기사 URL
                    .newsId(newsId)  // News 테이블 ID 설정
                    .build();

            posts.add(dto);
        }

        return posts;
    }

    /**
     * HTML 태그 제거 및 텍스트 정리
     */
    private String cleanText(String text) {
        if (text == null) {
            return "";
        }
        // HTML 태그 제거
        String result = text.replaceAll("<[^>]*>", "");
        // 연속된 공백 제거
        result = result.replaceAll("\\s+", " ");
        // 앞뒤 공백 제거
        return result.trim();
    }

    /**
     * SyndEntry에서 설명 추출
     * RSS에서는 content를 제공하지 않으므로 placeholder 값을 반환
     */
    private String extractDescription(SyndEntry entry) {
        if (entry.getDescription() != null) {
            return cleanText(entry.getDescription().getValue());
        }
        // RSS에서 content를 제공하지 않는 경우 placeholder 값 반환
        return "Content not available in RSS feed. Please visit the article URL for full content.";
    }

    /**
     * Date를 LocalDateTime으로 변환
     */
    private LocalDateTime convertToLocalDateTime(Date date) {
        if (date == null) {
            return null;
        }
        return date.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
    }

    /**
     * 카테고리 문자열로 해당 News 엔티티의 ID 찾기
     */
    private Long getCategoryId(String category) {
        if (category == null) return null;

        return newsRepository.findByCategory(category)
                .map(News::getId)
                .orElse(null);
    }
}

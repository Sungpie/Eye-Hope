package com.newsapp.eyehope.api.service;

import com.newsapp.eyehope.api.domain.Posts;
import com.newsapp.eyehope.api.dto.PostsRequestDto;
import com.newsapp.eyehope.api.dto.PostsResponseDto;
import com.newsapp.eyehope.api.repository.PostsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NewsService {
    private final RssFeedService rssFeedService;
    private final PostsRepository postsRepository;

    // 전체 수집
    public void collectAllNews() {
        Map<String, List<PostsRequestDto>> feedsByCategory =
                rssFeedService.fetchAllFeedsByCategory();
        feedsByCategory.forEach((category, posts) -> {
            savePosts(posts);
        });
    }

    // 특정 카테고리만 수집
    public void collectNewsByCategory(String category) {
        List<PostsRequestDto> posts =
                rssFeedService.fetchFeedsByCategory(category);
        savePosts(posts);
    }

    private void savePosts(List<PostsRequestDto> posts) {
        for (PostsRequestDto dto : posts) {
            if (!postsRepository.existsByUrl(dto.getUrl())) {
                // 뉴스 ID가 설정되어 있는지 확인
                if (dto.getNewsId() == null) {
                    // NewsId가 없는 경우 기본값 설정 (예: 기타 카테고리)
                    dto.setNewsId(8L); // 기본값으로 오피니언 카테고리 설정
                }
                postsRepository.save(dto.toEntity());
            }
        }
    }


    /**
     * 수동으로 뉴스 수집 실행
     */
    public String collectNews() {
        collectAllNews();
        return "뉴스 수집이 완료되었습니다.";
    }

    /**
     * 모든 뉴스 조회
     */
    public List<PostsResponseDto> getAllNews() {
        List<Posts> posts = postsRepository.findAll();
        return posts.stream()
                .map(PostsResponseDto::new)
                .collect(Collectors.toList());
    }

    /**
     * 최신 뉴스 조회

    public List<PostsResponseDto> getLatestNews(int limit) {
        PageRequest pageRequest = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "collected_at"));
        List<Posts> posts = postsRepository.findAll(pageRequest).getContent();
        return posts.stream()
                .map(PostsResponseDto::new)
                .collect(Collectors.toList());
    }*/
}

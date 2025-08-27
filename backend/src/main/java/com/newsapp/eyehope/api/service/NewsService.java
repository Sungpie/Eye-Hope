package com.newsapp.eyehope.api.service;

import com.newsapp.eyehope.api.domain.Posts;
import com.newsapp.eyehope.api.dto.PostsRequestDto;
import com.newsapp.eyehope.api.dto.PostsResponseDto;
import com.newsapp.eyehope.api.repository.PostsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class NewsService {
    private final RssFeedService rssFeedService;
    private final PostsRepository postsRepository;
    private final GeminiService geminiService;

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

                try {
                    // URL을 사용하여 Gemini API로 내용 요약
                    String summarizedContent = summarizeNewsContent(dto.getUrl(), dto.getTitle());

                    // 요약된 내용이 있으면 DTO의 content 필드 업데이트
                    if (summarizedContent != null && !summarizedContent.isEmpty() &&
                        !summarizedContent.startsWith("Error")) {
                        dto.setContent(summarizedContent);
                        log.info("뉴스 요약 성공: {}", dto.getTitle());
                    } else {
                        log.warn("뉴스 요약 실패, 원본 내용 유지: {}", dto.getTitle());
                    }
                } catch (Exception e) {
                    log.error("뉴스 요약 중 오류 발생: {}", e.getMessage());
                }

                postsRepository.save(dto.toEntity());
            }
        }
    }

    /**
     * Gemini API를 사용하여 뉴스 URL의 내용을 요약
     * @param url 뉴스 기사 URL
     * @param title 뉴스 제목
     * @return 요약된 내용
     */
    private String summarizeNewsContent(String url, String title) {
        String prompt = String.format(
            "다음 뉴스 기사 URL을 분석하고 내용을 요약해주세요. 요약은 한국어로 작성하고, 3-4문장 정도로 간결하게 작성해주세요."+
                    "절대 다른 말을 추가하지 말고 요약만 출력하세요.\n" +
                    " '알겠습니다', '요약:', '**' 같은 표현 금지."+
            "고유명사/수치/날짜 를 유지해주세요. 과장, 의견, 추측 금지 (본문에 없는 내용 금지). 문장을 짧게, 청각 사용자(TTS) 친화적으로 작성. URL: %s, 제목: %s",
            url, title
        );

        return geminiService.generateContent(prompt);
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
     */
    public List<PostsResponseDto> getLatestNews(int limit) {
        PageRequest pageRequest = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "collectedAt"));
        List<Posts> posts = postsRepository.findAll(pageRequest).getContent();
        return posts.stream()
                .map(PostsResponseDto::new)
                .collect(Collectors.toList());
    }

    /**
     * 카테고리별 뉴스 조회
     */
    public List<PostsResponseDto> getNewsByCategory(String category, int page, int size) {
        Long newsId = getCategoryId(category);
        if (newsId == null) {
            return List.of(); // 잘못된 카테고리인 경우 빈 목록 반환
        }

        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "collectedAt"));
        List<Posts> posts = postsRepository.findByNewsId(newsId, pageRequest);
        return posts.stream()
                .map(PostsResponseDto::new)
                .collect(Collectors.toList());
    }

    /**
     * 키워드로 뉴스 검색
     */
    public List<PostsResponseDto> searchNews(String keyword, int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "collectedAt"));
        List<Posts> posts = postsRepository.searchByKeyword(keyword, pageRequest);
        return posts.stream()
                .map(PostsResponseDto::new)
                .collect(Collectors.toList());
    }

    /**
     * 뉴스 상세 조회
     */
    public PostsResponseDto getNewsDetail(Long id) {
        Posts post = postsRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 뉴스가 존재하지 않습니다. id=" + id));
        return new PostsResponseDto(post);
    }

    /**
     * 카테고리 문자열을 ID로 변환
     */
    private Long getCategoryId(String category) {
        return switch (category) {
            case "경제" -> 1L;
            case "증권" -> 2L;
            case "스포츠" -> 3L;
            case "연예" -> 4L;
            case "정치" -> 5L;
            case "IT" -> 6L;
            case "사회" -> 7L;
            case "오피니언" -> 8L;
            default -> null;
        };
    }
}

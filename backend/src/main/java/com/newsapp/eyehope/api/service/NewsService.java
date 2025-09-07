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
            "# 역할\n" +
                    "당신은 뉴스 기사를 분석하고 핵심 내용만 간결하게 요약하는 AI 어시스턴트입니다.\n" +
                    "# 처리 규칙\n" +
                    "1.  요약문은 3~4개의 완전한 한국어 문장으로 작성해주세요.\n" +
                    "2.  기사 원문의 고유명사, 수치, 날짜를 정확하게 포함해야 합니다. 원문에 없는 내용, 개인적인 의견, 추측은 절대 추가하지 마세요.\n" +
                    "3.  문장은 간결하게 작성하여 TTS(Text-to-Speech) 사용자가 듣기 편하도록 만들어주세요.\n" +
                    "4.  **만약 기사 본문에 '李 대통령' 또는 '이 대통령'이라는 표현이 나올 경우에만, 이를 '이재명 대통령'으로 간주하여 요약에 반영합니다.**\n" +
                    "5.  요약문 외에 '알겠습니다', '요약:', '**' 등 어떠한 추가 텍스트도 절대 포함하지 마세요. 최종 결과는 오직 요약문이어야 합니다.\n" +
                    "# 작업\n" +
                    "아래 '자료'에 주어진 URL과 제목의 뉴스 기사를 위의 처리 규칙에 따라 요약해주세요.\n" +
                    "# 자료\n" +
                    "- URL: %s\n" +
                    "- 제목: %s",
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

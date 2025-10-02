package com.newsapp.eyehope.api.service;

import com.newsapp.eyehope.api.domain.Posts;
import com.newsapp.eyehope.api.dto.PostsRequestDto;
import com.newsapp.eyehope.api.dto.PostsResponseDto;
import com.newsapp.eyehope.api.exception.ResourceNotFoundException;
import com.newsapp.eyehope.api.repository.PostsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NewsService {
    private final RssFeedService rssFeedService;
    private final PostsRepository postsRepository;
    private final GeminiService geminiService;

    // 전체 수집
    @Transactional
    public void collectAllNews() {
        Map<String, List<PostsRequestDto>> feedsByCategory =
                rssFeedService.fetchAllFeedsByCategory();
        feedsByCategory.forEach((category, posts) -> {
            savePosts(posts);
        });
    }

    // 특정 카테고리만 수집
    @Transactional
    public void collectNewsByCategory(String category) {
        List<PostsRequestDto> posts =
                rssFeedService.fetchFeedsByCategory(category);
        savePosts(posts);
    }

    private void savePosts(List<PostsRequestDto> posts) {
        if (posts == null || posts.isEmpty()) {
            log.warn("저장할 뉴스가 없습니다.");
            return;
        }

        int successCount = 0;
        int skipCount = 0;
        int errorCount = 0;

        for (PostsRequestDto dto : posts) {
            try {
                // URL이 null이거나 비어있는지 확인
                if (dto.getUrl() == null || dto.getUrl().trim().isEmpty()) {
                    log.warn("URL이 없는 뉴스는 건너뜁니다: {}", dto.getTitle());
                    continue;
                }

                // 이미 존재하는 URL인지 확인
                if (postsRepository.existsByUrl(dto.getUrl())) {
                    skipCount++;
                    continue;
                }

                // 뉴스 ID가 설정되어 있는지 확인
                if (dto.getNewsId() == null) {
                    // NewsId가 없는 경우 기본값 설정 (예: 기타 카테고리)
                    dto.setNewsId(8L); // 기본값으로 오피니언 카테고리 설정
                    log.debug("NewsId가 없는 뉴스에 기본값 설정: {}", dto.getTitle());
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
                    log.error("뉴스 요약 중 오류 발생: {}", e.getMessage(), e);
                    // 요약 실패해도 원본 내용으로 저장 진행
                }

                // 저장 전 필수 필드 검증
                if (dto.getTitle() == null || dto.getTitle().trim().isEmpty()) {
                    log.warn("제목이 없는 뉴스는 건너뜁니다: {}", dto.getUrl());
                    continue;
                }

                postsRepository.save(dto.toEntity());
                successCount++;
            } catch (Exception e) {
                log.error("뉴스 저장 중 오류 발생: {}, URL: {}", e.getMessage(), dto.getUrl(), e);
                errorCount++;
            }
        }

        log.info("뉴스 저장 결과: 성공 {}, 건너뜀 {}, 오류 {}", successCount, skipCount, errorCount);
    }

    /**
     * 뉴스 URL에서 본문 내용을 추출
     * @param url 뉴스 기사 URL
     * @return 추출된 본문 내용
     */
    private String extractContentFromUrl(String url) {
        try {
            log.info("뉴스 URL에서 본문 추출 시작: {}", url);

            // URL에서 HTML 문서 가져오기
            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                    .timeout(10000)
                    .get();

            // 일반적인 뉴스 사이트의 본문 컨텐츠를 찾기 위한 선택자들
            // 다양한 뉴스 사이트에 대응하기 위해 여러 선택자 시도
            Elements contentElements = doc.select("article, .article, .article-body, .article-content, .news-content, .entry-content, #article-body, .news_content, .article_content, .articleBody, .article_view, #articleBody, #newsContent");

            if (!contentElements.isEmpty()) {
                // 추출된 본문에서 불필요한 요소 제거
                contentElements.select("script, style, iframe, .reporter, .share, .social, .related, .recommend, .copyright, .ad, .advertisement, .banner").remove();

                // 본문 텍스트 추출
                String content = contentElements.text();

                // 내용이 너무 길면 적절히 자르기 (Gemini API 제한 고려)
                if (content.length() > 15000) {
                    content = content.substring(0, 15000);
                }

                log.info("뉴스 본문 추출 성공: {} 글자", content.length());
                return content;
            } else {
                log.warn("뉴스 본문을 찾을 수 없음: {}", url);
                return "뉴스 본문을 추출할 수 없습니다.";
            }
        } catch (IOException e) {
            log.error("뉴스 URL에서 본문 추출 중 오류 발생: {}", e.getMessage(), e);
            return "뉴스 본문 추출 중 오류 발생: " + e.getMessage();
        } catch (Exception e) {
            log.error("뉴스 본문 추출 중 예상치 못한 오류 발생: {}", e.getMessage(), e);
            return "뉴스 본문 추출 중 예상치 못한 오류 발생: " + e.getMessage();
        }
    }

    /**
     * Gemini API를 사용하여 뉴스 URL의 내용을 요약
     * @param url 뉴스 기사 URL
     * @param title 뉴스 제목
     * @return 요약된 내용
     */
    private String summarizeNewsContent(String url, String title) {
        // URL에서 뉴스 본문 추출
        String newsContent = extractContentFromUrl(url);

        // 추출 실패 시 URL만 전달
        if (newsContent.startsWith("뉴스 본문 추출 중 오류") || newsContent.equals("뉴스 본문을 추출할 수 없습니다.")) {
            log.warn("본문 추출 실패로 URL만 전달: {}", url);
            newsContent = "URL: " + url;
        }

        String prompt = String.format(
            "# 역할\n" +
                    "당신은 뉴스 기사를 분석하고 핵심 내용만 간결하게 요약하는 AI 어시스턴트입니다.\n" +
                    "# 처리 규칙\n" +
                    "1.  요약문은 3~4개의 완전한 한국어 문장으로 작성해주세요.\n" +
                    "2.  기사 원문의 고유명사, 수치, 날짜를 정확하게 포함해야 합니다. 원문에 없는 내용, 개인적인 의견, 추측은 절대 추가하지 마세요.\n" +
                    "3.  문장은 간결하게 작성하여 TTS(Text-to-Speech) 사용자가 듣기 편하도록 만들어주세요.\n" +
                    "4.  **만약 기사 본문에 '李 대통령' 또는 '이 대통령'이라는 표현이 나올 경우에만, 이를 '이재명 대통령'으로 간주하여 요약에 반영합니다.**\n" +
                    "5.  요약문 외에 '알겠습니다', '요약:', '**' 등 어떠한 추가 텍스트도 절대 포함하지 마세요. 최종 결과는 오직 요약문이어야 합니다.\n" +
                    "6. 주어진 내용이 너무 짧거나 유의미한 정보가 없어 요약이 불가능하다면, 혹은 본문이 URL형태로 입력되어있다면 억지로 요약문을 만들지 말고 \"본문이 없는 기사입니다.\" 라고만 답변해 주세요.\n"+
                    "# 작업\n" +
                    "아래 '자료'에 주어진 뉴스 기사를 위의 처리 규칙에 따라 요약해주세요.\n" +
                    "# 자료\n" +
                    "- 제목: %s\n" +
                    "- 본문: %s",
            title, newsContent
        );

        return geminiService.generateContent(prompt);
    }


    /**
     * 수동으로 뉴스 수집 실행
     */
    @Transactional
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
                .orElseThrow(() -> new ResourceNotFoundException("News", id));
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

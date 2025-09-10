package com.newsapp.eyehope.api.config;

import com.newsapp.eyehope.api.service.NewsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NewsScheduler {

    private final NewsService newsService;

    /**
     * 10분마다 자동으로 뉴스 수집 실행
     */
    @Scheduled(fixedRate = 600000) // 10분(600,000 밀리초)마다 실행
    public void scheduleNewsCollection() {
        log.info("스케줄링된 뉴스 수집 시작");
        try {
            newsService.collectAllNews();
            log.info("스케줄링된 뉴스 수집 완료");
        } catch (Exception e) {
            log.error("스케줄링된 뉴스 수집 중 오류 발생: {}", e.getMessage(), e);
            // 스케줄러는 계속 실행되어야 하므로 예외를 다시 던지지 않음
        }
    }
}

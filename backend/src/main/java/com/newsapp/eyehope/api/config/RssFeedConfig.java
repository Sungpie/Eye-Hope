package com.newsapp.eyehope.api.config;

import com.newsapp.eyehope.api.domain.News;
import com.newsapp.eyehope.api.repository.NewsRepository;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class RssFeedConfig {

    @Getter
    @Setter
    public static class FeedInfo {
        private String url;
        private String category;
        private String source;

        public static FeedInfo fromNews(News news) {
            FeedInfo feedInfo = new FeedInfo();
            feedInfo.setUrl(news.getRss());
            feedInfo.setCategory(news.getCategory());
            feedInfo.setSource(news.getPressName());
            return feedInfo;
        }
    }

    @Component
    @RequiredArgsConstructor
    public static class FeedProvider {
        private final NewsRepository newsRepository;

        public List<FeedInfo> getFeeds() {
            List<FeedInfo> feeds = new ArrayList<>();
            List<News> newsEntities = newsRepository.findAllWithPressOrderByCategory();

            for (News news : newsEntities) {
                if (news.getRss() != null && !news.getRss().isEmpty()) {
                    feeds.add(FeedInfo.fromNews(news));
                }
            }

            return feeds;
        }
    }
}

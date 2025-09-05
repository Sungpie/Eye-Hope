package com.newsapp.eyehope.api.service;

import com.newsapp.eyehope.api.domain.News;
import com.newsapp.eyehope.api.domain.User;
import com.newsapp.eyehope.api.domain.UsersNews;
import com.newsapp.eyehope.api.dto.UserNewsRequestDto;
import com.newsapp.eyehope.api.dto.UserNewsResponseDto;
import com.newsapp.eyehope.api.repository.NewsRepository;
import com.newsapp.eyehope.api.repository.UserRepository;
import com.newsapp.eyehope.api.repository.UsersNewsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserNewsService {

    private final UsersNewsRepository usersNewsRepository;
    private final UserRepository userRepository;
    private final NewsRepository newsRepository;

    /**
     * 사용자의 관심 뉴스 카테고리 저장
     */
    @Transactional
    public UserNewsResponseDto saveUserNewsPreferences(UserNewsRequestDto requestDto) {
        UUID deviceId = requestDto.getDeviceId();
        List<Long> newsIds = requestDto.getNewsIds();

        // 사용자 존재 여부 확인
        User user = userRepository.findByDeviceId(deviceId)
                .orElseThrow(() -> new NoSuchElementException("해당 디바이스 ID로 등록된 사용자가 없습니다: " + deviceId));

        // 기존 관심 뉴스 카테고리 삭제
        usersNewsRepository.deleteByDeviceId(deviceId);

        // 새로운 관심 뉴스 카테고리 저장
        List<UsersNews> savedPreferences = new ArrayList<>();
        for (Long newsId : newsIds) {
            // 뉴스 존재 여부 확인
            if (!newsRepository.existsById(newsId)) {
                throw new NoSuchElementException("해당 뉴스 ID가 존재하지 않습니다: " + newsId);
            }

            UsersNews usersNews = UsersNews.builder()
                    .deviceId(deviceId)
                    .newsId(newsId)
                    .build();

            savedPreferences.add(usersNewsRepository.save(usersNews));
        }

        log.info("사용자 관심 뉴스 카테고리 저장 완료: {}, 카테고리 수: {}", deviceId, newsIds.size());

        // 저장된 관심 뉴스 카테고리 조회 및 반환
        return getUserNewsPreferences(deviceId);
    }

    /**
     * 사용자의 관심 뉴스 카테고리 조회
     */
    @Transactional(readOnly = true)
    public UserNewsResponseDto getUserNewsPreferences(UUID deviceId) {
        // 사용자 존재 여부 확인
        if (!userRepository.existsByDeviceId(deviceId)) {
            throw new NoSuchElementException("해당 디바이스 ID로 등록된 사용자가 없습니다: " + deviceId);
        }

        // 사용자의 관심 뉴스 카테고리 조회 (News 엔티티 함께 로드)
        List<UsersNews> usersNews = usersNewsRepository.findByDeviceIdWithNews(deviceId);

        log.info("사용자 관심 뉴스 카테고리 조회 완료: {}, 카테고리 수: {}", deviceId, usersNews.size());

        return UserNewsResponseDto.fromEntities(deviceId, usersNews);
    }

    /**
     * 사용자의 관심 뉴스 카테고리 삭제
     */
    @Transactional
    public void deleteUserNewsPreferences(UUID deviceId) {
        // 사용자 존재 여부 확인
        if (!userRepository.existsByDeviceId(deviceId)) {
            throw new NoSuchElementException("해당 디바이스 ID로 등록된 사용자가 없습니다: " + deviceId);
        }

        // 사용자의 관심 뉴스 카테고리 삭제
        usersNewsRepository.deleteByDeviceId(deviceId);

        log.info("사용자 관심 뉴스 카테고리 삭제 완료: {}", deviceId);
    }
}

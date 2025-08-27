package com.newsapp.eyehope.api.repository;

import com.newsapp.eyehope.api.domain.Posts;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PostsRepository extends JpaRepository<Posts, Long> {
    Optional<Posts> findByUrl(String url); // 중복 체크용
    boolean existsByUrl(String url);

    // 카테고리별 뉴스 조회
    List<Posts> findByNewsId(Long newsId, Pageable pageable);

    // 검색 기능
    @Query("SELECT p FROM Posts p WHERE p.title LIKE %:keyword% OR p.content LIKE %:keyword%")
    List<Posts> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);
}

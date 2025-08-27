package com.newsapp.eyehope.api.repository;

import com.newsapp.eyehope.api.domain.News;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NewsRepository extends JpaRepository<News, Long> {
    Optional<News> findByCategory(String category);

    // 카테고리 순으로 정렬하여 모든 뉴스 조회 (프레스 정보 함께 가져오기)
    @Query("SELECT n FROM News n JOIN FETCH n.press ORDER BY n.category")
    List<News> findAllWithPressOrderByCategory();

    // press_id 순으로 정렬
    List<News> findAllByOrderByPressIdAsc();

    // 카테고리 순으로 정렬
    List<News> findAllByOrderByCategoryAsc();
}

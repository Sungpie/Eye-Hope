package com.newsapp.eyehope.api.repository;

import com.newsapp.eyehope.api.domain.Posts;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PostsRepository extends JpaRepository<Posts, Long> {
    Optional<Posts> findByUrl(String url); // 중복 체크용
    boolean existsByUrl(String url);
}

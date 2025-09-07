package com.newsapp.eyehope.api.repository;

import com.newsapp.eyehope.api.domain.Topic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TopicRepository extends JpaRepository<Topic, Long> {
    Optional<Topic> findByTopicName(String topicName);
    boolean existsByTopicName(String topicName);
}
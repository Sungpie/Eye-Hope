package com.newsapp.eyehope.api.repository;

import com.newsapp.eyehope.api.domain.Topic;
import com.newsapp.eyehope.api.domain.User;
import com.newsapp.eyehope.api.domain.UserTopic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserTopicRepository extends JpaRepository<UserTopic, Long> {
    List<UserTopic> findByUser(User user);
    List<UserTopic> findByTopic(Topic topic);
    Optional<UserTopic> findByUserAndTopic(User user, Topic topic);
    boolean existsByUserAndTopic(User user, Topic topic);
    void deleteByUserAndTopic(User user, Topic topic);
}
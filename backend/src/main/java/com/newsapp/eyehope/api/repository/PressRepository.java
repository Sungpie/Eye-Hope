package com.newsapp.eyehope.api.repository;

import com.newsapp.eyehope.api.domain.Press;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PressRepository extends JpaRepository<Press, Long> {
    Optional<Press> findByName(String name);
}

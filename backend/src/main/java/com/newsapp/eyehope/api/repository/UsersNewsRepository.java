package com.newsapp.eyehope.api.repository;

import com.newsapp.eyehope.api.domain.UsersNews;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface UsersNewsRepository extends JpaRepository<UsersNews, Long> {
    List<UsersNews> findByDeviceId(UUID deviceId);

    @Query("SELECT un FROM UsersNews un JOIN FETCH un.news WHERE un.deviceId = :deviceId")
    List<UsersNews> findByDeviceIdWithNews(@Param("deviceId") UUID deviceId);

    boolean existsByDeviceIdAndNewsId(UUID deviceId, Long newsId);

    void deleteByDeviceIdAndNewsId(UUID deviceId, Long newsId);

    void deleteByDeviceId(UUID deviceId);
}

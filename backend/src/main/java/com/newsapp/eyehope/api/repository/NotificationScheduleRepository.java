package com.newsapp.eyehope.api.repository;

import com.newsapp.eyehope.api.domain.NotificationSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface NotificationScheduleRepository extends JpaRepository<NotificationSchedule, Long> {
    
    /**
     * Find all notification schedules for a specific device
     * 
     * @param deviceId the device ID
     * @return list of notification schedules
     */
    List<NotificationSchedule> findByDeviceId(UUID deviceId);
    
    /**
     * Delete all notification schedules for a specific device
     * 
     * @param deviceId the device ID
     */
    void deleteByDeviceId(UUID deviceId);
}
package com.newsapp.eyehope.api.service;

import com.newsapp.eyehope.api.domain.NotificationSchedule;
import com.newsapp.eyehope.api.domain.Topic;
import com.newsapp.eyehope.api.domain.User;
import com.newsapp.eyehope.api.dto.NotificationScheduleRequestDto;
import com.newsapp.eyehope.api.dto.NotificationScheduleResponseDto;
import com.newsapp.eyehope.api.repository.NotificationScheduleRepository;
import com.newsapp.eyehope.api.repository.TopicRepository;
import com.newsapp.eyehope.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationScheduleService {

    private final NotificationScheduleRepository notificationScheduleRepository;
    private final UserRepository userRepository;
    private final TopicRepository topicRepository;
    private final FCMService fcmService;

    /**
     * Save notification schedules for a device
     * 
     * @param requestDto the request DTO containing device ID and notification times
     * @return the response DTO containing the saved notification schedules
     */
    @Transactional
    public NotificationScheduleResponseDto saveNotificationSchedules(NotificationScheduleRequestDto requestDto) {
        UUID deviceId = requestDto.getDeviceId();
        List<String> notificationTimeStrings = requestDto.getNotificationTime();

        // Verify that the device exists
        User user = userRepository.findById(deviceId)
                .orElseThrow(() -> new IllegalArgumentException("Device with ID " + deviceId + " not found"));

        // Delete existing notification schedules for this device
        notificationScheduleRepository.deleteByDeviceId(deviceId);

        // Create and save new notification schedules
        List<NotificationSchedule> schedules = notificationTimeStrings.stream()
                .map(timeStr -> NotificationSchedule.builder()
                        .deviceId(deviceId)
                        .notificationTime(LocalTime.parse(timeStr))
                        .build())
                .collect(Collectors.toList());

        notificationScheduleRepository.saveAll(schedules);

        // Subscribe user to daily_news topic (id 2)
        try {
            // Get the daily_news topic (id 2)
            Topic dailyNewsTopic = topicRepository.findById(2L)
                    .orElseThrow(() -> new IllegalArgumentException("Daily news topic with ID 2 not found"));

            // Subscribe user to the topic
            fcmService.subscribeUserToTopic(deviceId, dailyNewsTopic.getTopicName());
            log.info("User with device ID {} subscribed to topic {}", deviceId, dailyNewsTopic.getTopicName());
        } catch (Exception e) {
            log.error("Failed to subscribe user with device ID {} to daily_news topic: {}", deviceId, e.getMessage(), e);
            // Continue with the process even if subscription fails
        }

        // Return the response DTO
        return NotificationScheduleResponseDto.from(deviceId, notificationTimeStrings);
    }

    /**
     * Get notification schedules for a device
     * 
     * @param deviceId the device ID
     * @return the response DTO containing the notification schedules
     */
    @Transactional(readOnly = true)
    public NotificationScheduleResponseDto getNotificationSchedules(UUID deviceId) {
        // Verify that the device exists
        userRepository.findById(deviceId)
                .orElseThrow(() -> new IllegalArgumentException("Device with ID " + deviceId + " not found"));

        // Get notification schedules for this device
        List<NotificationSchedule> schedules = notificationScheduleRepository.findByDeviceId(deviceId);

        // Extract notification times as strings
        List<String> notificationTimeStrings = schedules.stream()
                .map(schedule -> schedule.getNotificationTime().toString())
                .collect(Collectors.toList());

        // Return the response DTO
        return NotificationScheduleResponseDto.from(deviceId, notificationTimeStrings);
    }

    /**
     * Block notifications for a device
     * 
     * @param deviceId the device ID
     */
    @Transactional
    public void blockNotifications(UUID deviceId) {
        // Verify that the device exists
        User user = userRepository.findById(deviceId)
                .orElseThrow(() -> new IllegalArgumentException("Device with ID " + deviceId + " not found"));

        // Delete all notification schedules for this device
        notificationScheduleRepository.deleteByDeviceId(deviceId);
        log.info("Deleted all notification schedules for device ID {}", deviceId);

        // Unsubscribe user from daily_news topic (id 2)
        try {
            // Get the daily_news topic (id 2)
            Topic dailyNewsTopic = topicRepository.findById(2L)
                    .orElseThrow(() -> new IllegalArgumentException("Daily news topic with ID 2 not found"));

            // Unsubscribe user from the topic
            fcmService.unsubscribeUserFromTopic(deviceId, dailyNewsTopic.getTopicName());
            log.info("User with device ID {} unsubscribed from topic {}", deviceId, dailyNewsTopic.getTopicName());
        } catch (Exception e) {
            log.error("Failed to unsubscribe user with device ID {} from daily_news topic: {}", deviceId, e.getMessage(), e);
            // Continue with the process even if unsubscription fails
        }
    }
}

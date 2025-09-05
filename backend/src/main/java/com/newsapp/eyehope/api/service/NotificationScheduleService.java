package com.newsapp.eyehope.api.service;

import com.newsapp.eyehope.api.domain.NotificationSchedule;
import com.newsapp.eyehope.api.dto.NotificationScheduleRequestDto;
import com.newsapp.eyehope.api.dto.NotificationScheduleResponseDto;
import com.newsapp.eyehope.api.repository.NotificationScheduleRepository;
import com.newsapp.eyehope.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationScheduleService {

    private final NotificationScheduleRepository notificationScheduleRepository;
    private final UserRepository userRepository;

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
        userRepository.findById(deviceId)
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
}

package com.newsapp.eyehope.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationScheduleResponseDto {

    private UUID deviceId;
    private List<String> notificationTime;

    // Static method to create a response DTO from a list of notification times
    public static NotificationScheduleResponseDto from(UUID deviceId, List<String> notificationTime) {
        return NotificationScheduleResponseDto.builder()
                .deviceId(deviceId)
                .notificationTime(notificationTime)
                .build();
    }
}

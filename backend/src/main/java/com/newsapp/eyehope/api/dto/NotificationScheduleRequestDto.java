package com.newsapp.eyehope.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationScheduleRequestDto {
    
    @NotNull(message = "Device ID cannot be null")
    private UUID deviceId;
    
    @NotEmpty(message = "Notification times cannot be empty")
    private List<LocalTime> notificationTimes;
}
package com.newsapp.eyehope.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.newsapp.eyehope.api.dto.NotificationScheduleRequestDto;
import com.newsapp.eyehope.api.dto.NotificationScheduleResponseDto;
import com.newsapp.eyehope.api.dto.UserRequestDto;
import com.newsapp.eyehope.api.dto.UserResponseDto;
import com.newsapp.eyehope.api.service.NotificationScheduleService;
import com.newsapp.eyehope.api.service.UserService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@Import(UserControllerTest.TestConfig.class)
public class UserControllerTest {

    @Configuration
    static class TestConfig {
        @Bean
        public UserService userService() {
            return Mockito.mock(UserService.class);
        }

        @Bean
        public NotificationScheduleService notificationScheduleService() {
            return Mockito.mock(NotificationScheduleService.class);
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserService userService;

    @Autowired
    private NotificationScheduleService notificationScheduleService;

    @Test
    public void testRegisterUserWithAllFields() throws Exception {
        // Given
        UUID deviceId = UUID.randomUUID();
        UserRequestDto requestDto = UserRequestDto.builder()
                .deviceId(deviceId)
                .name("홍길동")
                .email("hong@example.com")
                .nickname("길동이")
                .password("password123")
                .build();

        UserResponseDto responseDto = UserResponseDto.builder()
                .deviceId(deviceId)
                .name("홍길동")
                .email("hong@example.com")
                .nickname("길동이")
                .build();

        when(userService.registerUser(any(UserRequestDto.class))).thenReturn(responseDto);

        // When & Then
        mockMvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("사용자 등록이 완료되었습니다."))
                .andExpect(jsonPath("$.data.deviceId").value(deviceId.toString()))
                .andExpect(jsonPath("$.data.name").value("홍길동"))
                .andExpect(jsonPath("$.data.email").value("hong@example.com"))
                .andExpect(jsonPath("$.data.nickname").value("길동이"));
    }

    @Test
    public void testRegisterUserWithOnlyRequiredFields() throws Exception {
        // Given
        UUID deviceId = UUID.randomUUID();
        UserRequestDto requestDto = UserRequestDto.builder()
                .deviceId(deviceId)
                .nickname("길동이")
                .build();

        UserResponseDto responseDto = UserResponseDto.builder()
                .deviceId(deviceId)
                .nickname("길동이")
                .build();

        when(userService.registerUser(any(UserRequestDto.class))).thenReturn(responseDto);

        // When & Then
        mockMvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("사용자 등록이 완료되었습니다."))
                .andExpect(jsonPath("$.data.deviceId").value(deviceId.toString()))
                .andExpect(jsonPath("$.data.nickname").value("길동이"))
                .andExpect(jsonPath("$.data.name").doesNotExist())
                .andExpect(jsonPath("$.data.email").doesNotExist());
    }

    @Test
    public void testSaveNotificationSchedules() throws Exception {
        // Arrange
        UUID deviceId = UUID.randomUUID();
        List<LocalTime> notificationTimes = Arrays.asList(
                LocalTime.of(8, 0),
                LocalTime.of(12, 0),
                LocalTime.of(18, 0)
        );

        NotificationScheduleRequestDto requestDto = NotificationScheduleRequestDto.builder()
                .deviceId(deviceId)
                .notificationTimes(notificationTimes)
                .build();

        NotificationScheduleResponseDto responseDto = NotificationScheduleResponseDto.builder()
                .deviceId(deviceId)
                .notificationTimes(notificationTimes)
                .build();

        when(notificationScheduleService.saveNotificationSchedules(any(NotificationScheduleRequestDto.class)))
                .thenReturn(responseDto);

        // Act & Assert
        mockMvc.perform(post("/api/users/schedules")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Notification schedules saved successfully"))
                .andExpect(jsonPath("$.data.deviceId").value(deviceId.toString()))
                .andExpect(jsonPath("$.data.notificationTimes.length()").value(3));
    }

    @Test
    public void testGetNotificationSchedules() throws Exception {
        // Arrange
        UUID deviceId = UUID.randomUUID();
        List<LocalTime> notificationTimes = Arrays.asList(
                LocalTime.of(8, 0),
                LocalTime.of(12, 0),
                LocalTime.of(18, 0)
        );

        NotificationScheduleResponseDto responseDto = NotificationScheduleResponseDto.builder()
                .deviceId(deviceId)
                .notificationTimes(notificationTimes)
                .build();

        when(notificationScheduleService.getNotificationSchedules(deviceId))
                .thenReturn(responseDto);

        // Act & Assert
        mockMvc.perform(get("/api/users/schedules/" + deviceId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Notification schedules retrieved successfully"))
                .andExpect(jsonPath("$.data.deviceId").value(deviceId.toString()))
                .andExpect(jsonPath("$.data.notificationTimes.length()").value(3));
    }
}

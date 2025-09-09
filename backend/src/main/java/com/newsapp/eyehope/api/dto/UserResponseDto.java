package com.newsapp.eyehope.api.dto;

import com.newsapp.eyehope.api.domain.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponseDto {
    private UUID deviceId;
    private String name;
    private String email;
    private String nickname;
    private String fcmToken;
    private boolean isAdmin;

    public static UserResponseDto fromEntity(User user) {
        return UserResponseDto.builder()
                .deviceId(user.getDeviceId())
                .name(user.getName())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .fcmToken(user.getFcmToken())
                .isAdmin(user.isAdmin())
                .build();
    }
}

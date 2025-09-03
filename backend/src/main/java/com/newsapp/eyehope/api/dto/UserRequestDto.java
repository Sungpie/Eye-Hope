package com.newsapp.eyehope.api.dto;

import com.newsapp.eyehope.api.domain.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRequestDto {
    private UUID deviceId;
    private String name;
    private String email;
    private String nickname;
    private String password;
    
    public User toEntity(String passwordHash) {
        return User.builder()
                .deviceId(deviceId)
                .name(name)
                .email(email)
                .nickname(nickname)
                .passwordHash(passwordHash)
                .build();
    }
}
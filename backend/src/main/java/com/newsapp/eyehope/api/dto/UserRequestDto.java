package com.newsapp.eyehope.api.dto;

import com.newsapp.eyehope.api.domain.User;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
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
    @NotNull(message = "디바이스 ID는 필수입니다")
    @Schema(description = "사용자 디바이스 ID", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    private UUID deviceId;

    @Size(max = 100, message = "이름은 100자를 초과할 수 없습니다")
    @Pattern(regexp = "^[\\p{L}\\s\\d\\p{Punct}]*$", message = "이름에 유효하지 않은 문자가 포함되어 있습니다")
    @Schema(description = "사용자 이름", example = "홍길동")
    private String name;

    @Email(message = "유효한 이메일 형식이 아닙니다")
    @Size(max = 100, message = "이메일은 100자를 초과할 수 없습니다")
    @Schema(description = "사용자 이메일", example = "user@example.com")
    private String email;

    @NotBlank(message = "닉네임은 필수입니다")
    @Size(min = 2, max = 50, message = "닉네임은 2자 이상 50자 이하여야 합니다")
    @Pattern(regexp = "^[\\p{L}\\s\\d\\p{Punct}]*$", message = "닉네임에 유효하지 않은 문자가 포함되어 있습니다")
    @Schema(description = "사용자 닉네임", example = "뉴스러버")
    private String nickname;

    @Size(min = 8, max = 100, message = "비밀번호는 8자 이상 100자 이하여야 합니다")
    @Schema(description = "사용자 비밀번호 (8자 이상)", example = "password123")
    private String password;

    @Size(max = 255, message = "FCM 토큰은 255자를 초과할 수 없습니다")
    @Schema(description = "FCM 토큰", example = "fcm-token-example")
    private String fcmToken;

    @Schema(description = "관리자 여부", example = "false")
    private Boolean isAdmin;

    public User toEntity(String passwordHash) {
        return User.builder()
                .deviceId(deviceId)
                .name(name)
                .email(email)
                .nickname(nickname)
                .passwordHash(passwordHash)
                .fcmToken(fcmToken)
                .isAdmin(isAdmin != null && isAdmin)
                .build();
    }
}

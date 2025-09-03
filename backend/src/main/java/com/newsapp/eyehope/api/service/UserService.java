package com.newsapp.eyehope.api.service;

import com.newsapp.eyehope.api.domain.User;
import com.newsapp.eyehope.api.dto.UserRequestDto;
import com.newsapp.eyehope.api.dto.UserResponseDto;
import com.newsapp.eyehope.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional
    public UserResponseDto registerUser(UserRequestDto requestDto) {
        // 이메일 중복 확인 (이메일이 있는 경우에만)
        if (requestDto.getEmail() != null && userRepository.existsByEmail(requestDto.getEmail())) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }

        // 디바이스 ID 중복 확인
        if (userRepository.existsByDeviceId(requestDto.getDeviceId())) {
            throw new IllegalArgumentException("이미 등록된 디바이스입니다.");
        }

        // 비밀번호 해싱 (비밀번호가 있는 경우에만)
        String hashedPassword = null;
        if (requestDto.getPassword() != null) {
            hashedPassword = hashPassword(requestDto.getPassword());
        }

        // 사용자 엔티티 생성 및 저장
        User user = requestDto.toEntity(hashedPassword);
        User savedUser = userRepository.save(user);

        log.info("사용자 등록 완료: {}", savedUser.getEmail());

        return UserResponseDto.fromEntity(savedUser);
    }

    /**
     * 비밀번호를 SHA-256으로 해싱
     */
    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            log.error("비밀번호 해싱 오류", e);
            throw new RuntimeException("비밀번호 해싱 중 오류가 발생했습니다.", e);
        }
    }
}

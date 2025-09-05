package com.newsapp.eyehope.api.service;

import com.newsapp.eyehope.api.domain.User;
import com.newsapp.eyehope.api.dto.PasswordChangeDto;
import com.newsapp.eyehope.api.dto.UserRequestDto;
import com.newsapp.eyehope.api.dto.UserResponseDto;
import com.newsapp.eyehope.api.dto.UserUpdateDto;
import com.newsapp.eyehope.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional
    public UserResponseDto registerUser(UserRequestDto requestDto) {
        // Validate nickname (required, not empty)
        if (requestDto.getNickname() == null || !StringUtils.hasText(requestDto.getNickname())) {
            throw new IllegalArgumentException("닉네임은 필수이며 비어 있거나 공백일 수 없습니다.");
        }

        // Validate name (can be null, but not empty)
        if (requestDto.getName() != null && !StringUtils.hasText(requestDto.getName())) {
            throw new IllegalArgumentException("이름이 비어 있거나 공백일 수 없습니다.");
        }

        // Validate email (can be null, but not empty)
        if (requestDto.getEmail() != null && !StringUtils.hasText(requestDto.getEmail())) {
            throw new IllegalArgumentException("이메일이 비어 있거나 공백일 수 없습니다.");
        }

        // Validate password (can be null, but not empty)
        if (requestDto.getPassword() != null && !StringUtils.hasText(requestDto.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 비어 있거나 공백일 수 없습니다.");
        }

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

    /**
     * 디바이스 ID로 사용자 정보 조회
     */
    @Transactional(readOnly = true)
    public UserResponseDto getUserByDeviceId(UUID deviceId) {
        User user = userRepository.findByDeviceId(deviceId)
                .orElseThrow(() -> new NoSuchElementException("해당 디바이스 ID로 등록된 사용자가 없습니다: " + deviceId));

        log.info("사용자 정보 조회 완료: {}", user.getEmail());

        return UserResponseDto.fromEntity(user);
    }

    /**
     * 사용자 정보 업데이트
     */
    @Transactional
    public UserResponseDto updateUser(UUID deviceId, UserUpdateDto updateDto) {
        User user = userRepository.findByDeviceId(deviceId)
                .orElseThrow(() -> new NoSuchElementException("해당 디바이스 ID로 등록된 사용자가 없습니다: " + deviceId));

        // Validate nickname (can be null in DTO, but if provided, not empty)
        if (updateDto.getNickname() != null && !StringUtils.hasText(updateDto.getNickname())) {
            throw new IllegalArgumentException("닉네임이 비어 있거나 공백일 수 없습니다.");
        }

        // Validate name (can be null, but not empty)
        if (updateDto.getName() != null && !StringUtils.hasText(updateDto.getName())) {
            throw new IllegalArgumentException("이름이 비어 있거나 공백일 수 없습니다.");
        }

        // Validate email (can be null, but not empty)
        if (updateDto.getEmail() != null && !StringUtils.hasText(updateDto.getEmail())) {
            throw new IllegalArgumentException("이메일이 비어 있거나 공백일 수 없습니다.");
        }

        // 이메일 중복 확인 (이메일이 변경되는 경우에만)
        if (updateDto.getEmail() != null && !updateDto.getEmail().equals(user.getEmail()) 
                && userRepository.existsByEmail(updateDto.getEmail())) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }

        // 변경할 필드만 업데이트
        if (updateDto.getName() != null) {
            user.setName(updateDto.getName());
        }

        if (updateDto.getEmail() != null) {
            user.setEmail(updateDto.getEmail());
        }

        if (updateDto.getNickname() != null) {
            user.setNickname(updateDto.getNickname());
        } else if (user.getNickname() == null || !StringUtils.hasText(user.getNickname())) {
            // Ensure nickname is never null or empty, even if not provided in update
            throw new IllegalArgumentException("닉네임은 필수이며 비어 있거나 공백일 수 없습니다.");
        }

        User updatedUser = userRepository.save(user);
        log.info("사용자 정보 업데이트 완료: {}", updatedUser.getEmail());

        return UserResponseDto.fromEntity(updatedUser);
    }

    /**
     * 비밀번호 변경
     */
    @Transactional
    public UserResponseDto changePassword(PasswordChangeDto passwordChangeDto) {
        // Validate current password
        if (passwordChangeDto.getCurrentPassword() == null || !StringUtils.hasText(passwordChangeDto.getCurrentPassword())) {
            throw new IllegalArgumentException("현재 비밀번호가 비어 있거나 공백일 수 없습니다.");
        }

        // Validate new password
        if (passwordChangeDto.getNewPassword() == null || !StringUtils.hasText(passwordChangeDto.getNewPassword())) {
            throw new IllegalArgumentException("새 비밀번호가 비어 있거나 공백일 수 없습니다.");
        }

        User user = userRepository.findByDeviceId(passwordChangeDto.getDeviceId())
                .orElseThrow(() -> new NoSuchElementException("해당 디바이스 ID로 등록된 사용자가 없습니다: " + passwordChangeDto.getDeviceId()));

        // 현재 비밀번호 확인
        String currentPasswordHash = hashPassword(passwordChangeDto.getCurrentPassword());
        if (!Objects.equals(currentPasswordHash, user.getPasswordHash())) {
            throw new IllegalArgumentException("현재 비밀번호가 일치하지 않습니다.");
        }

        // 새 비밀번호 해싱 및 저장
        String newPasswordHash = hashPassword(passwordChangeDto.getNewPassword());
        user.setPasswordHash(newPasswordHash);

        User updatedUser = userRepository.save(user);
        log.info("비밀번호 변경 완료: {}", updatedUser.getEmail());

        return UserResponseDto.fromEntity(updatedUser);
    }
}

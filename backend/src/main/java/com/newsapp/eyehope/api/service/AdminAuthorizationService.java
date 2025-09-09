package com.newsapp.eyehope.api.service;

import com.newsapp.eyehope.api.domain.User;
import com.newsapp.eyehope.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;
import java.util.UUID;

/**
 * Service for handling admin authorization in a no-login environment
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminAuthorizationService {

    private final UserRepository userRepository;

    /**
     * Checks if a user with the given device ID has admin privileges
     * @param deviceId The device ID to check
     * @return true if the user is an admin, false otherwise
     */
    public boolean isAdmin(UUID deviceId) {
        if (deviceId == null) {
            return false;
        }
        
        return userRepository.findByDeviceId(deviceId)
                .map(User::isAdmin)
                .orElse(false);
    }

    /**
     * Verifies that a user with the given device ID has admin privileges
     * @param deviceId The device ID to check
     * @throws AccessDeniedException if the user is not an admin
     * @throws NoSuchElementException if the user does not exist
     */
    public void verifyAdminAccess(UUID deviceId) {
        if (deviceId == null) {
            throw new AccessDeniedException("디바이스 ID가 제공되지 않았습니다.");
        }
        
        User user = userRepository.findByDeviceId(deviceId)
                .orElseThrow(() -> new NoSuchElementException("해당 디바이스 ID로 등록된 사용자가 없습니다: " + deviceId));
        
        if (!user.isAdmin()) {
            log.warn("관리자 권한이 없는 사용자({})가 관리자 기능에 접근을 시도했습니다.", deviceId);
            throw new AccessDeniedException("관리자 권한이 필요합니다.");
        }
        
        log.info("관리자({})가 관리자 기능에 접근했습니다.", deviceId);
    }
}
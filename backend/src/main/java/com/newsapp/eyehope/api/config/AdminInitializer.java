package com.newsapp.eyehope.api.config;

import com.newsapp.eyehope.api.domain.User;
import com.newsapp.eyehope.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.List;
import java.util.UUID;

/**
 * Initializes the first admin user if no admin users exist in the system
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class AdminInitializer {

    private final UserRepository userRepository;

    /**
     * Creates the first admin user if no admin users exist
     * This is run on application startup
     */
    @Bean
    @Profile("!test") // Don't run in test profile
    public CommandLineRunner initializeAdmin() {
        return args -> {
            // Check if any admin users exist
            List<User> adminUsers = userRepository.findByIsAdmin(true);
            
            if (adminUsers.isEmpty()) {
                // Define a fixed UUID for the first admin user
                // This UUID should be documented and shared with the system administrator
                UUID firstAdminDeviceId = UUID.fromString("00000000-0000-0000-0000-000000000001");
                
                // Check if a user with this device ID already exists
                User existingUser = userRepository.findByDeviceId(firstAdminDeviceId).orElse(null);
                
                if (existingUser != null) {
                    // User exists, make them an admin
                    existingUser.setAdmin(true);
                    userRepository.save(existingUser);
                    log.info("기존 사용자({})에게 관리자 권한이 부여되었습니다.", firstAdminDeviceId);
                } else {
                    // Create a new admin user
                    User adminUser = User.builder()
                            .deviceId(firstAdminDeviceId)
                            .nickname("시스템 관리자")
                            .isAdmin(true)
                            .build();
                    
                    userRepository.save(adminUser);
                    log.info("첫 번째 관리자 사용자가 생성되었습니다. 디바이스 ID: {}", firstAdminDeviceId);
                }
                
                log.info("관리자 계정 정보: 디바이스 ID = {}", firstAdminDeviceId);
            } else {
                log.info("이미 {} 명의 관리자가 존재합니다.", adminUsers.size());
            }
        };
    }
}
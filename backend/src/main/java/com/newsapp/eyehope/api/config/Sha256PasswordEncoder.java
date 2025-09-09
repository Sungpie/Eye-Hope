package com.newsapp.eyehope.api.config;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class Sha256PasswordEncoder implements PasswordEncoder {
    // Using BCrypt with a strength of 12 (recommended for most applications)
    private final BCryptPasswordEncoder bCryptEncoder = new BCryptPasswordEncoder(12);

    @Override
    public String encode(CharSequence rawPassword) {
        if (rawPassword == null) {
            throw new IllegalArgumentException("Password cannot be null");
        }
        return bCryptEncoder.encode(rawPassword);
    }

    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        if (rawPassword == null || encodedPassword == null) {
            return false;
        }

        // Check if it's an old SHA-256 hash (no $ prefix)
        if (!encodedPassword.startsWith("$")) {
            // For backward compatibility with old SHA-256 hashes
            try {
                java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
                byte[] hash = digest.digest(rawPassword.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8));
                String oldHash = java.util.Base64.getEncoder().encodeToString(hash);
                return oldHash.equals(encodedPassword);
            } catch (java.security.NoSuchAlgorithmException e) {
                throw new RuntimeException("비밀번호 해싱 중 오류가 발생했습니다.", e);
            }
        }

        // For new BCrypt hashes
        return bCryptEncoder.matches(rawPassword, encodedPassword);
    }
}

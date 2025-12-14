package com.kindora.kindora_backend.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;
import java.time.LocalDateTime;

@Repository
public class JdbcOtpRepository {

    private final JdbcTemplate jdbc;

    public JdbcOtpRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public void insertOtp(String email, String otpHash, LocalDateTime expiresAt) {
        String sql = "INSERT INTO otp_codes (email, otp_hash, expires_at, verified, created_at, attempts) VALUES (?, ?, ?, 0, NOW(), 0)";
        jdbc.update(sql, email, otpHash, java.sql.Timestamp.valueOf(expiresAt));
    }

    public Optional<Map<String, Object>> findLatestByEmail(String email) {
        String sql = "SELECT id, email, otp_hash, expires_at, attempts, created_at, verified FROM otp_codes WHERE email = ? ORDER BY created_at DESC LIMIT 1";
        try {
            return Optional.of(jdbc.queryForMap(sql, email));
        } catch (Exception ex) {
            return Optional.empty();
        }
    }

    public void deleteByEmail(String email) {
        String sql = "DELETE FROM otp_codes WHERE email = ?";
        jdbc.update(sql, email);
    }

    public void deleteById(Long id) {
        String sql = "DELETE FROM otp_codes WHERE id = ?";
        jdbc.update(sql, id);
    }

    public void incrementAttempts(Long id) {
        String sql = "UPDATE otp_codes SET attempts = attempts + 1 WHERE id = ?";
        jdbc.update(sql, id);
    }

    public void markVerified(Long id) {
        String sql = "UPDATE otp_codes SET verified = 1 WHERE id = ?";
        jdbc.update(sql, id);
    }
}

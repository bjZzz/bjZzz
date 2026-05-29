package com.nanda;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 生成 admin123 的 BCrypt 哈希，用于校验种子数据。
 */
class BcryptHashTest {

    @Test
    void generateAdminPasswordHash() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String hash = encoder.encode("admin123");
        assertTrue(encoder.matches("admin123", hash));
        System.out.println("BCrypt(admin123)=" + hash);
    }
}

package com.example.lms.security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {
    private static final String SECRET = "test-secret-key-that-is-at-least-32-bytes-long";

    @Test
    void generateAndEmailRoundTrip() {
        JwtService service = new JwtService(SECRET, 60_000);

        String token = service.generate("student@example.com", "USER");

        assertEquals("student@example.com", service.email(token));
        assertTrue(service.valid(token));
    }

    @Test
    void validRejectsTokenSignedByAnotherKey() {
        JwtService service = new JwtService(SECRET, 60_000);
        JwtService otherService = new JwtService("another-test-secret-key-that-is-32-bytes", 60_000);

        String token = otherService.generate("student@example.com", "USER");

        assertFalse(service.valid(token));
    }

    @Test
    void validRejectsExpiredToken() throws InterruptedException {
        JwtService service = new JwtService(SECRET, 1);
        String token = service.generate("student@example.com", "USER");

        Thread.sleep(10);

        assertFalse(service.valid(token));
    }

    @Test
    void emailRejectsMalformedToken() {
        JwtService service = new JwtService(SECRET, 60_000);

        assertThrows(Exception.class, () -> service.email("not-a-jwt"));
    }
}

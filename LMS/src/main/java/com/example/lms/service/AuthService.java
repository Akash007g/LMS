package com.example.lms.service;

import com.example.lms.dto.*;
import com.example.lms.entity.*;
import com.example.lms.repository.UserRepository;
import com.example.lms.security.JwtService;
import org.springframework.security.authentication.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;

@Service
public class AuthService {
    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final AuthenticationManager authManager;
    private final JwtService jwt;
    private final EmailService email;

    public AuthService(UserRepository users, PasswordEncoder encoder,
                       AuthenticationManager authManager, JwtService jwt,
                       EmailService email) {
        this.users = users;
        this.encoder = encoder;
        this.authManager = authManager;
        this.jwt = jwt;
        this.email = email;
    }

    public void register(RegisterRequest r) {
        if (users.existsByEmailIgnoreCase(r.email()))
            throw new IllegalArgumentException("Email is already registered.");

        String otp = String.format("%06d", new Random().nextInt(1_000_000));

        User u = User.builder()
                .fullName(r.fullName())
                .email(r.email().toLowerCase())
                .password(encoder.encode(r.password()))
                .phone(r.phone())
                .role(Role.USER)
                .enabled(false)
                .otp(otp)
                .otpExpiresAt(LocalDateTime.now().plusMinutes(10))
                .build();

        final User savedUser = users.save(u);
        email.otp(savedUser.getEmail(), otp);
    }

    public void verify(VerifyOtpRequest r) {
        User u = users.findByEmailIgnoreCase(r.email())
                .orElseThrow(() -> new IllegalArgumentException("User not found."));

        if (u.isEnabled()) return;

        if (u.getOtp() == null || u.getOtpExpiresAt() == null
                || u.getOtpExpiresAt().isBefore(LocalDateTime.now())
                || !u.getOtp().equals(r.otp())) {
            throw new IllegalArgumentException("Invalid or expired OTP.");
        }

        u.setEnabled(true);
        u.setOtp(null);
        u.setOtpExpiresAt(null);
        users.save(u);
    }

    public String login(LoginRequest r) {
        authManager.authenticate(
                new UsernamePasswordAuthenticationToken(r.email(), r.password()));

        User u = users.findByEmailIgnoreCase(r.email())
                .orElseThrow(() -> new IllegalArgumentException("User not found."));

        email.loginNotification(u.getEmail(), u.getFullName());
        return jwt.generate(u.getEmail(), u.getRole().name());
    }
}

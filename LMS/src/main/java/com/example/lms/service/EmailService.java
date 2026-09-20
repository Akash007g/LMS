package com.example.lms.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    private final JavaMailSender sender;

    @Value("${spring.mail.username:}")
    private String from;

    @Value("${app.mail.enabled:false}")
    private boolean enabled;

    public EmailService(JavaMailSender sender) {
        this.sender = sender;
    }

    public void otp(String to, String otp) {
        if (!enabled || from == null || from.isBlank()) {
            System.out.println("========== LMS OTP ==========");
            System.out.println("Email: " + to);
            System.out.println("OTP: " + otp);
            System.out.println("==============================");
            return;
        }

        SimpleMailMessage m = new SimpleMailMessage();
        m.setFrom(from);
        m.setTo(to);
        m.setSubject("LMS OTP Verification");
        m.setText("Your LMS OTP is: " + otp + "\nIt expires in 10 minutes.");
        sender.send(m);
    }

    public void loginNotification(String to, String name) {
        if (!enabled || from == null || from.isBlank()) return;

        SimpleMailMessage m = new SimpleMailMessage();
        m.setFrom(from);
        m.setTo(to);
        m.setSubject("LMS Login Notification");
        m.setText("Hello " + name + ",\n\nYou have successfully logged in to LMS.");
        sender.send(m);
    }
}

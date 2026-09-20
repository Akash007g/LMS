package com.example.lms.controller;

import com.example.lms.dto.*;
import com.example.lms.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class AuthController {
    private final AuthService auth;

    public AuthController(AuthService auth) {
        this.auth = auth;
    }

    @GetMapping("/register")
    String registerPage(Model model) {
        model.addAttribute("registerRequest", new RegisterRequest("", "", "", ""));
        return "register";
    }

    @PostMapping("/register")
    String register(@Valid @ModelAttribute RegisterRequest request, Model model) {
        try {
            auth.register(request);
            model.addAttribute("email", request.email());
            return "verify-otp";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "register";
        }
    }

    @PostMapping("/verify-otp")
    String verify(@Valid @ModelAttribute VerifyOtpRequest request, Model model) {
        try {
            auth.verify(request);
            return "redirect:/login?verified";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("email", request.email());
            return "verify-otp";
        }
    }

    @GetMapping("/verify-otp")
    String verifyPage(@RequestParam(required = false) String email, Model model) {
        model.addAttribute("email", email == null ? "" : email);
        return "verify-otp";
    }

    @GetMapping("/login")
    String loginPage(@RequestParam(required = false) String error,
                     @RequestParam(required = false) String verified,
                     Model model) {
        if (error != null) model.addAttribute("error", "Invalid email or password.");
        if (verified != null) model.addAttribute("message", "OTP verified. Please login.");
        return "login";
    }

    @PostMapping("/login")
    String login(@Valid @ModelAttribute LoginRequest request,
                 jakarta.servlet.http.HttpServletResponse response,
                 Model model) {
        try {
            String token = auth.login(request);

            // Thymeleaf pages use a simple HttpOnly cookie for JWT.
            var cookie = new jakarta.servlet.http.Cookie("LMS_TOKEN", token);
            cookie.setHttpOnly(true);
            cookie.setPath("/");
            cookie.setMaxAge(24 * 60 * 60);
            response.addCookie(cookie);

            return "redirect:/dashboard";
        } catch (Exception e) {
            model.addAttribute("error", "Invalid email/password or account not verified.");
            return "login";
        }
    }

    @GetMapping("/logout")
    String logout(jakarta.servlet.http.HttpServletRequest request,
                  jakarta.servlet.http.HttpServletResponse response) {
        var cookie = new jakarta.servlet.http.Cookie("LMS_TOKEN", "");
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
        return "redirect:/login";
    }
}

package com.example.lms.dto;

import jakarta.validation.constraints.*;

public record VerifyOtpRequest(
        @NotBlank @Email String email,
        @NotBlank String otp) {}

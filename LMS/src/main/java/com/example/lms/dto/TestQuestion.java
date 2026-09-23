package com.example.lms.dto;

import java.util.List;

public record TestQuestion(String id, String prompt, List<String> options) {
}
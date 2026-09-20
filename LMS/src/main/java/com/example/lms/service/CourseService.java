package com.example.lms.service;

import com.example.lms.entity.*;
import com.example.lms.repository.*;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CourseService {
    private final CourseRepository courses;
    private final UserRepository users;
    private final EnrollmentRepository enrollments;

    public CourseService(CourseRepository courses, UserRepository users,
                         EnrollmentRepository enrollments) {
        this.courses = courses;
        this.users = users;
        this.enrollments = enrollments;
    }

    public List<Course> all() { return courses.findAll(); }

    public Course get(Long id) {
        return courses.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Course not found."));
    }

    public void enroll(String email, Long courseId) {
        User u = users.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));
        Course c = get(courseId);

        if (isEnrolled(email, courseId))
            throw new IllegalArgumentException("Already enrolled in this course.");

        enrollments.save(Enrollment.builder()
                .user(u).course(c).enrolledAt(LocalDateTime.now()).build());
    }

    public boolean isEnrolled(String email, Long courseId) {
        User u = users.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));
        return enrollments.findByUserIdAndCourseId(u.getId(), courseId).isPresent();
    }

    public List<Course> myCourses(String email) {
        User u = users.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));

        return enrollments.findByUserId(u.getId()).stream()
                .map(Enrollment::getCourse).toList();
    }

    public User profile(String email) {
        return users.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));
    }
}

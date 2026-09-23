package com.example.lms.service;

import com.example.lms.entity.*;
import com.example.lms.repository.*;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

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

    public List<Course> search(String query) {
        if (query == null || query.isBlank()) return all();

        String term = query.trim().toLowerCase(Locale.ROOT);
        return all().stream()
                .filter(course -> contains(course.getTitle(), term)
                        || contains(course.getDescription(), term)
                        || contains(course.getInstructor(), term)
                        || contains(course.getLevel(), term))
                .toList();
    }

    private boolean contains(String value, String term) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(term);
    }

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

    public Enrollment enrollment(String email, Long courseId) {
        User user = users.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));
        return enrollments.findByUserIdAndCourseId(user.getId(), courseId)
                .orElseThrow(() -> new IllegalArgumentException("Enroll in this course first."));
    }

    public boolean completeTest(String email, Long courseId, int score) {
        Enrollment enrollment = enrollment(email, courseId);
        enrollment.setTestScore(score);
        if (score < 2) {
            enrollments.save(enrollment);
            return false;
        }

        enrollment.setCompletedAt(LocalDateTime.now());
        enrollments.save(enrollment);
        return true;
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

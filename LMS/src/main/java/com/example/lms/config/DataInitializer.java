package com.example.lms.config;

import com.example.lms.entity.Course;
import com.example.lms.repository.CourseRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.*;

import java.util.Objects;

@Configuration
public class DataInitializer {
    @Bean
    CommandLineRunner seed(CourseRepository repo) {
        return args -> {
            if (repo.count() == 0) {
                repo.save(Objects.requireNonNull(Course.builder()
                        .title("Java & Spring Boot")
                        .description("Learn Java, Spring Boot, REST APIs, JPA and MySQL.")
                        .instructor("LMS Academy").level("Intermediate")
                        .thumbnailUrl("https://img.youtube.com/vi/9SGDpanrc8U/maxresdefault.jpg")
                    .demoYoutubeUrl("https://www.youtube.com/watch?v=9SGDpanrc8U").build()));

                repo.save(Objects.requireNonNull(Course.builder()
                        .title("Spring Boot for Beginners")
                        .description("Understand controllers, services, repositories, dependency injection and MVC.")
                        .instructor("LMS Academy").level("Beginner")
                        .thumbnailUrl("https://img.youtube.com/vi/9SGDpanrc8U/maxresdefault.jpg")
                    .demoYoutubeUrl("https://www.youtube.com/watch?v=9SGDpanrc8U").build()));

                repo.save(Objects.requireNonNull(Course.builder()
                        .title("MySQL Database Essentials")
                        .description("SQL, joins, indexes, normalization and practical database design.")
                        .instructor("LMS Academy").level("Beginner")
                        .thumbnailUrl("https://img.youtube.com/vi/7S_tz1z_5bA/maxresdefault.jpg")
                    .demoYoutubeUrl("https://www.youtube.com/watch?v=7S_tz1z_5bA").build()));
            }
        };
    }
}

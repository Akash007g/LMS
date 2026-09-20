CREATE DATABASE IF NOT EXISTS lms_db;
USE lms_db;

-- Spring Boot JPA creates/updates these tables automatically.
-- This file documents the core database structure.

CREATE TABLE IF NOT EXISTS users (
 id BIGINT PRIMARY KEY AUTO_INCREMENT,
 email VARCHAR(255) NOT NULL UNIQUE,
 password VARCHAR(255) NOT NULL,
 full_name VARCHAR(255) NOT NULL,
 phone VARCHAR(50),
 role VARCHAR(20) NOT NULL,
 enabled BOOLEAN NOT NULL DEFAULT FALSE,
 otp VARCHAR(10),
 otp_expires_at DATETIME,
 created_at DATETIME
);

CREATE TABLE IF NOT EXISTS courses (
 id BIGINT PRIMARY KEY AUTO_INCREMENT,
 title VARCHAR(255) NOT NULL,
 description VARCHAR(2000),
 instructor VARCHAR(255),
 level VARCHAR(100),
 thumbnail_url VARCHAR(1000),
 demo_youtube_url VARCHAR(1000)
);

CREATE TABLE IF NOT EXISTS enrollments (
 id BIGINT PRIMARY KEY AUTO_INCREMENT,
 user_id BIGINT NOT NULL,
 course_id BIGINT NOT NULL,
 enrolled_at DATETIME,
 UNIQUE KEY uk_user_course(user_id, course_id),
 FOREIGN KEY(user_id) REFERENCES users(id),
 FOREIGN KEY(course_id) REFERENCES courses(id)
);

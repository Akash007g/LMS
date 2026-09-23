# LMS Website - Spring Boot + Thymeleaf + MySQL

This version DOES NOT use React, ReactJS, Angular, Vue, Node.js frontend, or a separate frontend application.

The browser UI is rendered by Thymeleaf inside the Spring Boot application.

## Flow

Registration
  -> OTP
  -> Verify OTP
  -> Login
  -> Login email notification
  -> User Dashboard
  -> Courses
  -> Course Details
  -> Watch Demo on YouTube
  -> Enroll
  -> My Courses
  -> Profile

## Technology

- Java 25
- Spring Boot 3.5.x
- Spring MVC
- Thymeleaf
- Spring Security
- JWT stored in an HttpOnly cookie
- Spring Data JPA / Hibernate
- MySQL
- Maven
- JavaMailSender

## Folder structure

src/main/java/com/example/lms/
  config/
  controller/
  dto/
  entity/
  exception/
  repository/
  security/
  service/

src/main/resources/
  templates/
  static/css/
  application.properties

database/
  lms_schema.sql

## Setup

1. Create MySQL database:

CREATE DATABASE lms_db;

2. Open:
src/main/resources/application.properties

For local development, the file defaults to a local `lms` database with `root` / `root`.
For deployment, set these environment variables instead:

DATABASE_URL=jdbc:mysql://HOST:3306/DATABASE?useSSL=true&serverTimezone=UTC
DB_USERNAME=YOUR_MYSQL_USERNAME
DB_PASSWORD=YOUR_MYSQL_PASSWORD
JWT_SECRET=YOUR_LONG_RANDOM_SECRET
PORT=8080

3. Start:

mvn spring-boot:run

4. Open:

http://localhost:8080

## Vercel deployment

The repository includes `Dockerfile.vercel` for Vercel's container deployment support.

1. Push the repository to GitHub and import it into Vercel.
2. Set `DATABASE_URL`, `DB_USERNAME`, `DB_PASSWORD`, and `JWT_SECRET` in the Vercel project environment variables.
3. Redeploy the project. Vercel supplies `PORT`; locally the application defaults to port 8080.

Use a hosted MySQL database. A database running on `localhost` is only reachable from your development machine.

## OTP

For development, keep:

app.mail.enabled=false

The generated OTP is printed in the Spring Boot console.

For real email, configure Gmail SMTP using an App Password:

MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-app-password
MAIL_ENABLED=true

## Important architecture

Browser
   |
   | HTTP GET/POST
   v
Spring MVC Controller
   |
   v
Service Layer
   |
   v
Repository
   |
   v
MySQL

Thymeleaf:
Controller -> Model -> HTML template -> Browser

There is no React frontend and no REST API requirement for the browser pages.

## Security

Passwords are BCrypt hashed.

After successful login:
Spring Security authenticates the credentials
-> JWT is generated
-> JWT is stored in an HttpOnly cookie
-> protected pages validate the JWT.

## Interview explanation

You can explain the project as:

"I developed an LMS using Spring Boot, Thymeleaf and MySQL. Thymeleaf is used for server-side rendering, so I did not create a separate React frontend. Users can register using their email, receive an OTP, verify the account and login. Spring Security with BCrypt handles authentication and JWT is stored in an HttpOnly cookie. After login, the user reaches the dashboard where they can browse courses, view course details, enroll, view their enrolled courses and access their profile. Each course contains a YouTube demo link. Spring Data JPA handles database operations with MySQL, and the application follows Controller-Service-Repository architecture."

## Suggested next production features

- Admin login
- Admin course CRUD
- Password reset
- Resend OTP
- OTP rate limiting
- Course lessons and progress
- Search/filter courses
- Pagination
- File/video storage
- Unit/integration tests
- Docker
- CI/CD

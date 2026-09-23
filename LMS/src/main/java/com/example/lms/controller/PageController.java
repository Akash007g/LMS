package com.example.lms.controller;

import com.example.lms.service.CourseService;
import com.example.lms.security.JwtService;
import com.example.lms.entity.PaymentGateway;
import com.example.lms.dto.TestQuestion;
import com.example.lms.entity.Enrollment;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.util.HtmlUtils;

import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class PageController {
    private final CourseService courses;
    private final JwtService jwt;

    public PageController(CourseService courses, JwtService jwt) {
        this.courses = courses;
        this.jwt = jwt;
    }

    @GetMapping("/")
    String home() {
        return "index";
    }

    @GetMapping("/courses")
    String courses(@RequestParam(required = false) String query,
                   HttpServletRequest request, Model model) {
        model.addAttribute("courses", courses.search(query));
        model.addAttribute("query", query == null ? "" : query);
        model.addAttribute("loggedIn", currentEmail(request) != null);
        return "courses";
    }

    @GetMapping("/courses/{id}")
    String course(@PathVariable Long id, HttpServletRequest request, Model model) {
        String email = currentEmail(request);
        Enrollment enrollment = email != null && courses.isEnrolled(email, id)
            ? courses.enrollment(email, id) : null;
        model.addAttribute("course", courses.get(id));
        model.addAttribute("loggedIn", email != null);
        model.addAttribute("enrolled", enrollment != null);
        model.addAttribute("completed", enrollment != null && enrollment.getCompletedAt() != null);
        return "course-details";
    }

    @GetMapping("/courses/{id}/demo")
    String demo(@PathVariable Long id, HttpServletRequest request) {
        if (currentEmail(request) == null) return "redirect:/login";
        return "redirect:" + courses.get(id).getDemoYoutubeUrl();
    }

    @GetMapping("/dashboard")
    String dashboard(HttpServletRequest request, Model model) {
        String email = currentEmail(request);
        if (email == null) return "redirect:/login";

        model.addAttribute("user", courses.profile(email));
        model.addAttribute("myCourses", courses.myCourses(email));
        return "dashboard";
    }

    @PostMapping("/courses/{id}/enroll")
    String enroll(@PathVariable Long id, HttpServletRequest request, Model model) {
        String email = currentEmail(request);
        if (email == null) return "redirect:/login";

        try {
            return "redirect:/courses/" + id + "/payment";
        } catch (Exception e) {
            model.addAttribute("course", courses.get(id));
            model.addAttribute("loggedIn", true);
            model.addAttribute("enrolled", courses.isEnrolled(email, id));
            model.addAttribute("error", e.getMessage());
            return "course-details";
        }
    }

    @GetMapping("/courses/{id}/payment")
    String payment(@PathVariable Long id, HttpServletRequest request, Model model) {
        if (currentEmail(request) == null) return "redirect:/login";

        model.addAttribute("course", courses.get(id));
        model.addAttribute("gateways", PaymentGateway.values());
        return "payment";
    }

    @PostMapping("/courses/{id}/payment")
    String pay(@PathVariable Long id, @RequestParam PaymentGateway gateway,
               HttpServletRequest request, Model model) {
        String email = currentEmail(request);
        if (email == null) return "redirect:/login";

        try {
            courses.enroll(email, id);
            return "redirect:/my-courses?success&gateway=" + gateway.name();
        } catch (Exception e) {
            model.addAttribute("course", courses.get(id));
            model.addAttribute("gateways", PaymentGateway.values());
            model.addAttribute("error", e.getMessage());
            return "payment";
        }
    }

    @GetMapping("/my-courses")
    String myCourses(HttpServletRequest request, Model model) {
        String email = currentEmail(request);
        if (email == null) return "redirect:/login";

        model.addAttribute("courses", courses.myCourses(email));
        return "my-courses";
    }

    @GetMapping("/courses/{id}/test")
    String test(@PathVariable Long id, HttpServletRequest request, Model model) {
        String email = currentEmail(request);
        if (email == null) return "redirect:/login";

        Enrollment enrollment = courses.enrollment(email, id);
        model.addAttribute("course", courses.get(id));
        model.addAttribute("questions", testQuestions());
        model.addAttribute("completed", enrollment.getCompletedAt() != null);
        return "test";
    }

    @PostMapping("/courses/{id}/test")
    String submitTest(@PathVariable Long id, @RequestParam Map<String, String> answers,
                      HttpServletRequest request, Model model) {
        String email = currentEmail(request);
        if (email == null) return "redirect:/login";

        int score = 0;
        Map<String, String> correctAnswers = Map.of(
                "q1", "Spring Boot",
                "q2", "Repository",
                "q3", "Database"
        );
        for (Map.Entry<String, String> answer : correctAnswers.entrySet()) {
            if (answer.getValue().equals(answers.get(answer.getKey()))) score++;
        }

        boolean passed = courses.completeTest(email, id, score);
        model.addAttribute("course", courses.get(id));
        model.addAttribute("questions", testQuestions());
        model.addAttribute("score", score);
        model.addAttribute("passed", passed);
        return "test-result";
    }

    @GetMapping("/courses/{id}/certificate")
    ResponseEntity<byte[]> certificate(@PathVariable Long id, HttpServletRequest request) {
        String email = currentEmail(request);
        if (email == null) return ResponseEntity.status(302).header("Location", "/login").build();

        Enrollment enrollment = courses.enrollment(email, id);
        if (enrollment.getCompletedAt() == null) return ResponseEntity.status(403).build();

        String name = HtmlUtils.htmlEscape(enrollment.getUser().getFullName());
        String course = HtmlUtils.htmlEscape(enrollment.getCourse().getTitle());
        String date = enrollment.getCompletedAt().format(DateTimeFormatter.ISO_LOCAL_DATE);
        String html = """
                <!doctype html><html><head><meta charset="UTF-8"><title>Certificate</title>
                <style>body{font-family:Georgia,serif;background:#f1ede5;text-align:center;padding:80px;color:#20231f}
                .certificate{max-width:850px;margin:auto;padding:80px 50px;border:12px solid #20231f;background:#fffdf8}
                h1{font-size:52px;margin:20px}h2{font-size:32px;color:#758f22}p{font-size:20px}</style></head>
                <body><main class="certificate"><p>LMS SYSTEM PRESENTS</p><h1>Certificate of Completion</h1>
                <p>This certifies that</p><h2>%s</h2><p>has successfully completed</p><h2>%s</h2>
                <p>Final test completed on %s</p></main></body></html>
                """.formatted(name, course, date);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename("lms-certificate.html").build().toString())
                .contentType(MediaType.TEXT_HTML)
                .body(html.getBytes(StandardCharsets.UTF_8));
    }

    private List<TestQuestion> testQuestions() {
        return List.of(
                new TestQuestion("q1", "Which framework powers this LMS application?", List.of("Spring Boot", "React", "Django")),
                new TestQuestion("q2", "Which layer normally handles database access?", List.of("Controller", "Repository", "Template")),
                new TestQuestion("q3", "What does JPA help manage?", List.of("Images", "Database", "CSS"))
        );
    }

    @GetMapping("/profile")
    String profile(HttpServletRequest request, Model model) {
        String email = currentEmail(request);
        if (email == null) return "redirect:/login";

        model.addAttribute("user", courses.profile(email));
        return "profile";
    }

    private String currentEmail(HttpServletRequest request) {
        if (request.getCookies() == null) return null;

        for (Cookie c : request.getCookies()) {
            if ("LMS_TOKEN".equals(c.getName()) && jwt.valid(c.getValue())) {
                return jwt.email(c.getValue());
            }
        }
        return null;
    }
}

package com.example.lms.controller;

import com.example.lms.service.CourseService;
import com.example.lms.security.JwtService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
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
    String courses(HttpServletRequest request, Model model) {
        model.addAttribute("courses", courses.all());
        model.addAttribute("loggedIn", currentEmail(request) != null);
        return "courses";
    }

    @GetMapping("/courses/{id}")
    String course(@PathVariable Long id, HttpServletRequest request, Model model) {
        String email = currentEmail(request);
        model.addAttribute("course", courses.get(id));
        model.addAttribute("loggedIn", email != null);
        model.addAttribute("enrolled", email != null && courses.isEnrolled(email, id));
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
            courses.enroll(email, id);
            return "redirect:/my-courses?success";
        } catch (Exception e) {
            model.addAttribute("course", courses.get(id));
            model.addAttribute("loggedIn", true);
            model.addAttribute("enrolled", courses.isEnrolled(email, id));
            model.addAttribute("error", e.getMessage());
            return "course-details";
        }
    }

    @GetMapping("/my-courses")
    String myCourses(HttpServletRequest request, Model model) {
        String email = currentEmail(request);
        if (email == null) return "redirect:/login";

        model.addAttribute("courses", courses.myCourses(email));
        return "my-courses";
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

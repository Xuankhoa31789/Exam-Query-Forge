package com.eqf.controller;

import java.util.Map;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.eqf.model.User;
import com.eqf.service.UserService;

@RestController
@RequestMapping("/api/register")
public class RegisterController {
    private final UserService userService;

    public RegisterController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public Map<String, Object> register(@RequestBody RegisterRequest request) {
        String name = request.name();
        String email = request.email();
        String password = request.password();

        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Name is required");
        }

        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email is required");
        }

        if (!email.contains("@")) {
            throw new IllegalArgumentException("Email must be valid");
        }

        if (password == null || password.length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters");
        }

        try {
            User user = userService.register(name, email, password);

            return Map.of(
                    "status", "success",
                    "message", "Registration successful",
                    "email", user.getEmail(),
                    "fullName", user.getFullName()
            );
        } catch (IllegalArgumentException e) {
            throw e;
        }
    }

    public static class RegisterRequest {
        private String name;
        private String email;
        private String password;

        public RegisterRequest() {}

        public RegisterRequest(String name, String email, String password) {
            this.name = name;
            this.email = email;
            this.password = password;
        }

        public String name() {
            return name;
        }

        public String email() {
            return email;
        }

        public String password() {
            return password;
        }

        public void setName(String name) {
            this.name = name;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }
}

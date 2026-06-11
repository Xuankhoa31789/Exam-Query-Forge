package com.eqf.controller;

import java.util.Map;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/register")
public class RegisterController {
    // Shared users map with LoginController
    private static final Map<String, String> users = UserRegistry.getUsers();

    @PostMapping
    public Map<String, Object> register(@RequestBody RegisterRequest request) {
        String username = request.username();
        String email = request.email();
        String password = request.password();

        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username is required");
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

        if (users.containsKey(username)) {
            throw new IllegalArgumentException("Username already exists");
        }

        users.put(username, password);

        return Map.of(
                "status", "success",
                "message", "Registration successful",
                "username", username,
                "email", email
        );
    }

    public static class RegisterRequest {
        private String username;
        private String email;
        private String password;

        public RegisterRequest() {}

        public RegisterRequest(String username, String email, String password) {
            this.username = username;
            this.email = email;
            this.password = password;
        }

        public String username() {
            return username;
        }

        public String email() {
            return email;
        }

        public String password() {
            return password;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }
}

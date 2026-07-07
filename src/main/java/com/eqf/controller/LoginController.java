package com.eqf.controller;

import java.util.Map;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.eqf.model.User;
import com.eqf.service.UserService;

@RestController
@RequestMapping("/api/login")
public class LoginController {
    private final UserService userService;

    public LoginController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public Map<String, Object> login(@RequestBody LoginRequest request) {
        String email = request.email();
        String password = request.password();

        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email is required");
        }

        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("Password is required");
        }

        try {
            User user = userService.login(email, password);

            return Map.of(
                    "status", "success",
                    "message", "Login successful",
                    "email", user.getEmail(),
                    "fullName", user.getFullName(),
                    "userId", user.getId(),
                    "token", generateToken(user.getId())
            );
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid email or password");
        }
    }

    private String generateToken(Long userId) {
        return "token_" + userId + "_" + System.currentTimeMillis();
    }

    public static class LoginRequest {
        private String email;
        private String password;

        public LoginRequest() {}

        public LoginRequest(String email, String password) {
            this.email = email;
            this.password = password;
        }

        public String email() {
            return email;
        }

        public String password() {
            return password;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }
}

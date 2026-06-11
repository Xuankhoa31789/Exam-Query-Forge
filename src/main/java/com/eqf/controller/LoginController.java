package com.eqf.controller;

import java.util.Map;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/login")
public class LoginController {
    private static final Map<String, String> users = UserRegistry.getUsers();

    @PostMapping
    public Map<String, Object> login(@RequestBody LoginRequest request) {
        String username = request.username();
        String password = request.password();

        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username is required");
        }

        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("Password is required");
        }

        if (!users.containsKey(username)) {
            throw new IllegalArgumentException("Invalid username or password");
        }

        if (!users.get(username).equals(password)) {
            throw new IllegalArgumentException("Invalid username or password");
        }

        return Map.of(
                "status", "success",
                "message", "Login successful",
                "username", username,
                "token", generateToken(username)
        );
    }

    private String generateToken(String username) {
        return "token_" + username + "_" + System.currentTimeMillis();
    }

    public static class LoginRequest {
        private String username;
        private String password;

        public LoginRequest() {}

        public LoginRequest(String username, String password) {
            this.username = username;
            this.password = password;
        }

        public String username() {
            return username;
        }

        public String password() {
            return password;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }
}

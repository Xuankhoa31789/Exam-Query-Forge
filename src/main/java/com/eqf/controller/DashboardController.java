package com.eqf.controller;

import java.time.Instant;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    @GetMapping("/health")
    public Map<String, Object> health() {
        return Map.of(
                "status", "ok",
                "service", "A LifeTime Project",
                "timestamp", Instant.now().toString()
        );
    }

    @GetMapping("/user/{username}")
    public Map<String, Object> getUserInfo(@PathVariable String username) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username is required");
        }

        return Map.of(
                "status", "success",
                "username", username,
                "timestamp", Instant.now().toString(),
                "message", "User dashboard data"
        );
    }

    @PostMapping("/profile")
    public Map<String, Object> updateProfile(@RequestBody ProfileRequest request) {
        if (request.username() == null || request.username().trim().isEmpty()) {
            throw new IllegalArgumentException("Username is required");
        }

        return Map.of(
                "status", "success",
                "message", "Profile updated successfully",
                "username", request.username(),
                "timestamp", Instant.now().toString()
        );
    }

    public static class ProfileRequest {
        private String username;
        private String bio;

        public ProfileRequest() {}

        public ProfileRequest(String username, String bio) {
            this.username = username;
            this.bio = bio;
        }

        public String username() {
            return username;
        }

        public String bio() {
            return bio;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public void setBio(String bio) {
            this.bio = bio;
        }
    }
}

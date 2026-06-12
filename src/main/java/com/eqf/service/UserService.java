package com.eqf.service;

import com.eqf.model.User;
import com.eqf.model.UserRole;
import com.eqf.model.VerifyStatus;
import com.eqf.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Register a new user with email and password.
     * Password is hashed using BCrypt before storage.
     */
    public User register(String fullName, String email, String plainPassword) {
        // Check if email already exists
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email already exists");
        }

        // Hash password using BCrypt
        String passwordHash = passwordEncoder.encode(plainPassword);

        // Create new user
        User user = new User(fullName, email, passwordHash);
        user.setRole(UserRole.TEACHER);
        user.setVerifyStatus(VerifyStatus.PENDING);

        return userRepository.save(user);
    }

    /**
     * Authenticate user by email and password.
     * Returns the user if credentials are valid, otherwise throws an exception.
     */
    public User login(String email, String plainPassword) {
        Optional<User> userOpt = userRepository.findByEmail(email);

        if (userOpt.isEmpty()) {
            throw new IllegalArgumentException("Invalid email or password");
        }

        User user = userOpt.get();

        // Compare plain password with hashed password using BCrypt
        if (!passwordEncoder.matches(plainPassword, user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid email or password");
        }

        return user;
    }

    /**
     * Find user by email
     */
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    /**
     * Check if email exists
     */
    public boolean emailExists(String email) {
        return userRepository.existsByEmail(email);
    }
}

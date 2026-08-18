package com.eqf.service;

import com.eqf.model.User;
import com.eqf.model.UserRole;
import com.eqf.model.VerifyStatus;
import com.eqf.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
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

        // Bootstrap: trên một CSDL trắng (ví dụ lần deploy production đầu tiên)
        // người đăng ký ĐẦU TIÊN trở thành ADMIN đã xác minh, để còn tạo được
        // bộ môn và duyệt các tài khoản sau. Không có bước này thì hệ thống
        // khởi động lên nhưng không ai dùng được gì nếu không vào SQL gõ tay.
        if (userRepository.count() == 0) {
            user.setRole(UserRole.ADMIN);
            user.setVerifyStatus(VerifyStatus.VERIFIED);
        } else {
            user.setRole(UserRole.TEACHER);
            user.setVerifyStatus(VerifyStatus.PENDING);
        }

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

    // ---------- Thao tác quản trị (AdminController đã kiểm quyền trước khi gọi) ----------

    /** Toàn bộ người dùng, mới nhất lên đầu. */
    public List<User> listAllUsers() {
        List<User> users = new ArrayList<>(userRepository.findAll());
        users.sort(Comparator.comparing(User::getId).reversed());
        return users;
    }

    /** Đặt trạng thái xác minh cho một tài khoản. */
    public User setVerifyStatus(Long userId, VerifyStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("Trạng thái xác minh là bắt buộc");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng id=" + userId));
        user.setVerifyStatus(status);
        return userRepository.save(user);
    }

    /** Đổi vai trò của một tài khoản (ví dụ nâng giáo viên lên tổ trưởng bộ môn). */
    public User setRole(Long userId, UserRole role) {
        if (role == null) {
            throw new IllegalArgumentException("Vai trò là bắt buộc");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng id=" + userId));
        user.setRole(role);
        return userRepository.save(user);
    }
}

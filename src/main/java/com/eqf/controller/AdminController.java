package com.eqf.controller;

import com.eqf.exception.ForbiddenException;
import com.eqf.model.User;
import com.eqf.model.UserRole;
import com.eqf.model.VerifyStatus;
import com.eqf.security.AuthenticatedUser;
import com.eqf.service.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Quản trị người dùng — thay cho việc phải mở SQL console gõ tay
 * (AGENTS.md trước đây hướng dẫn tự UPDATE verify_status trong DB).
 * Mọi endpoint ở đây chỉ dành cho ADMIN.
 */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final UserService userService;

    public AdminController(UserService userService) {
        this.userService = userService;
    }

    private void requireAdmin(AuthenticatedUser user) {
        if (user == null || user.role() != UserRole.ADMIN) {
            throw new ForbiddenException("Chỉ quản trị viên mới truy cập được khu vực này");
        }
    }

    private Map<String, Object> toView(User user) {
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("id", user.getId());
        view.put("fullName", user.getFullName());
        view.put("email", user.getEmail());
        view.put("role", user.getRole().name());
        view.put("verifyStatus", user.getVerifyStatus().name());
        return view;
    }

    @GetMapping("/users")
    public List<Map<String, Object>> listUsers(@AuthenticationPrincipal AuthenticatedUser user) {
        requireAdmin(user);
        return userService.listAllUsers().stream().map(this::toView).toList();
    }

    /** Duyệt / thu hồi xác minh. Body: { "status": "VERIFIED" | "PENDING" | "REJECTED" } */
    @PostMapping("/users/{id}/verify-status")
    public Map<String, Object> setVerifyStatus(@PathVariable Long id,
                                               @RequestBody Map<String, String> body,
                                               @AuthenticationPrincipal AuthenticatedUser user) {
        requireAdmin(user);
        return toView(userService.setVerifyStatus(id, parseEnum(VerifyStatus.class, body.get("status"))));
    }

    /** Đổi vai trò. Body: { "role": "TEACHER" | "DEPARTMENT_HEAD" | "ADMIN" } */
    @PostMapping("/users/{id}/role")
    public Map<String, Object> setRole(@PathVariable Long id,
                                       @RequestBody Map<String, String> body,
                                       @AuthenticationPrincipal AuthenticatedUser user) {
        requireAdmin(user);
        if (user.id().equals(id)) {
            throw new IllegalArgumentException("Không thể tự đổi vai trò của chính mình");
        }
        return toView(userService.setRole(id, parseEnum(UserRole.class, body.get("role"))));
    }

    private <E extends Enum<E>> E parseEnum(Class<E> type, String raw) {
        if (raw == null || raw.isBlank()) {
            throw new IllegalArgumentException("Thiếu giá trị bắt buộc");
        }
        try {
            return Enum.valueOf(type, raw.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Giá trị không hợp lệ: " + raw);
        }
    }
}

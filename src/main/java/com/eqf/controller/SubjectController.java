package com.eqf.controller;

import com.eqf.exception.ForbiddenException;
import com.eqf.model.Subject;
import com.eqf.model.UserRole;
import com.eqf.repository.SubjectRepository;
import com.eqf.security.AuthenticatedUser;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/** Bộ môn: ai đăng nhập cũng đọc được; chỉ ADMIN / tổ trưởng mới được tạo. */
@RestController
@RequestMapping("/api/subjects")
public class SubjectController {

    private final SubjectRepository subjectRepository;

    public SubjectController(SubjectRepository subjectRepository) {
        this.subjectRepository = subjectRepository;
    }

    @GetMapping
    public List<Map<String, Object>> list() {
        return subjectRepository.findAll().stream()
                .map(s -> Map.<String, Object>of("id", s.getId(), "name", s.getName()))
                .collect(Collectors.toList());
    }

    /**
     * Tạo bộ môn. Cần thiết để hệ thống tự khởi tạo được trên môi trường production:
     * DevDataInitializer không chạy ở profile prod nên bảng subjects sẽ rỗng, mà
     * không có bộ môn thì không tạo được câu hỏi lẫn kỳ thi.
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Object> create(@RequestBody Map<String, String> body,
                                      @AuthenticationPrincipal AuthenticatedUser user) {
        if (user.role() != UserRole.ADMIN && user.role() != UserRole.DEPARTMENT_HEAD) {
            throw new ForbiddenException("Chỉ quản trị viên hoặc tổ trưởng bộ môn mới được tạo bộ môn");
        }

        String name = body.get("name") == null ? "" : body.get("name").trim();
        if (name.isEmpty()) {
            throw new IllegalArgumentException("Tên bộ môn là bắt buộc");
        }
        if (name.length() > 100) {
            throw new IllegalArgumentException("Tên bộ môn tối đa 100 ký tự");
        }
        if (subjectRepository.existsByNameIgnoreCase(name)) {
            throw new IllegalArgumentException("Bộ môn '" + name + "' đã tồn tại");
        }

        Subject saved = subjectRepository.save(new Subject(name));
        return Map.of("id", saved.getId(), "name", saved.getName());
    }
}

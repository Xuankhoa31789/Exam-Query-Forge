package com.eqf.exception;

/**
 * Người dùng đã đăng nhập hợp lệ nhưng KHÔNG có quyền với tài nguyên này.
 * Khác với 401 (chưa đăng nhập / token hỏng) do Spring Security trả.
 * ApiExceptionHandler ánh xạ ngoại lệ này thành HTTP 403.
 */
public class ForbiddenException extends RuntimeException {
    public ForbiddenException(String message) {
        super(message);
    }
}

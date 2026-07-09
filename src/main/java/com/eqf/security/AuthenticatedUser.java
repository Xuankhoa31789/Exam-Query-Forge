package com.eqf.security;

import com.eqf.model.UserRole;

/** Principal đặt vào SecurityContext sau khi JWT được xác thực. */
public record AuthenticatedUser(Long id, String email, UserRole role) {}

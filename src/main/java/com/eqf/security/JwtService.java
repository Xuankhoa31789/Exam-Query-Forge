package com.eqf.security;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.eqf.model.User;
import com.eqf.model.UserRole;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

/** Sinh và xác thực JWT (HS256) chứa userId + email + role. */
@Service
public class JwtService {
    private final SecretKey key;
    private final Duration validity;

    public JwtService(@Value("${eqf.jwt.secret}") String secret,
                      @Value("${eqf.jwt.expiration-hours:24}") long expirationHours) {
        byte[] secretBytes = secret.getBytes(StandardCharsets.UTF_8);
        if (secretBytes.length < 32) {
            throw new IllegalStateException("eqf.jwt.secret phải dài tối thiểu 32 byte để dùng HS256");
        }
        this.key = Keys.hmacShaKeyFor(secretBytes);
        this.validity = Duration.ofHours(expirationHours);
    }

    public String generateToken(User user) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(String.valueOf(user.getId()))
                .claim("email", user.getEmail())
                .claim("role", user.getRole().name())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(validity)))
                .signWith(key)
                .compact();
    }

    /** @return principal đã xác thực, hoặc null nếu token sai chữ ký / hết hạn / sai định dạng. */
    public AuthenticatedUser parseToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return new AuthenticatedUser(
                    Long.valueOf(claims.getSubject()),
                    claims.get("email", String.class),
                    UserRole.valueOf(claims.get("role", String.class)));
        } catch (JwtException | IllegalArgumentException exception) {
            return null;
        }
    }
}

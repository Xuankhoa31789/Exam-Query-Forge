package com.eqf.security;

import com.eqf.model.User;
import com.eqf.model.UserRole;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {
    private static final String SECRET = "unit-test-secret-0123456789-0123456789-0123456789";

    private final JwtService jwtService = new JwtService(SECRET, 24);

    @Test
    void generatesTokenAndParsesBackSameClaims() {
        User user = new User("Giao vien test", "jwt-test@eqf.local", "hash");
        user.setId(42L);
        user.setRole(UserRole.DEPARTMENT_HEAD);

        String token = jwtService.generateToken(user);
        AuthenticatedUser parsed = jwtService.parseToken(token);

        assertThat(parsed).isNotNull();
        assertThat(parsed.id()).isEqualTo(42L);
        assertThat(parsed.email()).isEqualTo("jwt-test@eqf.local");
        assertThat(parsed.role()).isEqualTo(UserRole.DEPARTMENT_HEAD);
    }

    @Test
    void rejectsGarbageAndTokenSignedWithOtherSecret() {
        assertThat(jwtService.parseToken("not-a-jwt")).isNull();

        User user = new User("Ke gia mao", "fake@eqf.local", "hash");
        user.setId(1L);
        JwtService otherService = new JwtService("another-secret-9876543210-9876543210-9876543210", 24);
        String foreignToken = otherService.generateToken(user);

        assertThat(jwtService.parseToken(foreignToken)).isNull();
    }

    @Test
    void rejectsExpiredToken() {
        User user = new User("Het han", "expired@eqf.local", "hash");
        user.setId(7L);
        JwtService expiredIssuer = new JwtService(SECRET, -1);

        assertThat(jwtService.parseToken(expiredIssuer.generateToken(user))).isNull();
    }

    @Test
    void rejectsSecretShorterThan32Bytes() {
        assertThatThrownBy(() -> new JwtService("too-short", 24))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("32");
    }
}

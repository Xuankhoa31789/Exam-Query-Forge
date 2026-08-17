package com.eqf.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.eqf.security.JwtAuthFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * Configure BCryptPasswordEncoder as the password encoder
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Stateless JWT security: static pages + login/register are public,
     * every other endpoint requires a valid Bearer token (401 otherwise).
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, JwtAuthFilter jwtAuthFilter) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Trang tĩnh + URL sạch tương ứng (xem WebConfig). Việc chặn người chưa
                // đăng nhập do phía client lo (shell.js → eqfRequireLogin); dữ liệu thật
                // vẫn được bảo vệ ở tầng /api/**.
                .requestMatchers("/", "/login", "/home", "/questions", "/exams", "/voting").permitAll()
                .requestMatchers("/index.html", "/home.html", "/questions.html", "/exams.html", "/voting.html")
                        .permitAll()
                .requestMatchers("/styles.css", "/app.js", "/auth.js", "/shell.js",
                        "/favicon.ico").permitAll()
                // Không tìm thấy tài nguyên -> Spring forward sang /error. Nếu /error cũng
                // đòi xác thực thì mọi 404 sẽ hiện thành 401, rất khó debug.
                .requestMatchers("/error").permitAll()
                .requestMatchers("/api/login", "/api/register", "/api/dashboard/health").permitAll()
                .requestMatchers("/h2-console/**").permitAll()
                .anyRequest().authenticated()
            )
            .exceptionHandling(handler -> handler.authenticationEntryPoint((request, response, exception) -> {
                response.setStatus(HttpStatus.UNAUTHORIZED.value());
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                response.setCharacterEncoding("UTF-8");
                response.getWriter().write("{\"message\":\"Unauthorized\"}");
            }))
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
            .headers(headers -> headers.frameOptions(frameOptions -> frameOptions.disable()));

        return http.build();
    }
}

package com.eqf.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * URL sạch cho các trang tĩnh: /home thay vì /home.html.
 * Chỉ là forward nội bộ — file thật vẫn nằm trong src/main/resources/static.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addViewControllers(@NonNull ViewControllerRegistry registry) {
        registry.addViewController("/").setViewName("forward:/index.html");
        registry.addViewController("/login").setViewName("forward:/index.html");
        registry.addViewController("/home").setViewName("forward:/home.html");
        registry.addViewController("/questions").setViewName("forward:/questions.html");
        registry.addViewController("/exams").setViewName("forward:/exams.html");
        registry.addViewController("/voting").setViewName("forward:/voting.html");
        registry.addViewController("/admin").setViewName("forward:/admin.html");
    }
}

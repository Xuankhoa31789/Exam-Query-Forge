package com.eqf.config;

import com.eqf.model.*;
import com.eqf.repository.ChapterRepository;
import com.eqf.repository.SubjectRepository;
import com.eqf.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Seed dữ liệu mẫu cho môi trường dev (chỉ chạy khi bảng subjects còn trống).
 * Giúp test ngay: đã có bộ môn, chương, và 1 giáo viên đã xác minh.
 * Khi lên production nên tắt/loại bỏ file này.
 */
@Configuration
public class DevDataInitializer {

    @Bean
    CommandLineRunner seedDevData(SubjectRepository subjects,
                                  ChapterRepository chapters,
                                  UserRepository users,
                                  PasswordEncoder passwordEncoder) {
        return args -> {
            if (subjects.count() > 0) {
                return; // đã có dữ liệu, không seed lại
            }

            Subject toan = subjects.save(new Subject("Toán"));
            subjects.save(new Subject("Vật lý"));
            subjects.save(new Subject("Hóa học"));

            Chapter ch = chapters.save(new Chapter(toan, "Hàm số bậc hai", 10));

            User teacher = new User("Giáo viên Demo", "teacher@eqf.local",
                    passwordEncoder.encode("password123"));
            teacher.setRole(UserRole.TEACHER);
            teacher.setVerifyStatus(VerifyStatus.VERIFIED);
            teacher.setSubject(toan);
            teacher = users.save(teacher);

            System.out.println("====================================================");
            System.out.println(" [DEV SEED] Dữ liệu mẫu đã được tạo:");
            System.out.println("   Bộ môn Toán id = " + toan.getId());
            System.out.println("   Chương 'Hàm số bậc hai' id = " + ch.getId());
            System.out.println("   Giáo viên (VERIFIED) id = " + teacher.getId()
                    + "  | login: teacher@eqf.local / password123");
            System.out.println("   -> Dùng authorId=" + teacher.getId() + " khi tạo câu hỏi thử.");
            System.out.println("====================================================");
        };
    }
}

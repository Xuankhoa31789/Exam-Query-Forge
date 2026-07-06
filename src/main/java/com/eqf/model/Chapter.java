package com.eqf.model;

import jakarta.persistence.*;

/** Chương / chủ đề thuộc một bộ môn. Dùng để gắn nhãn câu hỏi. */
@Entity
@Table(name = "chapters",
       uniqueConstraints = @UniqueConstraint(columnNames = {"subject_id", "name", "grade"}))
public class Chapter {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    @Column(length = 200, nullable = false)
    private String name;

    /** Lớp 10 / 11 / 12 (có thể null). */
    private Integer grade;

    public Chapter() {}

    public Chapter(Subject subject, String name, Integer grade) {
        this.subject = subject;
        this.name = name;
        this.grade = grade;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Subject getSubject() { return subject; }
    public void setSubject(Subject subject) { this.subject = subject; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Integer getGrade() { return grade; }
    public void setGrade(Integer grade) { this.grade = grade; }
}

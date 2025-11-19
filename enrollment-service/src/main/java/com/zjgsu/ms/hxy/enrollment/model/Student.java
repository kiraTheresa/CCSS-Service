package com.zjgsu.ms.hxy.enrollment.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;
import java.util.UUID;


/**
 *
 * Student 实体字段说明：
 * • id: String 类型，唯一标识符，系统自动生成 UUID
 * • studentId: String 类型，学号（如 “2024001”），必须全局唯一，不可重复
 * • name: String 类型，学生姓名，必填
 * • major: String 类型，专业名称（如 “计算机科学与技术”），必填
 * • grade: Integer 类型，入学年份（如 2024），必填
 * • email: String 类型，邮箱地址，必填且必须符合邮箱格式
 * • createdAt: LocalDateTime 类型，创建时间戳，系统自动生成
 *
 */

@Entity
@Table(name = "students",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "studentId"),
                @UniqueConstraint(columnNames = "email")
        },
        indexes = {
                @Index(name = "idx_student_id", columnList = "studentId"),
                @Index(name = "idx_email", columnList = "email"),
                @Index(name = "idx_major", columnList = "major"),
                @Index(name = "idx_grade", columnList = "grade")
        })
public class Student {

    @Id
    @GeneratedValue
    private UUID id;

    @NotBlank(message = "学号不能为空")
    @Column(unique = true, nullable = false)
    private String studentId;

    @NotBlank(message = "学生姓名不能为空")
    @Column(nullable = false)
    private String name;

    @NotBlank(message = "专业名称不能为空")
    @Column(nullable = false)
    private String major;

    @NotNull(message = "入学年份不能为空")
    @Min(value = 2000, message = "入学年份必须大于2000")
    @Max(value = 2100, message = "入学年份必须小于2100")
    @Column(nullable = false)
    private Integer grade;

    @NotBlank(message = "邮箱地址不能为空")
    @Email(message = "邮箱格式不正确")
    @Column(unique = true, nullable = false)
    private String email;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // 默认构造函数
    public Student() {
    }

    // 带参构造函数（不含id和createdAt）
    public Student(String studentId, String name, String major, Integer grade, String email) {
        this.studentId = studentId;
        this.name = name;
        this.major = major;
        this.grade = grade;
        this.email = email;
    }

    // Getter 和 Setter 方法
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMajor() {
        return major;
    }

    public void setMajor(String major) {
        this.major = major;
    }

    public Integer getGrade() {
        return grade;
    }

    public void setGrade(Integer grade) {
        this.grade = grade;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "Student{" +
                "id=" + id +
                ", studentId='" + studentId + '\'' +
                ", name='" + name + '\'' +
                ", major='" + major + '\'' +
                ", grade=" + grade +
                ", email='" + email + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}
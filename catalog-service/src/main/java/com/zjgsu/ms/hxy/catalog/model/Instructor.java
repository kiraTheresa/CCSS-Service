package com.zjgsu.ms.hxy.catalog.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;


/**
 * Instructor 实体类
 * 代表教师信息，包含教师基本信息、联系方式和所属院系
 *
 * @author System
 * @version 1.0
 * @since 2024
 */
@Entity
@Table(name = "instructors",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "instructorId"),
                @UniqueConstraint(columnNames = "email")
        })
public class Instructor {

    /**
     * 唯一标识符，系统自动生成 UUID
     */
    @Id
    @GeneratedValue
    private UUID id;

    /**
     * 教师编号（如 "T001"），必须全局唯一，不可重复
     */
    @NotBlank(message = "教师编号不能为空")
    @Pattern(regexp = "^T\\d{3,}$", message = "教师编号格式不正确，应以T开头后跟至少3位数字")
    @Column(unique = true, nullable = false, length = 20)
    private String instructorId;

    /**
     * 教师姓名（如 "张教授"），必填
     */
    @NotBlank(message = "教师姓名不能为空")
    @Size(min = 2, max = 50, message = "教师姓名长度应在2-50个字符之间")
    @Column(nullable = false, length = 50)
    private String name;

    /**
     * 教师邮箱地址（如 "zhang@example.edu.cn"），必填且必须符合邮箱格式
     */
    @NotBlank(message = "邮箱地址不能为空")
    @Email(message = "邮箱格式不正确")
    @Column(unique = true, nullable = false, length = 100)
    private String email;

    /**
     * 所属院系（如 "计算机学院"），必填
     */
    @NotBlank(message = "所属院系不能为空")
    @Size(max = 100, message = "院系名称长度不能超过100个字符")
    @Column(nullable = false, length = 100)
    private String department;

    /**
     * 创建时间戳，系统自动生成
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // 默认构造函数
    public Instructor() {
    }

    /**
     * 带参构造函数（不含id和createdAt）
     *
     * @param instructorId 教师编号
     * @param name 教师姓名
     * @param email 教师邮箱
     * @param department 所属院系
     */
    public Instructor(String instructorId, String name, String email, String department) {
        this.instructorId = instructorId;
        this.name = name;
        this.email = email;
        this.department = department;
    }

    // Getter 和 Setter 方法

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getInstructorId() {
        return instructorId;
    }

    public void setInstructorId(String instructorId) {
        this.instructorId = instructorId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "Instructor{" +
                "id=" + id +
                ", instructorId='" + instructorId + '\'' +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", department='" + department + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }

}
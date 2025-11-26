package com.zjgsu.ms.hxy.catalog.model;


import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;
import java.util.UUID;


/**
 * Course 实体类
 * 代表课程信息，包含课程基本信息、授课教师、时间安排和容量限制
 *
 * @author System
 * @version 1.0
 * @since 2024
 */
@Entity
@Table(name = "courses",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "code")
        },
        indexes = {
                @Index(name = "idx_code", columnList = "code"),
                @Index(name = "idx_instructor_id", columnList = "instructor_id"),
                @Index(name = "idx_schedule_id", columnList = "schedule_id"),
                @Index(name = "idx_title", columnList = "title")
        })
public class Course {

    /**
     * 唯一标识符，系统自动生成 UUID
     */
    @Id
    @GeneratedValue
    private UUID id;

    /**
     * 课程编号（如 "CS101"），必须全局唯一，不可重复
     */
    @NotBlank(message = "课程编号不能为空")
    @Column(unique = true, nullable = false, length = 20)
    private String code;

    /**
     * 课程标题（如 "计算机科学导论"），必填
     */
    @NotBlank(message = "课程标题不能为空")
    @Size(max = 100, message = "课程标题长度不能超过100个字符")
    @Column(nullable = false, length = 100)
    private String title;

    /**
     * 授课教师的唯一标识符，对应 Instructor 实体的 id，必填
     */
    @NotBlank(message = "授课教师ID不能为空")
    @Column(name = "instructor_id", nullable = false)
    private String instructorId;

    /**
     * 课程对应的时间表编号，对应 ScheduleSlot 实体的 id，必填
     */
    @NotBlank(message = "时间表编号不能为空")
    @Column(name = "schedule_id", nullable = false)
    private String scheduleId;

    /**
     * 课程最大可选人数（如 60），必填且必须为正数
     */
    @NotNull(message = "课程容量不能为空")
    @Min(value = 1, message = "课程容量必须大于0")
    @Max(value = 500, message = "课程容量不能超过500")
    @Column(nullable = false)
    private Integer capacity;

    /**
     * 创建时间戳，系统自动生成
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 当前选课人数
     */
    @Column(nullable = false)
    private Integer enrolled = 0;

    /**
     * 课程描述（可选）
     */
    @Size(max = 1000, message = "课程描述长度不能超过1000个字符")
    @Column(length = 1000)
    private String description;

    /**
     * 课程学分（可选）
     */
    @Min(value = 0, message = "学分不能为负数")
    @Max(value = 10, message = "学分不能超过10")
    @Column
    private Integer credits;

    /**
     * 上课地点（可选）
     */
    @Size(max = 200, message = "上课地点长度不能超过200个字符")
    @Column(length = 200)
    private String location;

    // 默认构造函数
    public Course() {
    }

    /**
     * 带参构造函数（不含id和createdAt）
     *
     * @param code 课程编号
     * @param title 课程标题
     * @param instructorId 授课教师ID
     * @param scheduleId 时间表ID
     * @param capacity 课程容量
     */
    public Course(String code, String title, String instructorId, String scheduleId, Integer capacity) {
        this.code = code;
        this.title = title;
        this.instructorId = instructorId;
        this.scheduleId = scheduleId;
        this.capacity = capacity;
        this.enrolled = 0; // 初始化选课人数为0
    }

    // Getter 和 Setter 方法

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getInstructorId() {
        return instructorId;
    }

    public void setInstructorId(String instructorId) {
        this.instructorId = instructorId;
    }

    public String getScheduleId() {
        return scheduleId;
    }

    public void setScheduleId(String scheduleId) {
        this.scheduleId = scheduleId;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Integer getEnrolled() {
        return enrolled;
    }

    public void setEnrolled(Integer enrolled) {
        this.enrolled = enrolled;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getCredits() {
        return credits;
    }

    public void setCredits(Integer credits) {
        this.credits = credits;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    @Override
    public String toString() {
        return "Course{" +
                "id=" + id +
                ", code='" + code + '\'' +
                ", title='" + title + '\'' +
                ", instructorId='" + instructorId + '\'' +
                ", scheduleId='" + scheduleId + '\'' +
                ", capacity=" + capacity +
                ", createdAt=" + createdAt +
                ", enrolled=" + enrolled +
                ", description='" + description + '\'' +
                ", credits=" + credits +
                ", location='" + location + '\'' +
                '}';
    }

}
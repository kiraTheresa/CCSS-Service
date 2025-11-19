package com.zjgsu.ms.hxy.enrollment.repository;

import com.zjgsu.ms.hxy.enrollment.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * StudentRepository 接口
 * 使用 Spring Data JPA 实现数据库持久化，提供学生的 CRUD 操作
 *
 * @author System
 * @version 1.1.0
 * @since 2024
 */
@Repository
public interface StudentRepository extends JpaRepository<Student, UUID> {

    /**
     * 根据学号查找学生
     * @param studentId 学号
     * @return 包含学生的Optional，如果不存在则返回空Optional
     */
    Optional<Student> findByStudentId(String studentId);

    /**
     * 根据邮箱查找学生
     * @param email 邮箱地址
     * @return 包含学生的Optional，如果不存在则返回空Optional
     */
    Optional<Student> findByEmail(String email);

    /**
     * 根据专业查找学生
     * @param major 专业名称
     * @return 该专业的所有学生列表
     */
    List<Student> findByMajorIgnoreCase(String major);

    /**
     * 根据入学年份查找学生
     * @param grade 入学年份
     * @return 该年级的所有学生列表
     */
    List<Student> findByGrade(Integer grade);

    /**
     * 根据姓名关键词搜索学生
     * @param keyword 关键词
     * @return 匹配的学生列表
     */
    List<Student> findByNameContainingIgnoreCase(String keyword);

    /**
     * 检查学号是否存在
     * @param studentId 学号
     * @return 如果学号存在返回true，否则返回false
     */
    boolean existsByStudentId(String studentId);

    /**
     * 检查邮箱是否存在
     * @param email 邮箱地址
     * @return 如果邮箱存在返回true，否则返回false
     */
    boolean existsByEmail(String email);
}
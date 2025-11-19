package com.zjgsu.ms.hxy.catalog.repository;

import com.zjgsu.ms.hxy.catalog.model.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * CourseRepository 接口
 * 使用 Spring Data JPA 实现数据库持久化，提供课程的 CRUD 操作
 *
 * @author System
 * @version 1.1.0
 * @since 2024
 */
@Repository
public interface CourseRepository extends JpaRepository<Course, UUID> {

    /**
     * 根据课程编号查找课程
     * @param code 课程编号
     * @return 包含课程的Optional，如果不存在则返回空Optional
     */
    Optional<Course> findByCode(String code);

    /**
     * 根据教师ID查找课程
     * @param instructorId 教师ID
     * @return 该教师的所有课程列表
     */
    List<Course> findByInstructorId(String instructorId);
    
    /**
     * 根据时间表ID查找课程
     * @param scheduleId 时间表ID
     * @return 使用该时间表的课程列表
     */
    List<Course> findByScheduleId(String scheduleId);
    
    /**
     * 根据课程标题关键词搜索课程
     * @param keyword 关键词
     * @return 匹配的课程列表
     */
    List<Course> findByTitleContaining(String keyword);
    
    /**
     * 根据容量范围查找课程
     * @param minCapacity 最小容量
     * @param maxCapacity 最大容量
     * @return 容量在指定范围内的课程列表
     */
    List<Course> findByCapacityBetween(Integer minCapacity, Integer maxCapacity);
    
    /**
     * 根据课程编号删除课程
     * @param code 课程编号
     * @return 如果课程存在并被删除返回true，否则返回false
     */
    boolean deleteByCode(String code);
    
    /**
     * 检查课程编号是否存在
     * @param code 课程编号
     * @return 如果课程编号存在返回true，否则返回false
     */
    boolean existsByCode(String code);
    
    /**
     * 查找有剩余容量的课程（已选人数小于容量）
     * @return 有剩余容量的课程列表
     */
    @Query("SELECT c FROM Course c WHERE c.enrolled < c.capacity")
    List<Course> findCoursesWithAvailableCapacity();
    
    /**
     * 统计有剩余容量的课程数量
     * @return 有剩余容量的课程数量
     */
    @Query("SELECT COUNT(c) FROM Course c WHERE c.enrolled < c.capacity")
    long countCoursesWithAvailableCapacity();
}
package com.zjgsu.ms.hxy.catalog.service;

import com.zjgsu.ms.hxy.catalog.model.Course;
import com.zjgsu.ms.hxy.catalog.repository.CourseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * CourseService 类
 * 实现课程相关的业务逻辑，包括课程创建、更新、删除、查询等业务规则
 *
 * @author System
 * @version 1.0
 * @since 2024
 */
@Service
public class CourseService {

    private final CourseRepository courseRepository;

    public CourseService(CourseRepository courseRepository) {
        this.courseRepository = courseRepository;
    }

    /**
     * 获取所有课程
     * @return 课程列表
     */
    public List<Course> getAllCourses() {
        return courseRepository.findAll();
    }

    /**
     * 根据ID获取课程
     * @param id 课程ID
     * @return 包含课程的Optional
     */
    public Optional<Course> getCourseById(UUID id) {
        return courseRepository.findById(id);
    }

    /**
     * 根据课程编号获取课程
     * @param code 课程编号
     * @return 包含课程的Optional
     */
    public Optional<Course> getCourseByCode(String code) {
        return courseRepository.findByCode(code);
    }

    /**
     * 创建新课程
     * @param course 课程信息
     * @return 创建后的课程
     * @throws IllegalArgumentException 如果课程编号已存在或数据验证失败
     */
    @Transactional
    public Course createCourse(Course course) {
        // 验证课程数据
        validateCourse(course);

        // 检查课程编号是否已存在
        if (courseRepository.existsByCode(course.getCode())) {
            throw new IllegalArgumentException("课程编号已存在: " + course.getCode());
        }

        // 设置ID为null，确保创建新课程
        course.setId(null);

        return courseRepository.save(course);
    }

    /**
     * 更新课程信息
     * @param id 课程ID
     * @param course 更新后的课程信息
     * @return 更新后的课程Optional
     * @throws IllegalArgumentException 如果数据验证失败或课程不存在
     */
    @Transactional
    public Optional<Course> updateCourse(UUID id, Course course) {
        // 验证课程是否存在
        if (!courseRepository.existsById(id)) {
            throw new IllegalArgumentException("课程不存在，ID: " + id);
        }

        // 验证课程数据
        validateCourse(course);

        // 检查课程编号是否被其他课程使用
        Optional<Course> existingCourseWithSameCode = courseRepository.findByCode(course.getCode());
        if (existingCourseWithSameCode.isPresent() &&
                !existingCourseWithSameCode.get().getId().equals(id)) {
            throw new IllegalArgumentException("课程编号已被其他课程使用: " + course.getCode());
        }

        // 设置ID并保存
        course.setId(id);
        return Optional.of(courseRepository.save(course));
    }

    /**
     * 部分更新课程信息
     * @param id 课程ID
     * @param updates 包含更新字段的Map
     * @return 更新后的课程Optional
     * @throws IllegalArgumentException 如果课程不存在或数据验证失败
     */
    public Optional<Course> partialUpdateCourse(UUID id, Map<String, Object> updates) {
        // 验证课程是否存在
        if (!courseRepository.existsById(id)) {
            throw new IllegalArgumentException("课程不存在，ID: " + id);
        }

        // 验证更新数据
        validatePartialUpdates(updates);

        // 获取现有课程
        Optional<Course> existingCourse = courseRepository.findById(id);
        if (existingCourse.isPresent()) {
            Course course = existingCourse.get();
            
            // 应用部分更新
            for (Map.Entry<String, Object> entry : updates.entrySet()) {
                String field = entry.getKey();
                Object value = entry.getValue();
                
                switch (field) {
                    case "code":
                        course.setCode((String) value);
                        break;
                    case "title":
                        course.setTitle((String) value);
                        break;
                    case "description":
                        course.setDescription((String) value);
                        break;
                    case "credits":
                        course.setCredits((Integer) value);
                        break;
                    case "capacity":
                        course.setCapacity((Integer) value);
                        break;
                    case "enrolled":
                        course.setEnrolled((Integer) value);
                        break;
                    case "instructorId":
                        course.setInstructorId((String) value);
                        break;
                    case "scheduleId":
                        course.setScheduleId((String) value);
                        break;
                    case "location":
                        course.setLocation((String) value);
                        break;
                    // 可以根据需要添加更多字段
                }
            }
            
            // 保存更新后的课程
            return Optional.of(courseRepository.save(course));
        }
        
        return Optional.empty();
    }

    /**
     * 删除课程
     * @param id 课程ID
     * @return 如果删除成功返回true，否则返回false
     */
    @Transactional
    public boolean deleteCourse(UUID id) {
        if (courseRepository.existsById(id)) {
            courseRepository.deleteById(id);
            return true;
        }
        return false;
    }

    /**
     * 根据课程编号删除课程
     * @param code 课程编号
     * @return 如果删除成功返回true，否则返回false
     */
    public boolean deleteCourseByCode(String code) {
        return courseRepository.deleteByCode(code);
    }

    /**
     * 根据教师ID获取课程列表
     * @param instructorId 教师ID
     * @return 该教师的所有课程列表
     */
    public List<Course> getCoursesByInstructor(String instructorId) {
        if (!StringUtils.hasText(instructorId)) {
            throw new IllegalArgumentException("教师ID不能为空");
        }
        return courseRepository.findByInstructorId(instructorId);
    }

    /**
     * 根据时间表ID获取课程列表
     * @param scheduleId 时间表ID
     * @return 使用该时间表的课程列表
     */
    public List<Course> getCoursesBySchedule(String scheduleId) {
        if (!StringUtils.hasText(scheduleId)) {
            throw new IllegalArgumentException("时间表ID不能为空");
        }
        return courseRepository.findByScheduleId(scheduleId);
    }

    /**
     * 根据标题关键词搜索课程
     * @param keyword 关键词
     * @return 匹配的课程列表
     */
    public List<Course> searchCoursesByTitle(String keyword) {
        if (!StringUtils.hasText(keyword)) {
            throw new IllegalArgumentException("搜索关键词不能为空");
        }
        return courseRepository.findByTitleContaining(keyword);
    }

    /**
     * 根据容量范围查找课程
     * @param minCapacity 最小容量
     * @param maxCapacity 最大容量
     * @return 容量在指定范围内的课程列表
     * @throws IllegalArgumentException 如果容量范围无效
     */
    public List<Course> findCoursesByCapacityRange(Integer minCapacity, Integer maxCapacity) {
        if (minCapacity == null || maxCapacity == null) {
            throw new IllegalArgumentException("容量范围不能为空");
        }
        if (minCapacity < 1 || maxCapacity < 1) {
            throw new IllegalArgumentException("容量必须大于0");
        }
        if (minCapacity > maxCapacity) {
            throw new IllegalArgumentException("最小容量不能大于最大容量");
        }

        return courseRepository.findByCapacityBetween(minCapacity, maxCapacity);
    }

    /**
     * 检查课程是否存在
     * @param id 课程ID
     * @return 如果课程存在返回true
     */
    public boolean courseExists(UUID id) {
        return courseRepository.existsById(id);
    }

    /**
     * 检查课程编号是否存在
     * @param code 课程编号
     * @return 如果课程编号存在返回true
     */
    public boolean courseCodeExists(String code) {
        return courseRepository.existsByCode(code);
    }

    /**
     * 获取课程总数
     * @return 课程数量
     */
    public long getCourseCount() {
        return courseRepository.count();
    }

    /**
     * 验证课程数据的完整性
     * @param course 课程对象
     * @throws IllegalArgumentException 如果数据验证失败
     */
    private void validateCourse(Course course) {
        if (course == null) {
            throw new IllegalArgumentException("课程不能为空");
        }

        if (!StringUtils.hasText(course.getCode())) {
            throw new IllegalArgumentException("课程编号不能为空");
        }

        if (!StringUtils.hasText(course.getTitle())) {
            throw new IllegalArgumentException("课程标题不能为空");
        }

        if (!StringUtils.hasText(course.getInstructorId())) {
            throw new IllegalArgumentException("授课教师ID不能为空");
        }

        if (!StringUtils.hasText(course.getScheduleId())) {
            throw new IllegalArgumentException("时间表ID不能为空");
        }

        if (course.getCapacity() == null || course.getCapacity() <= 0) {
            throw new IllegalArgumentException("课程容量必须大于0");
        }

        if (course.getCapacity() > 500) {
            throw new IllegalArgumentException("课程容量不能超过500");
        }
    }

    /**
     * 验证部分更新数据的有效性
     * @param updates 更新字段Map
     * @throws IllegalArgumentException 如果数据验证失败
     */
    private void validatePartialUpdates(Map<String, Object> updates) {
        if (updates == null || updates.isEmpty()) {
            throw new IllegalArgumentException("更新数据不能为空");
        }

        // 验证容量字段
        if (updates.containsKey("capacity")) {
            Object capacityObj = updates.get("capacity");
            if (!(capacityObj instanceof Integer)) {
                throw new IllegalArgumentException("课程容量必须是整数");
            }
            Integer capacity = (Integer) capacityObj;
            if (capacity <= 0) {
                throw new IllegalArgumentException("课程容量必须大于0");
            }
            if (capacity > 500) {
                throw new IllegalArgumentException("课程容量不能超过500");
            }
        }

        // 验证课程编号字段
        if (updates.containsKey("code")) {
            Object codeObj = updates.get("code");
            if (!(codeObj instanceof String) || !StringUtils.hasText((String) codeObj)) {
                throw new IllegalArgumentException("课程编号不能为空");
            }
        }

        // 验证课程标题字段
        if (updates.containsKey("title")) {
            Object titleObj = updates.get("title");
            if (!(titleObj instanceof String) || !StringUtils.hasText((String) titleObj)) {
                throw new IllegalArgumentException("课程标题不能为空");
            }
        }
    }


    /**
     * 获取课程的剩余容量
     * @param courseId 课程ID
     * @param currentEnrollment 当前选课人数
     * @return 剩余容量
     */
    public int getRemainingCapacity(UUID courseId, int currentEnrollment) {
        Optional<Course> course = courseRepository.findById(courseId);
        if (course.isEmpty()) {
            throw new IllegalArgumentException("课程不存在，ID: " + courseId);
        }

        int remaining = course.get().getCapacity() - currentEnrollment;
        return Math.max(remaining, 0);
    }

    /**
     * 增加课程选课人数
     * @param courseId 课程ID
     * @return 更新后的课程Optional
     */
    @Transactional
    public Optional<Course> incrementEnrolled(UUID courseId) {
        Optional<Course> courseOpt = courseRepository.findById(courseId);
        if (courseOpt.isPresent()) {
            Course course = courseOpt.get();
            course.setEnrolled(course.getEnrolled() + 1);
            return Optional.of(courseRepository.save(course));
        }
        return Optional.empty();
    }

    /**
     * 减少课程选课人数
     * @param courseId 课程ID
     * @return 更新后的课程Optional
     */
    @Transactional
    public Optional<Course> decrementEnrolled(UUID courseId) {
        Optional<Course> courseOpt = courseRepository.findById(courseId);
        if (courseOpt.isPresent()) {
            Course course = courseOpt.get();
            int newEnrolled = Math.max(0, course.getEnrolled() - 1);
            course.setEnrolled(newEnrolled);
            return Optional.of(courseRepository.save(course));
        }
        return Optional.empty();
    }

    /**
     * 检查课程是否已满（基于 enrolled 字段）
     * @param courseId 课程ID
     * @return 如果课程已满返回true
     */
    public boolean isCourseFull(UUID courseId) {
        Optional<Course> course = courseRepository.findById(courseId);
        return course.map(c -> c.getEnrolled() >= c.getCapacity()).orElse(true);
    }

    //
    public boolean isInitialized() {
        return courseRepository.count() > 0;
    }

}
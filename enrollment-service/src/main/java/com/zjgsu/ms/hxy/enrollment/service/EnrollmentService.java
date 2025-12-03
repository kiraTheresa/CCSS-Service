package com.zjgsu.ms.hxy.enrollment.service;

import com.zjgsu.ms.hxy.enrollment.model.Enrollment;
import com.zjgsu.ms.hxy.enrollment.model.EnrollmentStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * EnrollmentService 类
 * 实现选课相关的业务逻辑，包括选课、退课、成绩管理等业务规则
 * 处理 Student ───< Enrollment >─── Course 之间的约束关系
 *
 * @author System
 * @version 1.0
 * @since 2024
 */
@Service
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final StudentService studentService;
    private final RestTemplate restTemplate;
    
    @Value("${catalog-service.url}")
    private String catalogServiceUrl;

    public EnrollmentService(EnrollmentRepository enrollmentRepository,
                             @Lazy StudentService studentService,
                             RestTemplate restTemplate) {
        this.enrollmentRepository = enrollmentRepository;
        this.studentService = studentService;
        this.restTemplate = restTemplate;
    }
    /**
     * 获取所有选课记录
     * @return 选课记录列表
     */
    public List<Enrollment> getAllEnrollments() {
        return enrollmentRepository.findAll();
    }

    /**
     * 根据ID获取选课记录
     * @param id 选课记录ID
     * @return 包含选课记录的Optional
     */
    public Optional<Enrollment> getEnrollmentById(UUID id) {
        return enrollmentRepository.findById(id);
    }

    /**
     * 根据课程ID获取选课记录
     * @param courseId 课程ID
     * @return 该课程的所有选课记录列表
     */
    public List<Enrollment> getEnrollmentsByCourse(String courseId) {
        if (!StringUtils.hasText(courseId)) {
            throw new IllegalArgumentException("课程ID不能为空");
        }
        return enrollmentRepository.findByCourseId(courseId);
    }

    /**
     * 根据学生ID获取选课记录
     * @param studentId 学生ID
     * @return 该学生的所有选课记录列表
     */
    public List<Enrollment> getEnrollmentsByStudent(String studentId) {
        if (!StringUtils.hasText(studentId)) {
            throw new IllegalArgumentException("学生ID不能为空");
        }
        return enrollmentRepository.findByStudentId(studentId);
    }

    /**
     * 根据状态获取选课记录
     * @param status 选课状态字符串
     * @return 指定状态的所有选课记录列表
     */
    public List<Enrollment> getEnrollmentsByStatus(String status) {
        if (!StringUtils.hasText(status)) {
            throw new IllegalArgumentException("状态不能为空");
        }
        try {
            EnrollmentStatus statusEnum = EnrollmentStatus.valueOf(status.toUpperCase());
            return enrollmentRepository.findByStatus(statusEnum);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("无效的选课状态: " + status);
        }
    }

    /**
     * 根据状态枚举获取选课记录
     * @param status 选课状态枚举
     * @return 指定状态的所有选课记录列表
     */
    public List<Enrollment> getEnrollmentsByStatus(EnrollmentStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("状态不能为空");
        }
        return enrollmentRepository.findByStatus(status);
    }

    /**
     * 获取课程的选课人数
     * @param courseId 课程ID
     * @return 该课程的选课人数
     */
    public long getEnrollmentCountByCourse(String courseId) {
        if (!StringUtils.hasText(courseId)) {
            throw new IllegalArgumentException("课程ID不能为空");
        }
        return enrollmentRepository.countByCourseIdAndStatusNot(courseId, EnrollmentStatus.WITHDRAWN);
    }

    /**
     * 获取学生的选课数量
     * @param studentId 学生ID
     * @return 该学生的选课数量
     */
    public long getEnrollmentCountByStudent(String studentId) {
        if (!StringUtils.hasText(studentId)) {
            throw new IllegalArgumentException("学生ID不能为空");
        }
        return enrollmentRepository.countByStudentIdAndStatusNot(studentId, EnrollmentStatus.WITHDRAWN);
    }

    /**
     * 更新选课状态
     * @param id 选课记录ID
     * @param status 新状态字符串
     * @return 更新后的选课记录Optional
     */
    @Transactional
    public Optional<Enrollment> updateEnrollmentStatus(UUID id, String status) {
        if (!StringUtils.hasText(status)) {
            throw new IllegalArgumentException("状态不能为空");
        }
        try {
            EnrollmentStatus statusEnum = EnrollmentStatus.valueOf(status.toUpperCase());
            return updateEnrollmentStatus(id, statusEnum);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("无效的选课状态: " + status);
        }
    }

    /**
     * 更新选课状态（枚举版本）
     * @param id 选课记录ID
     * @param status 新状态枚举
     * @return 更新后的选课记录Optional
     */
    @Transactional
    public Optional<Enrollment> updateEnrollmentStatus(UUID id, EnrollmentStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("状态不能为空");
        }

        Optional<Enrollment> enrollment = enrollmentRepository.findById(id);
        if (enrollment.isPresent()) {
            Enrollment enroll = enrollment.get();
            enroll.setStatus(status);
            return Optional.of(enrollmentRepository.save(enroll));
        }
        return Optional.empty();
    }

    /**
     * 更新学生成绩
     * @param id 选课记录ID
     * @param grade 成绩
     * @return 更新后的选课记录Optional
     */
    @Transactional
    public Optional<Enrollment> updateGrade(UUID id, Double grade) {
        if (grade == null) {
            throw new IllegalArgumentException("成绩不能为空");
        }
        if (grade < 0.0 || grade > 100.0) {
            throw new IllegalArgumentException("成绩必须在0-100之间");
        }

        Optional<Enrollment> enrollment = enrollmentRepository.findById(id);
        if (enrollment.isPresent()) {
            Enrollment enroll = enrollment.get();
            // 只有在特定状态下才能更新成绩
            if (canUpdateGrade(enroll)) {
                enroll.setGrade(grade);
                return Optional.of(enrollmentRepository.save(enroll));
            } else {
                throw new IllegalArgumentException("当前无法更新成绩，选课状态为: " + enroll.getStatus());
            }
        }

        return Optional.empty();
    }

    /**
     * 批量更新课程成绩
     * @param courseId 课程ID
     * @param grades 学生ID到成绩的映射
     * @return 更新成功的数量
     */
    @Transactional
    public int updateGradesForCourse(String courseId, Map<String, Double> grades) {
        if (!StringUtils.hasText(courseId)) {
            throw new IllegalArgumentException("课程ID不能为空");
        }
        if (grades == null || grades.isEmpty()) {
            throw new IllegalArgumentException("成绩数据不能为空");
        }

        // 验证所有成绩值
        for (Double grade : grades.values()) {
            if (grade == null || grade < 0.0 || grade > 100.0) {
                throw new IllegalArgumentException("成绩必须在0-100之间");
            }
        }

        // 获取该课程的所有选课记录
        List<Enrollment> enrollments = enrollmentRepository.findByCourseId(courseId);
        int updatedCount = 0;

        for (Enrollment enrollment : enrollments) {
            String studentId = enrollment.getStudentId();
            if (grades.containsKey(studentId) && canUpdateGrade(enrollment)) {
                enrollment.setGrade(grades.get(studentId));
                enrollmentRepository.save(enrollment);
                updatedCount++;
            }
        }

        return updatedCount;
    }

    /**
     * 删除选课记录
     * @param id 选课记录ID
     * @return 如果删除成功返回true，否则返回false
     */
    @Transactional
    public boolean deleteEnrollment(UUID id) {
        if (enrollmentRepository.existsById(id)) {
            enrollmentRepository.deleteById(id);
            return true;
        }
        return false;
    }

    /**
     * 检查学生是否已选某课程
     * @param courseId 课程ID
     * @param studentId 学生ID
     * @return 如果已选课返回true
     */
    public boolean isStudentEnrolled(String courseId, String studentId) {
        if (!StringUtils.hasText(courseId) || !StringUtils.hasText(studentId)) {
            return false;
        }
        return enrollmentRepository.existsByCourseIdAndStudentIdAndStatusNot(courseId, studentId, EnrollmentStatus.WITHDRAWN);
    }

    /**
     * 获取学生的课程成绩
     * @param studentId 学生ID
     * @param courseId 课程ID
     * @return 成绩Optional
     */
    public Optional<Double> getStudentGrade(String studentId, String courseId) {
        Optional<Enrollment> enrollment = enrollmentRepository.findByCourseIdAndStudentId(courseId, studentId);
        return enrollment.map(Enrollment::getGrade);
    }

    /**
     * 获取学生的平均成绩
     * @param studentId 学生ID
     * @return 平均成绩，如果没有成绩返回空Optional
     */
    public Optional<Double> getStudentAverageGrade(String studentId) {
        List<Enrollment> enrollments = enrollmentRepository.findByStudentId(studentId);

        List<Double> grades = enrollments.stream()
                .filter(enrollment -> enrollment.getGrade() != null &&
                        EnrollmentStatus.COMPLETED.equals(enrollment.getStatus()))
                .map(Enrollment::getGrade)
                .toList();

        if (grades.isEmpty()) {
            return Optional.empty();
        }

        double average = grades.stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);

        return Optional.of(average);
    }

    /**
     * 验证课程和学生ID
     */
    private void validateCourseAndStudentIds(String courseId, String studentId) {
        if (!StringUtils.hasText(courseId)) {
            throw new IllegalArgumentException("课程ID不能为空");
        }
        if (!StringUtils.hasText(studentId)) {
            throw new IllegalArgumentException("学生ID不能为空");
        }
    }

    /**
     * 检查学生是否存在
     */
    private boolean studentExists(String studentId) {
        // 这里假设studentId是UUID字符串，需要转换为UUID查询
        try {
            UUID studentUUID = UUID.fromString(studentId);
            return studentService.studentExists(studentUUID);
        } catch (IllegalArgumentException e) {
            // 如果studentId不是UUID格式，可能需要其他查询方式
            return studentService.getStudentByStudentId(studentId).isPresent();
        }
    }



    /**
     * 检查是否可以退课
     */
    private boolean canWithdrawCourse(Enrollment enrollment) {
        // 这里可以添加更多的业务规则
        // 例如：课程是否已开始、是否有成绩等
        return EnrollmentStatus.ENROLLED.equals(enrollment.getStatus()) && enrollment.getGrade() == null;
    }

    /**
     * 检查是否可以更新成绩
     */
    private boolean canUpdateGrade(Enrollment enrollment) {
        // 只有在课程进行中或已完成的状态下才能更新成绩
        return EnrollmentStatus.ENROLLED.equals(enrollment.getStatus()) ||
                EnrollmentStatus.COMPLETED.equals(enrollment.getStatus());
    }

    /**
     * 获取选课记录总数
     * @return 选课记录数量
     */
    public long getEnrollmentCount() {
        return enrollmentRepository.count();
    }

    /**
     * 解析UUID，处理字符串格式的ID
     */
    private UUID parseUUID(String id) {
        try {
            return UUID.fromString(id);
        } catch (IllegalArgumentException e) {
            // 如果不是UUID格式，可能需要其他处理方式
            throw new IllegalArgumentException("无效的ID格式: " + id);
        }
    }


    /**
     * 学生选课（完善版）
     */
    @Transactional
    public Enrollment enrollCourse(String courseId, String studentId) {
        // 验证输入参数
        validateCourseAndStudentIds(courseId, studentId);

        // 检查学生是否存在
        if (!studentExists(studentId)) {
            throw new IllegalArgumentException("学生不存在，ID: " + studentId);
        }

        // 1. 调用课程目录服务验证课程是否存在
        String courseUrl = catalogServiceUrl + "/api/courses/" + courseId;
        Map<String, Object> courseResponse;
        try {
            courseResponse = restTemplate.getForObject(courseUrl, Map.class);
        } catch (HttpClientErrorException.NotFound e) {
            throw new IllegalArgumentException("课程不存在，ID: " + courseId);
        } catch (HttpClientErrorException e) {
            throw new RuntimeException("调用课程服务失败，状态码: " + e.getStatusCode() + ", 错误信息: " + e.getResponseBodyAsString());
        } catch (Exception e) {
            throw new RuntimeException("调用课程服务失败: " + e.getMessage(), e);
        }

        // 2. 从响应中提取课程信息
        if (courseResponse == null || courseResponse.get("data") == null) {
            throw new RuntimeException("课程服务返回无效响应");
        }
        Map<String, Object> courseData = (Map<String, Object>) courseResponse.get("data");
        Integer capacity = (Integer) courseData.get("capacity");
        Integer enrolled = (Integer) courseData.get("enrolled");

        // 3. 检查课程容量
        if (capacity == null || enrolled == null) {
            throw new RuntimeException("课程信息不完整，无法选课");
        }
        if (enrolled >= capacity) {
            throw new IllegalArgumentException("课程容量已满，无法选课");
        }

        // 4. 检查重复选课
        if (enrollmentRepository.existsByCourseIdAndStudentIdAndStatusNot(courseId, studentId, EnrollmentStatus.WITHDRAWN)) {
            throw new IllegalArgumentException("学生已选该课程，无法重复选课");
        }

        // 5. 创建选课记录
        Enrollment enrollment = new Enrollment(courseId, studentId);
        Enrollment savedEnrollment = enrollmentRepository.save(enrollment);

        // 6. 更新课程的已选人数
        updateCourseEnrolledCount(courseId, enrolled + 1);

        return savedEnrollment;
    }

    /**
     * 更新课程的已选人数
     */
    private void updateCourseEnrolledCount(String courseId, int newCount) {
        String updateUrl = catalogServiceUrl + "/api/courses/" + courseId;
        Map<String, Object> updateData = new HashMap<>();
        Map<String, Object> courseData = new HashMap<>();
        courseData.put("enrolled", newCount);
        updateData.put("data", courseData);
        
        try {
            restTemplate.put(updateUrl, updateData);
        } catch (Exception e) {
            // 记录日志但不影响主流程
            System.err.println("Failed to update course enrolled count: " + e.getMessage());
        }
    }

    /**
     * 学生退课（完善版）
     */
    @Transactional
    public boolean withdrawCourse(String courseId, String studentId) {
        // 验证输入参数
        validateCourseAndStudentIds(courseId, studentId);

        Optional<Enrollment> enrollment = enrollmentRepository.findByCourseIdAndStudentId(courseId, studentId);

        if (enrollment.isPresent()) {
            Enrollment enroll = enrollment.get();
            // 检查是否可以退课
            if (canWithdrawCourse(enroll)) {
                enroll.setStatus(EnrollmentStatus.WITHDRAWN);
                enrollmentRepository.save(enroll);

                // 调用课程目录服务获取当前课程信息
                String courseUrl = catalogServiceUrl + "/api/courses/" + courseId;
                Map<String, Object> courseResponse;
                try {
                    courseResponse = restTemplate.getForObject(courseUrl, Map.class);
                    Map<String, Object> courseData = (Map<String, Object>) courseResponse.get("data");
                    Integer enrolled = (Integer) courseData.get("enrolled");
                    
                    // 减少课程选课人数
                    updateCourseEnrolledCount(courseId, enrolled - 1);
                } catch (Exception e) {
                    // 记录日志但不影响主流程
                    System.err.println("Failed to update course enrolled count during withdraw: " + e.getMessage());
                }

                return true;
            } else {
                throw new IllegalArgumentException("当前无法退课，可能课程已结束或已评分");
            }
        }

        return false;
    }

}
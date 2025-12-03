package com.zjgsu.ms.hxy.enrollment.service;

import com.zjgsu.ms.hxy.enrollment.model.Enrollment;
import com.zjgsu.ms.hxy.enrollment.model.EnrollmentStatus;
import com.zjgsu.ms.hxy.enrollment.model.Student;
import com.zjgsu.ms.hxy.enrollment.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * StudentService 类
 * 实现学生相关的业务逻辑，包括学生注册、信息更新、唯一性验证等业务规则
 *
 * @author System
 * @version 1.0
 * @since 2024
 */
@Service
public class StudentService {

    private final StudentRepository studentRepository;

    @Autowired
    private EnrollmentService enrollmentService;

    // 邮箱正则表达式
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");

    public StudentService(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    /**
     * 获取所有学生
     * @return 学生列表
     */
    public List<Student> getAllStudents() {
        return studentRepository.findAll();
    }

    /**
     * 根据ID获取学生
     * @param id 学生ID
     * @return 包含学生的Optional
     */
    public Optional<Student> getStudentById(UUID id) {
        return studentRepository.findById(id);
    }

    /**
     * 根据学号获取学生
     * @param studentId 学号
     * @return 包含学生的Optional
     */
    public Optional<Student> getStudentByStudentId(String studentId) {
        if (!StringUtils.hasText(studentId)) {
            throw new IllegalArgumentException("学号不能为空");
        }
        return studentRepository.findByStudentId(studentId);
    }

    /**
     * 根据邮箱获取学生
     * @param email 邮箱地址
     * @return 包含学生的Optional
     */
    public Optional<Student> getStudentByEmail(String email) {
        if (!StringUtils.hasText(email)) {
            throw new IllegalArgumentException("邮箱地址不能为空");
        }
        return studentRepository.findByEmail(email);
    }

    /**
     * 创建新学生
     * @param student 学生信息
     * @return 创建后的学生
     * @throws IllegalArgumentException 如果学号或邮箱已存在，或数据验证失败
     */
    @Transactional
    public Student createStudent(Student student) {
        // 验证学生数据
        validateStudent(student);

        // 检查学号是否已存在
        if (studentRepository.existsByStudentId(student.getStudentId())) {
            throw new IllegalArgumentException("学号已存在: " + student.getStudentId());
        }

        // 检查邮箱是否已存在
        if (studentRepository.existsByEmail(student.getEmail())) {
            throw new IllegalArgumentException("邮箱地址已存在: " + student.getEmail());
        }

        // 设置ID为null，确保创建新学生
        student.setId(null);

        return studentRepository.save(student);
    }

    /**
     * 更新学生信息
     * @param id 学生ID
     * @param student 更新后的学生信息
     * @return 更新后的学生Optional
     * @throws IllegalArgumentException 如果数据验证失败或学生不存在
     */
    @Transactional
    public Optional<Student> updateStudent(UUID id, Student student) {
        // 验证学生是否存在
        if (!studentRepository.existsById(id)) {
            throw new IllegalArgumentException("学生不存在，ID: " + id);
        }

        // 验证学生数据
        validateStudent(student);

        // 检查学号是否被其他学生使用
        Optional<Student> existingStudentWithSameStudentId = studentRepository.findByStudentId(student.getStudentId());
        if (existingStudentWithSameStudentId.isPresent() &&
                !existingStudentWithSameStudentId.get().getId().equals(id)) {
            throw new IllegalArgumentException("学号已被其他学生使用: " + student.getStudentId());
        }

        // 检查邮箱是否被其他学生使用
        Optional<Student> existingStudentWithSameEmail = studentRepository.findByEmail(student.getEmail());
        if (existingStudentWithSameEmail.isPresent() &&
                !existingStudentWithSameEmail.get().getId().equals(id)) {
            throw new IllegalArgumentException("邮箱地址已被其他学生使用: " + student.getEmail());
        }

        student.setId(id);
        return Optional.of(studentRepository.save(student));
    }

    /**
     * 删除学生（检查选课记录）
     * @param id 学生ID
     * @throws IllegalArgumentException 如果学生存在选课记录或学生不存在
     */
    @Transactional
    public void deleteStudent(UUID id) {
        if (!studentRepository.existsById(id)) {
//            throw new ResourceNotFoundException("学生不存在，ID: " + id);
        }

        if (hasActiveEnrollments(id)) {
            // todo
//            throw new BusinessException("无法删除：该学生存在选课记录");
        }

        // Spring Data JPA的deleteById方法返回void
        studentRepository.deleteById(id);
    }

    /**
     * 根据学号删除学生（检查选课记录）
     * @param studentId 学号
     * @throws IllegalArgumentException 如果学生存在选课记录或学生不存在
     */
    public void deleteStudentByStudentId(String studentId) {
        if (!StringUtils.hasText(studentId)) {
            throw new IllegalArgumentException("学号不能为空");
        }

        // 查找学生
        Optional<Student> student = studentRepository.findByStudentId(studentId);
        if (student.isEmpty()) {
            throw new IllegalArgumentException("学生不存在，学号: " + studentId);
        }

        // 检查是否有活跃的选课记录
        if (hasActiveEnrollments(student.get().getId())) {
            throw new IllegalArgumentException("无法删除：该学生存在选课记录");
        }

        // 执行删除
        studentRepository.delete(student.get());
    }

    /**
     * 检查学生是否存在
     * @param id 学生ID
     * @return 如果学生存在返回true
     */
    public boolean studentExists(UUID id) {
        return studentRepository.existsById(id);
    }

    /**
     * 检查学号是否存在
     * @param studentId 学号
     * @return 如果学号存在返回true
     */
    public boolean studentIdExists(String studentId) {
        if (!StringUtils.hasText(studentId)) {
            throw new IllegalArgumentException("学号不能为空");
        }
        return studentRepository.existsByStudentId(studentId);
    }

    /**
     * 检查邮箱是否存在
     * @param email 邮箱地址
     * @return 如果邮箱存在返回true
     */
    public boolean emailExists(String email) {
        if (!StringUtils.hasText(email)) {
            throw new IllegalArgumentException("邮箱地址不能为空");
        }
        return studentRepository.existsByEmail(email);
    }

    /**
     * 获取学生总数
     * @return 学生数量
     */
    public long getStudentCount() {
        return studentRepository.count();
    }

    /**
     * 根据专业查找学生
     * @param major 专业名称
     * @return 该专业的学生列表
     */
    public List<Student> getStudentsByMajor(String major) {
        if (!StringUtils.hasText(major)) {
            throw new IllegalArgumentException("专业名称不能为空");
        }
        return studentRepository.findByMajorIgnoreCase(major);
    }

    /**
     * 根据入学年份查找学生
     * @param grade 入学年份
     * @return 该年级的学生列表
     */
    public List<Student> getStudentsByGrade(Integer grade) {
        if (grade == null) {
            throw new IllegalArgumentException("入学年份不能为空");
        }
        if (grade < 2000 || grade > 2100) {
            throw new IllegalArgumentException("入学年份必须在2000-2100之间");
        }
        return studentRepository.findByGrade(grade);
    }

    /**
     * 根据姓名关键词搜索学生
     * @param keyword 关键词
     * @return 匹配的学生列表
     */
    public List<Student> searchStudentsByName(String keyword) {
        if (!StringUtils.hasText(keyword)) {
            throw new IllegalArgumentException("搜索关键词不能为空");
        }
        return studentRepository.findByNameContainingIgnoreCase(keyword);
    }

    /**
     * 验证学生数据的完整性
     * @param student 学生对象
     * @throws IllegalArgumentException 如果数据验证失败
     */
    private void validateStudent(Student student) {
        if (student == null) {
            throw new IllegalArgumentException("学生不能为空");
        }

        if (!StringUtils.hasText(student.getStudentId())) {
            throw new IllegalArgumentException("学号不能为空");
        }

        if (!StringUtils.hasText(student.getName())) {
            throw new IllegalArgumentException("学生姓名不能为空");
        }

        if (!StringUtils.hasText(student.getMajor())) {
            throw new IllegalArgumentException("专业名称不能为空");
        }

        if (student.getGrade() == null) {
            throw new IllegalArgumentException("入学年份不能为空");
        }

        if (student.getGrade() < 2000 || student.getGrade() > 2100) {
            throw new IllegalArgumentException("入学年份必须在2000-2100之间");
        }

        if (!StringUtils.hasText(student.getEmail())) {
            throw new IllegalArgumentException("邮箱地址不能为空");
        }

        // 验证邮箱格式
        if (!isValidEmail(student.getEmail())) {
            throw new IllegalArgumentException("邮箱格式不正确: " + student.getEmail());
        }
    }

    /**
     * 验证邮箱格式
     * @param email 邮箱地址
     * @return 如果邮箱格式正确返回true
     */
    private boolean isValidEmail(String email) {
        return EMAIL_PATTERN.matcher(email).matches();
    }

    /**
     * 批量创建学生
     * @param students 学生列表
     * @return 创建后的学生列表
     * @throws IllegalArgumentException 如果任何学生数据验证失败
     */
    public List<Student> createStudents(List<Student> students) {
        if (students == null || students.isEmpty()) {
            throw new IllegalArgumentException("学生列表不能为空");
        }

        // 验证所有学生数据
        for (Student student : students) {
            validateStudent(student);

            // 检查学号唯一性
            if (studentRepository.existsByStudentId(student.getStudentId())) {
                throw new IllegalArgumentException("学号已存在: " + student.getStudentId());
            }

            // 检查邮箱唯一性
            if (studentRepository.existsByEmail(student.getEmail())) {
                throw new IllegalArgumentException("邮箱地址已存在: " + student.getEmail());
            }

            student.setId(null);
        }

        return studentRepository.saveAll(students);
    }

    /**
     * 获取学生的基本信息（不包含敏感信息）
     * @param id 学生ID
     * @return 包含学生基本信息的Optional
     */
    public Optional<Student> getStudentBasicInfo(UUID id) {
        return studentRepository.findById(id).map(student -> {
            // 这里可以返回一个只包含基本信息的DTO
            // 目前直接返回实体，实际项目中可以考虑使用DTO
            return student;
        });
    }

    /**
     * 检查学生是否有活跃的选课记录
     * @param studentId 学生ID
     * @return 如果有活跃选课记录返回true
     */
    private boolean hasActiveEnrollments(UUID studentId) {
        // 这里需要注入 EnrollmentService 或 EnrollmentRepository
        // 假设我们注入了 EnrollmentService
        List<Enrollment> enrollments = enrollmentService.getEnrollmentsByStudent(studentId.toString());

        // 活跃记录：状态为 ENROLLED 或 COMPLETED（未退课的）
        return enrollments.stream()
                .anyMatch(enrollment ->
                        EnrollmentStatus.ENROLLED.equals(enrollment.getStatus()) ||
                                EnrollmentStatus.COMPLETED.equals(enrollment.getStatus()));
    }

    //
    public boolean isInitialized() {
        return studentRepository.count() > 0;
    }
}
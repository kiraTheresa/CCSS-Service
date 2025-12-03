package com.zjgsu.ms.hxy.enrollment.controller;

import com.zjgsu.ms.hxy.enrollment.model.Student;
import com.zjgsu.ms.hxy.enrollment.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.HashMap;

/**
 * StudentController 类
 * 提供学生管理的 RESTful API 接口
 *
 * @author System
 * @version 1.0
 * @since 2024
 */
@RestController
@RequestMapping("/api/students")
public class StudentController {

    private final StudentService studentService;

    @Autowired
    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    /**
     * 查询所有学生
     * GET /api/students
     * @return 学生列表
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllStudents() {
        try {
            List<Student> students = studentService.getAllStudents();
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "Success");
            response.put("data", students);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("message", "获取学生列表失败: " + e.getMessage());
            response.put("data", null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 根据ID查询学生
     * GET /api/students/{id}
     * @param id 学生ID
     * @return 学生信息
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getStudentById(@PathVariable UUID id) {
        try {
            return studentService.getStudentById(id)
                    .map(student -> {
                        Map<String, Object> response = new HashMap<>();
                        response.put("code", 200);
                        response.put("message", "Success");
                        response.put("data", student);
                        return ResponseEntity.ok(response);
                    })
                    .orElseGet(() -> {
                        Map<String, Object> response = new HashMap<>();
                        response.put("code", 404);
                        response.put("message", "学生不存在，ID: " + id);
                        response.put("data", null);
                        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
                    });
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("message", "获取学生失败: " + e.getMessage());
            response.put("data", null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 创建学生
     * POST /api/students
     * Content-Type: application/json
     * @param student 学生信息
     * @return 创建后的学生
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createStudent(@RequestBody Student student) {
        try {
            Student createdStudent = studentService.createStudent(student);
            Map<String, Object> response = new HashMap<>();
            response.put("code", 201);
            response.put("message", "学生创建成功");
            response.put("data", createdStudent);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 400);
            response.put("message", e.getMessage());
            response.put("data", null);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("message", "创建学生失败: " + e.getMessage());
            response.put("data", null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 更新学生信息
     * PUT /api/students/{id}
     * Content-Type: application/json
     * @param id 学生ID
     * @param student 更新后的学生信息
     * @return 更新后的学生
     */
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateStudent(@PathVariable UUID id, @RequestBody Student student) {
        try {
            return studentService.updateStudent(id, student)
                    .map(updatedStudent -> {
                        Map<String, Object> response = new HashMap<>();
                        response.put("code", 200);
                        response.put("message", "学生信息更新成功");
                        response.put("data", updatedStudent);
                        return ResponseEntity.ok(response);
                    })
                    .orElseGet(() -> {
                        Map<String, Object> response = new HashMap<>();
                        response.put("code", 404);
                        response.put("message", "学生不存在，ID: " + id);
                        response.put("data", null);
                        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
                    });
        } catch (IllegalArgumentException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 400);
            response.put("message", e.getMessage());
            response.put("data", null);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("message", "更新学生信息失败: " + e.getMessage());
            response.put("data", null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 删除学生
     * DELETE /api/students/{id}
     * @param id 学生ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteStudent(@PathVariable UUID id) {
        try {
            studentService.deleteStudent(id);
            Map<String, Object> response = new HashMap<>();
            response.put("code", 204);
            response.put("message", "学生删除成功");
            response.put("data", null);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body(response);
        } catch (IllegalArgumentException e) {
            // 学生不存在或存在选课记录
            Map<String, Object> response = new HashMap<>();
            if (e.getMessage().contains("学生不存在")) {
                response.put("code", 404);
            } else if (e.getMessage().contains("选课记录")) {
                response.put("code", 400); // 或 409 Conflict
            } else {
                response.put("code", 400);
            }
            response.put("message", e.getMessage());
            response.put("data", null);
            return ResponseEntity.status(
                    e.getMessage().contains("学生不存在") ? HttpStatus.NOT_FOUND : HttpStatus.BAD_REQUEST
            ).body(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("message", "删除学生失败: " + e.getMessage());
            response.put("data", null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 根据学号查询学生
     * GET /api/students/student-id/{studentId}
     * @param studentId 学号
     * @return 学生信息
     */
    @GetMapping("/student-id/{studentId}")
    public ResponseEntity<Map<String, Object>> getStudentByStudentId(@PathVariable String studentId) {
        try {
            return studentService.getStudentByStudentId(studentId)
                    .map(student -> {
                        Map<String, Object> response = new HashMap<>();
                        response.put("code", 200);
                        response.put("message", "Success");
                        response.put("data", student);
                        return ResponseEntity.ok(response);
                    })
                    .orElseGet(() -> {
                        Map<String, Object> response = new HashMap<>();
                        response.put("code", 404);
                        response.put("message", "学生不存在，学号: " + studentId);
                        response.put("data", null);
                        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
                    });
        } catch (IllegalArgumentException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 400);
            response.put("message", e.getMessage());
            response.put("data", null);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("message", "获取学生失败: " + e.getMessage());
            response.put("data", null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 根据邮箱查询学生
     * GET /api/students/email/{email}
     * @param email 邮箱地址
     * @return 学生信息
     */
    @GetMapping("/email/{email}")
    public ResponseEntity<Map<String, Object>> getStudentByEmail(@PathVariable String email) {
        try {
            return studentService.getStudentByEmail(email)
                    .map(student -> {
                        Map<String, Object> response = new HashMap<>();
                        response.put("code", 200);
                        response.put("message", "Success");
                        response.put("data", student);
                        return ResponseEntity.ok(response);
                    })
                    .orElseGet(() -> {
                        Map<String, Object> response = new HashMap<>();
                        response.put("code", 404);
                        response.put("message", "学生不存在，邮箱: " + email);
                        response.put("data", null);
                        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
                    });
        } catch (IllegalArgumentException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 400);
            response.put("message", e.getMessage());
            response.put("data", null);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("message", "获取学生失败: " + e.getMessage());
            response.put("data", null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 根据专业查询学生
     * GET /api/students/major/{major}
     * @param major 专业名称
     * @return 学生列表
     */
    @GetMapping("/major/{major}")
    public ResponseEntity<Map<String, Object>> getStudentsByMajor(@PathVariable String major) {
        try {
            List<Student> students = studentService.getStudentsByMajor(major);
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "Success");
            response.put("data", students);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 400);
            response.put("message", e.getMessage());
            response.put("data", null);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("message", "获取学生失败: " + e.getMessage());
            response.put("data", null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 根据入学年份查询学生
     * GET /api/students/grade/{grade}
     * @param grade 入学年份
     * @return 学生列表
     */
    @GetMapping("/grade/{grade}")
    public ResponseEntity<Map<String, Object>> getStudentsByGrade(@PathVariable Integer grade) {
        try {
            List<Student> students = studentService.getStudentsByGrade(grade);
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "Success");
            response.put("data", students);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 400);
            response.put("message", e.getMessage());
            response.put("data", null);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("message", "获取学生失败: " + e.getMessage());
            response.put("data", null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 根据姓名搜索学生
     * GET /api/students/search
     * @param keyword 搜索关键词
     * @return 匹配的学生列表
     */
    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> searchStudentsByName(@RequestParam String keyword) {
        try {
            List<Student> students = studentService.searchStudentsByName(keyword);
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "Success");
            response.put("data", students);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 400);
            response.put("message", e.getMessage());
            response.put("data", null);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("message", "搜索学生失败: " + e.getMessage());
            response.put("data", null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 检查学号是否存在
     * GET /api/students/check-student-id/{studentId}
     * @param studentId 学号
     * @return 检查结果
     */
    @GetMapping("/check-student-id/{studentId}")
    public ResponseEntity<Map<String, Object>> checkStudentIdExists(@PathVariable String studentId) {
        try {
            boolean exists = studentService.studentIdExists(studentId);
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "Success");
            response.put("data", Map.of("exists", exists));
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 400);
            response.put("message", e.getMessage());
            response.put("data", null);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("message", "检查学号失败: " + e.getMessage());
            response.put("data", null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 检查邮箱是否存在
     * GET /api/students/check-email/{email}
     * @param email 邮箱地址
     * @return 检查结果
     */
    @GetMapping("/check-email/{email}")
    public ResponseEntity<Map<String, Object>> checkEmailExists(@PathVariable String email) {
        try {
            boolean exists = studentService.emailExists(email);
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "Success");
            response.put("data", Map.of("exists", exists));
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 400);
            response.put("message", e.getMessage());
            response.put("data", null);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("message", "检查邮箱失败: " + e.getMessage());
            response.put("data", null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
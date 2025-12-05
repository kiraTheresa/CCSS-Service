package com.zjgsu.ms.hxy.enrollment.controller;

import com.zjgsu.ms.hxy.enrollment.model.Enrollment;
import com.zjgsu.ms.hxy.enrollment.service.EnrollmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.HashMap;

/**
 * EnrollmentController 类
 * 提供选课管理的 RESTful API 接口
 *
 * @author System
 * @version 1.0
 * @since 2024
 */
@RestController
@RequestMapping("/api/enrollments")
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    @Autowired
    public EnrollmentController(EnrollmentService enrollmentService) {
        this.enrollmentService = enrollmentService;
    }

    /**
     * 查询所有选课记录
     * GET /api/enrollments
     * @return 选课记录列表
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllEnrollments() {
        try {
            List<Enrollment> enrollments = enrollmentService.getAllEnrollments();
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "Success");
            response.put("data", enrollments);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("message", "获取选课记录失败: " + e.getMessage());
            response.put("data", null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 根据ID查询选课记录
     * GET /api/enrollments/{id}
     * @param id 选课记录ID
     * @return 选课记录信息
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getEnrollmentById(@PathVariable UUID id) {
        try {
            return enrollmentService.getEnrollmentById(id)
                    .map(enrollment -> {
                        Map<String, Object> response = new HashMap<>();
                        response.put("code", 200);
                        response.put("message", "Success");
                        response.put("data", enrollment);
                        return ResponseEntity.ok(response);
                    })
                    .orElseGet(() -> {
                        Map<String, Object> response = new HashMap<>();
                        response.put("code", 404);
                        response.put("message", "选课记录不存在，ID: " + id);
                        response.put("data", null);
                        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
                    });
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("message", "获取选课记录失败: " + e.getMessage());
            response.put("data", null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 学生选课
     * POST /api/enrollments
     * Content-Type: application/json
     * @param request 选课请求
     * @return 创建后的选课记录
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> enrollCourse(@RequestBody Map<String, String> request) {
        try {
            String courseId = request.get("courseId");
            String studentId = request.get("studentId");

            if (courseId == null || studentId == null) {
                Map<String, Object> response = new HashMap<>();
                response.put("code", 400);
                response.put("message", "courseId 和 studentId 不能为空");
                response.put("data", null);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            Enrollment enrollment = enrollmentService.enrollCourse(courseId, studentId);
            Map<String, Object> response = new HashMap<>();
            response.put("code", 201);
            response.put("message", "选课成功");
            response.put("data", enrollment);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (IllegalArgumentException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 400);
            response.put("message", e.getMessage());
            response.put("data", null);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (com.zjgsu.ms.hxy.enrollment.exception.CourseNotFoundException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 404);
            response.put("message", e.getMessage());
            response.put("data", null);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("message", "选课失败: " + e.getMessage());
            response.put("data", null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 学生退课
     * DELETE /api/enrollments/{id}
     * @param id 选课记录ID
     * @return 退课结果
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> withdrawEnrollment(@PathVariable UUID id) {
        try {
            boolean deleted = enrollmentService.deleteEnrollment(id);
            if (deleted) {
                Map<String, Object> response = new HashMap<>();
                response.put("code", 204);
                response.put("message", "退课成功");
                response.put("data", null);
                return ResponseEntity.status(HttpStatus.NO_CONTENT).body(response);
            } else {
                Map<String, Object> response = new HashMap<>();
                response.put("code", 404);
                response.put("message", "选课记录不存在，ID: " + id);
                response.put("data", null);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("message", "退课失败: " + e.getMessage());
            response.put("data", null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 根据课程和学生退课
     * DELETE /api/enrollments/course/{courseId}/student/{studentId}
     * @param courseId 课程ID
     * @param studentId 学生ID
     * @return 退课结果
     */
    @DeleteMapping("/course/{courseId}/student/{studentId}")
    public ResponseEntity<Map<String, Object>> withdrawCourseByCourseAndStudent(
            @PathVariable String courseId,
            @PathVariable String studentId) {
        try {
            boolean withdrawn = enrollmentService.withdrawCourse(courseId, studentId);
            if (withdrawn) {
                Map<String, Object> response = new HashMap<>();
                response.put("code", 204);
                response.put("message", "退课成功");
                response.put("data", null);
                return ResponseEntity.status(HttpStatus.NO_CONTENT).body(response);
            } else {
                Map<String, Object> response = new HashMap<>();
                response.put("code", 404);
                response.put("message", "选课记录不存在");
                response.put("data", null);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
        } catch (IllegalArgumentException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 400);
            response.put("message", e.getMessage());
            response.put("data", null);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (com.zjgsu.ms.hxy.enrollment.exception.CourseNotFoundException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 404);
            response.put("message", e.getMessage());
            response.put("data", null);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("message", "退课失败: " + e.getMessage());
            response.put("data", null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 按课程查询选课记录
     * GET /api/enrollments/course/{courseId}
     * @param courseId 课程ID
     * @return 该课程的选课记录列表
     */
    @GetMapping("/course/{courseId}")
    public ResponseEntity<Map<String, Object>> getEnrollmentsByCourse(@PathVariable String courseId) {
        try {
            List<Enrollment> enrollments = enrollmentService.getEnrollmentsByCourse(courseId);
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "Success");
            response.put("data", enrollments);
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
            response.put("message", "获取课程选课记录失败: " + e.getMessage());
            response.put("data", null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 按学生查询选课记录
     * GET /api/enrollments/student/{studentId}
     * @param studentId 学生ID
     * @return 该学生的选课记录列表
     */
    @GetMapping("/student/{studentId}")
    public ResponseEntity<Map<String, Object>> getEnrollmentsByStudent(@PathVariable String studentId) {
        try {
            List<Enrollment> enrollments = enrollmentService.getEnrollmentsByStudent(studentId);
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "Success");
            response.put("data", enrollments);
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
            response.put("message", "获取学生选课记录失败: " + e.getMessage());
            response.put("data", null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 按状态查询选课记录
     * GET /api/enrollments/status/{status}
     * @param status 选课状态
     * @return 指定状态的选课记录列表
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<Map<String, Object>> getEnrollmentsByStatus(@PathVariable String status) {
        try {
            List<Enrollment> enrollments = enrollmentService.getEnrollmentsByStatus(status);
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "Success");
            response.put("data", enrollments);
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
            response.put("message", "获取状态选课记录失败: " + e.getMessage());
            response.put("data", null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 更新选课状态
     * PUT /api/enrollments/{id}/status
     * @param id 选课记录ID
     * @param request 状态更新请求
     * @return 更新后的选课记录
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<Map<String, Object>> updateEnrollmentStatus(
            @PathVariable UUID id,
            @RequestBody Map<String, String> request) {
        try {
            String status = request.get("status");
            if (status == null) {
                Map<String, Object> response = new HashMap<>();
                response.put("code", 400);
                response.put("message", "status 不能为空");
                response.put("data", null);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            return enrollmentService.updateEnrollmentStatus(id, status)
                    .map(enrollment -> {
                        Map<String, Object> response = new HashMap<>();
                        response.put("code", 200);
                        response.put("message", "状态更新成功");
                        response.put("data", enrollment);
                        return ResponseEntity.ok(response);
                    })
                    .orElseGet(() -> {
                        Map<String, Object> response = new HashMap<>();
                        response.put("code", 404);
                        response.put("message", "选课记录不存在，ID: " + id);
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
            response.put("message", "更新状态失败: " + e.getMessage());
            response.put("data", null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 更新成绩
     * PUT /api/enrollments/{id}/grade
     * @param id 选课记录ID
     * @param request 成绩更新请求
     * @return 更新后的选课记录
     */
    @PutMapping("/{id}/grade")
    public ResponseEntity<Map<String, Object>> updateGrade(
            @PathVariable UUID id,
            @RequestBody Map<String, Object> request) {
        try {
            Object gradeObj = request.get("grade");
            if (gradeObj == null) {
                Map<String, Object> response = new HashMap<>();
                response.put("code", 400);
                response.put("message", "grade 不能为空");
                response.put("data", null);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            Double grade;
            if (gradeObj instanceof Number) {
                grade = ((Number) gradeObj).doubleValue();
            } else if (gradeObj instanceof String) {
                grade = Double.parseDouble((String) gradeObj);
            } else {
                throw new IllegalArgumentException("grade 必须是数字类型");
            }

            return enrollmentService.updateGrade(id, grade)
                    .map(enrollment -> {
                        Map<String, Object> response = new HashMap<>();
                        response.put("code", 200);
                        response.put("message", "成绩更新成功");
                        response.put("data", enrollment);
                        return ResponseEntity.ok(response);
                    })
                    .orElseGet(() -> {
                        Map<String, Object> response = new HashMap<>();
                        response.put("code", 404);
                        response.put("message", "选课记录不存在，ID: " + id);
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
            response.put("message", "更新成绩失败: " + e.getMessage());
            response.put("data", null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 获取课程选课人数
     * GET /api/enrollments/course/{courseId}/count
     * @param courseId 课程ID
     * @return 选课人数
     */
    @GetMapping("/course/{courseId}/count")
    public ResponseEntity<Map<String, Object>> getEnrollmentCountByCourse(@PathVariable String courseId) {
        try {
            long count = enrollmentService.getEnrollmentCountByCourse(courseId);
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "Success");
            response.put("data", Map.of("count", count));
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
            response.put("message", "获取选课人数失败: " + e.getMessage());
            response.put("data", null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 获取学生选课数量
     * GET /api/enrollments/student/{studentId}/count
     * @param studentId 学生ID
     * @return 选课数量
     */
    @GetMapping("/student/{studentId}/count")
    public ResponseEntity<Map<String, Object>> getEnrollmentCountByStudent(@PathVariable String studentId) {
        try {
            long count = enrollmentService.getEnrollmentCountByStudent(studentId);
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "Success");
            response.put("data", Map.of("count", count));
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
            response.put("message", "获取选课数量失败: " + e.getMessage());
            response.put("data", null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 检查学生是否已选某课程
     * GET /api/enrollments/check-enrollment
     * @param courseId 课程ID
     * @param studentId 学生ID
     * @return 检查结果
     */
    @GetMapping("/check-enrollment")
    public ResponseEntity<Map<String, Object>> checkStudentEnrolled(
            @RequestParam String courseId,
            @RequestParam String studentId) {
        try {
            boolean enrolled = enrollmentService.isStudentEnrolled(courseId, studentId);
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "Success");
            response.put("data", Map.of("enrolled", enrolled));
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("message", "检查选课状态失败: " + e.getMessage());
            response.put("data", null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
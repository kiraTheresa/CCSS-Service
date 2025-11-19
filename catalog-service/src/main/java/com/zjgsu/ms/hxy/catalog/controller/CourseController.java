package com.zjgsu.ms.hxy.catalog.controller;

import com.zjgsu.ms.hxy.catalog.model.Course;
import com.zjgsu.ms.hxy.catalog.service.CourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.HashMap;

/**
 * CourseController 类
 * 提供课程管理的 RESTful API 接口
 *
 * @author System
 * @version 1.0
 * @since 2024
 */
@RestController
@RequestMapping("/api/courses")
public class CourseController {

    private final CourseService courseService;

    @Autowired
    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    /**
     * 查询所有课程
     * GET /api/courses
     * @return 课程列表
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllCourses() {
        try {
            List<Course> courses = courseService.getAllCourses();
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "Success");
            response.put("data", courses);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("message", "获取课程列表失败: " + e.getMessage());
            response.put("data", null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 根据ID查询单个课程
     * GET /api/courses/{id}
     * @param id 课程ID
     * @return 课程信息
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getCourseById(@PathVariable UUID id) {
        try {
            return courseService.getCourseById(id)
                    .map(course -> {
                        Map<String, Object> response = new HashMap<>();
                        response.put("code", 200);
                        response.put("message", "Success");
                        response.put("data", course);
                        return ResponseEntity.ok(response);
                    })
                    .orElseGet(() -> {
                        Map<String, Object> response = new HashMap<>();
                        response.put("code", 404);
                        response.put("message", "课程不存在，ID: " + id);
                        response.put("data", null);
                        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
                    });
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("message", "获取课程失败: " + e.getMessage());
            response.put("data", null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 创建课程
     * POST /api/courses
     * Content-Type: application/json
     * @param course 课程信息
     * @return 创建后的课程
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createCourse(@RequestBody Course course) {
        try {
            Course createdCourse = courseService.createCourse(course);
            Map<String, Object> response = new HashMap<>();
            response.put("code", 201);
            response.put("message", "课程创建成功");
            response.put("data", createdCourse);
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
            response.put("message", "创建课程失败: " + e.getMessage());
            response.put("data", null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 更新课程
     * PUT /api/courses/{id}
     * Content-Type: application/json
     * @param id 课程ID
     * @param course 更新后的课程信息
     * @return 更新后的课程
     */
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateCourse(@PathVariable UUID id, @RequestBody Course course) {
        try {
            return courseService.updateCourse(id, course)
                    .map(updatedCourse -> {
                        Map<String, Object> response = new HashMap<>();
                        response.put("code", 200);
                        response.put("message", "课程更新成功");
                        response.put("data", updatedCourse);
                        return ResponseEntity.ok(response);
                    })
                    .orElseGet(() -> {
                        Map<String, Object> response = new HashMap<>();
                        response.put("code", 404);
                        response.put("message", "课程不存在，ID: " + id);
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
            response.put("message", "更新课程失败: " + e.getMessage());
            response.put("data", null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 删除课程
     * DELETE /api/courses/{id}
     * @param id 课程ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteCourse(@PathVariable UUID id) {
        try {
            boolean deleted = courseService.deleteCourse(id);
            if (deleted) {
                Map<String, Object> response = new HashMap<>();
                response.put("code", 204);
                response.put("message", "课程删除成功");
                response.put("data", null);
                return ResponseEntity.status(HttpStatus.NO_CONTENT).body(response);
            } else {
                Map<String, Object> response = new HashMap<>();
                response.put("code", 404);
                response.put("message", "课程不存在，ID: " + id);
                response.put("data", null);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("message", "删除课程失败: " + e.getMessage());
            response.put("data", null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 根据课程编号查询课程
     * GET /api/courses/code/{code}
     * @param code 课程编号
     * @return 课程信息
     */
    @GetMapping("/code/{code}")
    public ResponseEntity<Map<String, Object>> getCourseByCode(@PathVariable String code) {
        try {
            return courseService.getCourseByCode(code)
                    .map(course -> {
                        Map<String, Object> response = new HashMap<>();
                        response.put("code", 200);
                        response.put("message", "Success");
                        response.put("data", course);
                        return ResponseEntity.ok(response);
                    })
                    .orElseGet(() -> {
                        Map<String, Object> response = new HashMap<>();
                        response.put("code", 404);
                        response.put("message", "课程不存在，编号: " + code);
                        response.put("data", null);
                        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
                    });
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("message", "获取课程失败: " + e.getMessage());
            response.put("data", null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 根据教师ID查询课程
     * GET /api/courses/instructor/{instructorId}
     * @param instructorId 教师ID
     * @return 课程列表
     */
    @GetMapping("/instructor/{instructorId}")
    public ResponseEntity<Map<String, Object>> getCoursesByInstructor(@PathVariable String instructorId) {
        try {
            List<Course> courses = courseService.getCoursesByInstructor(instructorId);
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "Success");
            response.put("data", courses);
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
            response.put("message", "获取教师课程失败: " + e.getMessage());
            response.put("data", null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 根据标题搜索课程
     * GET /api/courses/search
     * @param keyword 搜索关键词
     * @return 匹配的课程列表
     */
    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> searchCoursesByTitle(@RequestParam String keyword) {
        try {
            List<Course> courses = courseService.searchCoursesByTitle(keyword);
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "Success");
            response.put("data", courses);
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
            response.put("message", "搜索课程失败: " + e.getMessage());
            response.put("data", null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
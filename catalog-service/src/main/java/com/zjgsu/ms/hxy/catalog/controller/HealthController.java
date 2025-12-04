package com.zjgsu.ms.hxy.catalog.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * HealthController 类
 * 提供服务健康检查的 RESTful API 接口
 *
 * @author System
 * @version 1.0
 * @since 2024
 */
@RestController
public class HealthController {

    /**
     * 基本健康检查
     * GET /health
     * @return 服务健康状态
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "catalog-service");
        response.put("timestamp", LocalDateTime.now().toString());
        return ResponseEntity.ok(response);
    }

    /**
     * 详细健康检查
     * GET /health/detailed
     * @return 详细的服务健康信息
     */
    @GetMapping("/health/detailed")
    public ResponseEntity<Map<String, Object>> detailedHealth() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "catalog-service");
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("version", "1.0.0");
        response.put("description", "课程目录服务，提供课程管理功能");
        response.put("endpoints", Map.of(
                "health", "/health",
                "detailed_health", "/health/detailed",
                "courses", "/api/courses"
        ));
        return ResponseEntity.ok(response);
    }
}
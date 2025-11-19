package com.zjgsu.ms.hxy.enrollment.model;

/**
 * 选课状态枚举
 * 
 * @author System
 * @version 1.1
 * @since 2024
 */
public enum EnrollmentStatus {
    /**
     * 已选课（活跃状态）
     */
    ENROLLED,
    
    /**
     * 已退课
     */
    WITHDRAWN,
    
    /**
     * 已完成（课程结束）
     */
    COMPLETED,
    
    /**
     * 不及格
     */
    FAILED
}


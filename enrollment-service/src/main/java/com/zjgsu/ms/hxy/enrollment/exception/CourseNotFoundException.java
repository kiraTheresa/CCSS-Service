package com.zjgsu.ms.hxy.enrollment.exception;

/**
 * CourseNotFoundException 类
 * 当课程不存在时抛出此异常
 *
 * @author System
 * @version 1.0
 * @since 2024
 */
public class CourseNotFoundException extends RuntimeException {

    public CourseNotFoundException(String message) {
        super(message);
    }

    public CourseNotFoundException(String courseId, String message) {
        super("课程不存在，ID: " + courseId + ", " + message);
    }

    public CourseNotFoundException(String courseId) {
        super("课程不存在，ID: " + courseId);
    }
}
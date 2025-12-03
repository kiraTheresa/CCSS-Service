package com.zjgsu.ms.hxy.enrollment.exception;

/**
 * 资源未找到异常
 * 当请求的资源不存在时抛出
 */
public class ResourceNotFoundException extends RuntimeException {
    
    public ResourceNotFoundException(String message) {
        super(message);
    }
    
    public ResourceNotFoundException(String resourceName, String resourceId) {
        super(resourceName + "不存在，ID: " + resourceId);
    }
}
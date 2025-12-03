package com.zjgsu.ms.hxy.enrollment.exception;

/**
 * 业务异常
 * 当业务逻辑验证失败时抛出
 */
public class BusinessException extends RuntimeException {
    
    public BusinessException(String message) {
        super(message);
    }
}
// GlobalExceptionHandler.java
package com.zjgsu.ms.hxy.catalog.exception;

import com.zjgsu.ms.hxy.catalog.common.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.HashMap;
import java.util.Map;

/**
 * 全局异常处理器
 * 统一处理应用中抛出的各种异常，返回规范的API响应格式
 *
 * @author System
 * @version 1.0
 * @since 2024
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理资源不存在异常
     * 当查询不存在的课程、学生时抛出
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<?>> handleResourceNotFound(ResourceNotFoundException ex) {
        ApiResponse<?> response = ApiResponse.notFound(ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    /**
     * 处理业务逻辑异常
     * 如：重复选课、课程容量已满、删除有选课记录的学生等
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<?>> handleBusinessException(BusinessException ex) {
        ApiResponse<?> response = ApiResponse.badRequest(ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * 处理数据验证异常
     */
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ApiResponse<?>> handleValidationException(ValidationException ex) {
        ApiResponse<?> response = ApiResponse.badRequest(ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * 处理参数校验失败异常（@Valid 注解触发的异常）
     * 自动捕获实体类字段验证错误
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<?>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        @SuppressWarnings("unchecked")
        ApiResponse<Map<String, String>> response = (ApiResponse<Map<String, String>>) ApiResponse.error(400, "参数验证失败");
        response.setData(errors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * 处理参数类型不匹配异常
     * 如：UUID格式错误、数字格式错误等
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<?>> handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String error = String.format("参数 '%s' 类型不匹配，期望类型: %s",
                ex.getName(), ex.getRequiredType().getSimpleName());
        ApiResponse<?> response = ApiResponse.badRequest(error);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
    /**
     * 处理IllegalArgumentException（非法参数异常）
     * 这是你目前在Service层大量使用的异常类型
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<?>> handleIllegalArgumentException(IllegalArgumentException ex) {
        // 根据异常消息内容判断是业务异常还是参数错误
        String message = ex.getMessage();
        HttpStatus status = HttpStatus.BAD_REQUEST;

        if (message != null) {
            if (message.contains("不存在")) {
                status = HttpStatus.NOT_FOUND;
            } else if (message.contains("选课记录") || message.contains("容量") || message.contains("重复")) {
                status = HttpStatus.BAD_REQUEST; // 业务规则违反
            }
        }

        ApiResponse<?> response = ApiResponse.error(status.value(), message);
        return ResponseEntity.status(status).body(response);
    }

    /**
     * 处理所有未捕获的异常（兜底处理）
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<?>> handleGlobalException(Exception ex) {
        // 生产环境中应该记录详细的错误日志
        ex.printStackTrace(); // 开发阶段打印堆栈跟踪

        String message = "服务器内部错误，请稍后重试";
        // 开发阶段可以返回详细错误信息
        // message = ex.getMessage();

        ApiResponse<?> response = ApiResponse.error(500, message);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
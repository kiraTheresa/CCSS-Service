// ApiResponse.java
package com.zjgsu.ms.hxy.catalog.common;

import java.time.LocalDateTime;

public class ApiResponse<T> {
    private int code;
    private String message;
    private T data;
    private LocalDateTime timestamp;

    // 成功响应的静态方法
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(200, "Success", data, LocalDateTime.now());
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(200, message, data, LocalDateTime.now());
    }

    public static ApiResponse<?> created(Object data) {
        return new ApiResponse<>(201, "Created", data, LocalDateTime.now());
    }

    public static ApiResponse<?> noContent() {
        return new ApiResponse<>(204, "No Content", null, LocalDateTime.now());
    }

    // 错误响应的静态方法
    public static ApiResponse<?> error(int code, String message) {
        return new ApiResponse<>(code, message, null, LocalDateTime.now());
    }

    public static ApiResponse<?> notFound(String message) {
        return new ApiResponse<>(404, message, null, LocalDateTime.now());
    }

    public static ApiResponse<?> badRequest(String message) {
        return new ApiResponse<>(400, message, null, LocalDateTime.now());
    }

    // 构造函数、getter、setter
    public ApiResponse(int code, String message, T data, LocalDateTime timestamp) {
        this.code = code;
        this.message = message;
        this.data = data;
        this.timestamp = timestamp;
    }

    // getters and setters
    public int getCode() { return code; }
    public void setCode(int code) { this.code = code; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public T getData() { return data; }
    public void setData(T data) { this.data = data; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}
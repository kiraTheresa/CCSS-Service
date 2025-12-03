# 解决 Enrollment Service 启动问题

## 1. 问题分析

### 1.1 循环依赖问题
- `EnrollmentService` 依赖 `StudentService`
- `StudentService` 依赖 `EnrollmentService`
- Spring 无法初始化这些 Bean，导致应用启动失败

### 1.2 缺少异常类
- 代码中引用了 `ResourceNotFoundException`，但该类不存在
- 代码中引用了 `BusinessException`，但该类不存在

## 2. 解决方案

### 2.1 创建异常类

1. 在 `enrollment-service` 中创建 `exception` 包
2. 创建 `ResourceNotFoundException` 类，用于处理资源不存在的情况
3. 创建 `BusinessException` 类，用于处理业务逻辑异常

### 2.2 解决循环依赖

使用 `@Lazy` 注解结合构造函数注入来解决循环依赖：
- 在 `StudentService` 的构造函数中，为 `EnrollmentService` 添加 `@Lazy` 注解
- 或在 `EnrollmentService` 的构造函数中，为 `StudentService` 添加 `@Lazy` 注解

### 2.3 更新代码中的异常引用

- 在 `StudentService.deleteStudent()` 方法中，使用正确的异常类
- 确保所有异常都被正确导入和使用

## 3. 实施步骤

1. 创建 `exception` 包和异常类
2. 修改 `StudentService` 的构造函数，添加 `@Lazy` 注解
3. 更新 `deleteStudent()` 方法，使用正确的异常类
4. 测试应用启动

## 4. 预期结果

- 应用能够成功启动，不再出现循环依赖错误
- `deleteStudent()` 方法能够正确抛出异常
- 所有异常都被正确处理和返回
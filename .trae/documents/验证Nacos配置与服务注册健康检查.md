# 修复Nacos服务注册与发现问题

## 1. 问题分析

通过代码检查，发现以下问题：

### 1.1 缺少服务发现注解

两个服务的主启动类都缺少`@EnableDiscoveryClient`注解，虽然从Spring Cloud 2020开始该注解可选，但在某些情况下（特别是使用Spring Cloud Alibaba时）可能需要显式添加。

### 1.2 硬编码URL问题

在`EnrollmentServiceApplication.java`的`enrollSafely`方法中，使用了硬编码URL `http://localhost:8081` 来调用catalog-service，而不是使用DiscoveryClient获取服务实例。

## 2. 修复步骤

### 2.1 添加@EnableDiscoveryClient注解

为两个服务的主启动类添加`@EnableDiscoveryClient`注解：

* **catalog-service**: 在`CatalogServiceApplication.java`中添加注解

* **enrollment-service**: 在`EnrollmentServiceApplication.java`中添加注解

### 2.2 修改硬编码URL

修改`EnrollmentServiceApplication.java`中的`enrollSafely`方法，使用DiscoveryClient获取catalog-service的实例：

1. 添加DiscoveryClient依赖注入
2. 在`initEnrollmentData`方法中注入DiscoveryClient
3. 在`enrollSafely`方法中使用DiscoveryClient获取服务实例
4. 替换硬编码URL为动态获取的服务URL

### 2.3 验证修复

#### 2.3.1 启动服务

```bash
docker-compose up -d nacos catalog-db enrollment-db catalog-service enrollment-service
```

#### 2.3.2 验证服务注册

* 通过Nacos控制台查看服务列表，确认两个服务已注册

* 通过API验证服务注册：

  ```bash
  curl -X GET "http://localhost:8848/nacos/v1/ns/instance/list?serviceName=catalog-service&namespaceId=dev"
  curl -X GET "http://localhost:8848/nacos/v1/ns/instance/list?serviceName=enrollment-service&namespaceId=dev"
  ```

#### 2.3.3 验证健康检查

* 访问服务的健康检查端点：

  ```bash
  curl http://localhost:8081/actuator/health
  curl http://localhost:8082/actuator/health
  ```

#### 2.3.4 验证服务间通信

* 检查服务日志，确认初始化数据时服务间通信正常

* 使用enrollment-service调用catalog-service验证服务发现功能

#### 2.3.5 运行测试脚本

```bash
chmod +x scripts/nacos-test.sh
./scripts/nacos-test.sh
```

## 3. 预期结果

* ✅ 两个服务成功注册到Nacos

* ✅ 健康检查状态正常

* ✅ 服务间通信正常，使用动态服务发现而非硬编码URL

* ✅ 测试脚本执行通过

## 4. 代码修改点

### 4.1 CatalogServiceApplication.java

```java
@SpringBootApplication
@EnableDiscoveryClient  // 添加此注解
public class CatalogServiceApplication {
    // ...
}
```

### 4.2 EnrollmentServiceApplication.java

```java
@SpringBootApplication
@EnableDiscoveryClient  // 添加此注解
public class EnrollmentServiceApplication {
    // ...
    
    // 修改initEnrollmentData方法，添加DiscoveryClient参数
    @Bean
    public CommandLineRunner initEnrollmentData(RestTemplate restTemplate, 
                                               StudentService studentService, 
                                               EnrollmentService enrollmentService, 
                                               DiscoveryClient discoveryClient) {
        return args -> {
            // ...
            enrollSafely("CS101", s1.getId().toString(), restTemplate, enrollmentService, discoveryClient);
            // ...
        };
    }
    
    // 修改enrollSafely方法，添加DiscoveryClient参数，使用动态URL
    private void enrollSafely(String courseCode, String studentId, 
                             RestTemplate restTemplate, 
                             EnrollmentService enrollmentService, 
                             DiscoveryClient discoveryClient) {
        try {
            // 使用DiscoveryClient获取catalog-service实例
            List<ServiceInstance> instances = discoveryClient.getInstances("catalog-service");
            if (instances == null || instances.isEmpty()) {
                System.err.println("No instances available for catalog-service");
                return;
            }
            String baseUrl = instances.get(0).getUri().toString();
            
            // 首先通过课程代码获取课程ID
            String courseUrl = baseUrl + "/api/courses/code/" + courseCode;
            // ...
        } catch (Exception e) {
            // ...
        }
    }
}
```

## 5. 故障排查

如果出现问题，按照以下步骤排查：

1. 检查服务日志：`docker logs catalog-service` 和 `docker logs enrollment-service`
2. 验证Nacos服务是否正常运行：`docker logs nacos`
3. 检查配置文件中的Nacos服务器地址、命名空间和分组是否正确
4. 确认所有服务在同一个Docker网络中
5. 检查健康检查端点是否可访问

## 6. 清理资源

测试完成后，清理资源：

```bash
docker-compose down
```

<br />

<br />

你复杂修改代码部分，命令行的调试我自己来进行


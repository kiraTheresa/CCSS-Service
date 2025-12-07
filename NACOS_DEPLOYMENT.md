# Nacos 部署运行说明

## 1. Nacos 介绍

Nacos 是一个更易于构建云原生应用的动态服务发现、配置管理和服务管理平台。它提供了以下核心功能：

- **服务发现与服务健康检查**：支持基于 DNS 和基于 RPC 的服务发现，实时健康检查，防止向不健康的实例发送请求
- **动态配置服务**：集中管理应用配置，支持热更新，无需重启服务
- **动态 DNS 服务**：支持权重路由，更容易实现中间层负载均衡、更灵活的 DNS 解析
- **服务及其元数据管理**：支持服务元数据的 CRUD 操作

## 2. 部署方式

本项目使用 Docker 部署 Nacos 服务器，采用单机模式运行。

### 2.1 部署准备

- 安装 Docker 和 Docker Compose
- 克隆项目代码

### 2.2 启动 Nacos

使用项目根目录下的 `docker-compose.yml` 文件启动 Nacos 服务：

```bash
docker-compose up -d nacos
```

### 2.3 访问 Nacos 控制台

根据实际运行情况，Nacos 控制台访问地址：

- URL: http://localhost:8848/nacos
- 用户名: nacos
- 密码: nacos

**注意**：从日志可以看到，容器内部 Nacos 使用端口 8080 启动，但通过 Docker 端口映射，我们可以通过主机的 8848 端口访问 Nacos 控制台。

## 3. 配置说明

### 3.1 服务配置

在每个服务的 `application.yml` 和 `application-docker.yml` 文件中添加以下 Nacos 配置：

```yaml
spring:
  cloud:
    nacos:
      discovery:
        server-addr: nacos:8848  # Docker 环境下使用容器名，开发环境使用 localhost:8848
        namespace: dev  # 命名空间，用于环境隔离
        group: COURSEHUB_GROUP  # 服务分组
        ephemeral: true  # 临时实例，服务停止后自动注销
        heart-beat-interval: 5000  # 心跳间隔，单位毫秒
        heart-beat-timeout: 15000  # 心跳超时，单位毫秒
```

### 3.2 健康检查配置

添加 Spring Boot Actuator 依赖并配置健康检查端点：

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info  # 暴露健康检查和信息端点
  endpoint:
    health:
      show-details: always  # 显示详细的健康信息
  health:
    nacos:
      discovery:
        enabled: true  # 启用 Nacos 健康检查
```

### 3.3 Docker Compose 配置

在 `docker-compose.yml` 文件中添加 Nacos 服务配置：

```yaml
nacos:
  image: nacos/nacos-server:v3.1.0
  container_name: nacos
  environment:
    - MODE=standalone  # 单机模式
  ports:
    - "8848:8848"  # 控制台端口
    - "9848:9848"  # 服务间通信端口
  networks:
    - course-network
```

## 4. 服务注册与发现

### 4.1 服务注册

服务启动时，会自动注册到 Nacos，注册信息包括：

- 服务名
- 服务实例 IP
- 服务实例端口
- 服务分组
- 命名空间
- 健康检查状态

### 4.2 服务发现

使用 `DiscoveryClient` 和 `RestTemplate` 通过服务名调用其他服务：

```java
@Autowired
private DiscoveryClient discoveryClient;

@Autowired
private RestTemplate restTemplate;

// 获取服务实例列表
List<ServiceInstance> instances = discoveryClient.getInstances("catalog-service");

// 从实例列表中选择一个实例
ServiceInstance instance = instances.get(0);
String serviceUrl = instance.getUri().toString();

// 调用服务
restTemplate.getForObject(serviceUrl + "/api/courses/" + courseId, Map.class);
```

## 5. 健康检查

### 5.1 自动健康检查

Nacos 会定期发送心跳请求到服务的健康检查端点，默认配置为：

- 心跳间隔：5 秒
- 心跳超时：15 秒

如果服务连续 3 次心跳超时，Nacos 会将该服务实例标记为不健康，并从服务实例列表中移除。

### 5.2 手动检查健康状态

可以通过以下方式检查服务健康状态：

#### 5.2.1 通过 Nacos 控制台

1. 登录 Nacos 控制台
2. 进入「服务管理」→「服务列表」
3. 选择命名空间 `dev` 和分组 `COURSEHUB_GROUP`
4. 查看服务实例的健康状态

#### 5.2.2 通过 HTTP API

```bash
curl -X GET "http://localhost:8848/nacos/v1/ns/instance/list?serviceName=catalog-service&namespaceId=dev"
```

#### 5.2.3 通过服务自身的健康检查端点

```bash
curl http://localhost:8081/actuator/health  # catalog-service 健康检查
curl http://localhost:8082/actuator/health  # enrollment-service 健康检查
```

## 6. 测试方法

### 6.1 使用测试脚本

项目提供了测试脚本 `scripts/nacos-test.sh`，可以自动测试 Nacos 部署和服务注册情况：

```bash
chmod +x scripts/nacos-test.sh
./scripts/nacos-test.sh
```

测试脚本会执行以下操作：

- 启动所有服务
- 等待服务启动
- 检查 Nacos 控制台
- 检查服务注册情况
- 测试服务调用
- 查看容器状态

### 6.2 手动测试

#### 6.2.1 测试服务注册

1. 启动所有服务：

   ```bash
   docker-compose up -d
   ```
2. 检查服务是否注册到 Nacos：

   ```bash
   curl -X GET "http://localhost:8848/nacos/v1/ns/instance/list?serviceName=catalog-service&namespaceId=dev"
   curl -X GET "http://localhost:8848/nacos/v1/ns/instance/list?serviceName=enrollment-service&namespaceId=dev"
   ```

#### 6.2.2 测试服务调用

1. 使用 enrollment-service 调用 catalog-service：
   ```bash
   # 首先获取一个课程ID
   COURSE_ID=$(curl -s http://localhost:8081/api/courses | jq -r '.data[0].id')

   # 然后使用 enrollment-service 调用 catalog-service 验证课程是否存在
   curl -X POST -H "Content-Type: application/json" -d '{"courseId": "'$COURSE_ID'", "studentId": "2024001"}' http://localhost:8082/api/enrollments
   ```

### 6.3 多实例负载均衡测试

#### 6.3.1 启动多个服务实例

1. 启动基础服务：

   ```bash
   docker-compose up -d nacos catalog-db enrollment-db
   ```
2. 启动多个 catalog-service 实例：

   ```bash
   # 启动第一个实例（端口 8081）
   docker-compose up -d catalog-service

   # 启动第二个实例（端口 8083）
   docker run -d --name catalog-service-2 \
     --network course-network \
     -p 8083:8081 \
     -e SPRING_PROFILES_ACTIVE=docker \
     -e DB_URL=jdbc:mysql://catalog-db:3306/catalog_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true \
     -e DB_USERNAME=catalog_user \
     -e DB_PASSWORD=catalog_pass \
     catalog-service

   # 启动第三个实例（端口 8084）
   docker run -d --name catalog-service-3 \
     --network course-network \
     -p 8084:8081 \
     -e SPRING_PROFILES_ACTIVE=docker \
     -e DB_URL=jdbc:mysql://catalog-db:3306/catalog_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true \
     -e DB_USERNAME=catalog_user \
     -e DB_PASSWORD=catalog_pass \
     catalog-service
   ```
3. 启动 enrollment-service：

   ```bash
   docker-compose up -d enrollment-service
   ```

#### 6.3.2 验证负载均衡

1. 修改 catalog-service 的 Controller，返回当前服务的端口号：

   ```java
   // 在 CourseController.java 中添加端口信息到返回结果
   @Autowired
   private ServerProperties serverProperties;

   @GetMapping
   public ResponseEntity<ApiResponse<List<Course>>> getAllCourses() {
       List<Course> courses = courseService.getAllCourses();
       Map<String, Object> data = new HashMap<>();
       data.put("courses", courses);
       data.put("port", serverProperties.getPort());
       return ResponseEntity.ok(new ApiResponse<>("success", data, null));
   }
   ```
2. 编译并重新构建 catalog-service 镜像：

   ```bash
   cd catalog-service
   mvn clean package -DskipTests
   docker build -t catalog-service .
   cd ..
   ```
3. 多次调用 enrollment-service 的测试接口，观察返回的端口号：

   ```bash
   for i in {1..10}; do
     curl -s http://localhost:8082/api/enrollments/test | grep -o "port":[0-9]*
   done
   ```
4. 观察输出结果，应该能看到请求被分发到不同的端口（8081, 8083, 8084），证明负载均衡生效。

##### 多实例负载均衡效果截图：

执行上述命令后，截图显示终端输出，包含不同端口号的分布情况。

### 6.4 故障转移测试

1. 确保有多个 catalog-service 实例在运行：

   ```bash
   docker ps | grep catalog-service
   ```
2. 记录当前运行的 catalog-service 实例的容器 ID。
3. 多次调用 enrollment-service 的测试接口，确保所有实例都能正常响应：

   ```bash
   for i in {1..5}; do
     curl -s http://localhost:8082/api/enrollments/test | grep -o "port":[0-9]*
   done
   ```
4. 停止其中一个 catalog-service 实例：

   ```bash
   docker stop <container_id>
   ```
5. 立即再次调用测试接口，观察请求是否仍然成功：

   ```bash
   for i in {1..5}; do
     curl -s http://localhost:8082/api/enrollments/test | grep -o "port":[0-9]*
   done
   ```
6. 等待约 15-30 秒（Nacos 心跳超时时间），然后检查 Nacos 控制台，确认该实例已被标记为不健康。
7. 再次调用测试接口，观察请求是否只分发到健康的实例。

##### 故障转移效果：

- 截图 1：停止实例前，所有实例的响应情况
- 截图 2：停止实例后，立即调用的响应情况（可能会有一两次失败，这是正常的）
- 截图 3：Nacos 控制台显示实例状态变化
- 截图 4：等待一段时间后，所有请求都成功分发到健康实例

### 6.5 健康检查测试

1. 启动所有服务：

   ```bash
   docker-compose up -d
   ```
2. 检查 Nacos 控制台中的服务健康状态：

   - 登录 Nacos 控制台
   - 进入「服务管理」→「服务列表」
   - 选择命名空间 `dev` 和分组 `COURSEHUB_GROUP`
   - 点击服务名，查看实例列表和健康状态
3. 检查服务自身的健康检查端点：

   ```bash
   curl -s http://localhost:8081/actuator/health | jq
   curl -s http://localhost:8082/actuator/health | jq
   ```
4. 使用 Nacos API 检查服务健康状态：

   ```bash
   curl -s "http://localhost:8848/nacos/v1/ns/instance/list?serviceName=catalog-service&namespaceId=dev&healthyOnly=true" | jq
   ```

##### 健康检查截图：

- 截图 1：Nacos 控制台中服务实例的健康状态列表
- 截图 2：调用 `/actuator/health` 端点的返回结果
- 截图 3：使用 Nacos API 获取健康实例的返回结果

## 7. 常见问题

### 7.1 服务无法注册到 Nacos

**可能原因：**

- Nacos 服务器未启动或网络不通
- 配置文件中的 `server-addr` 地址错误
- 命名空间 ID 不存在

**解决方法：**

- 检查 Nacos 容器是否正常运行：`docker logs nacos`
- 确保服务和 Nacos 在同一个网络中
- 在 Nacos 控制台创建对应的命名空间

### 7.2 服务间调用失败

**可能原因：**

- 服务名配置错误（大小写敏感）
- 目标服务未启动或未注册成功
- 网络配置问题

**解决方法：**

- 在 Nacos 控制台确认目标服务已注册且状态健康
- 检查 `DiscoveryClient` 调用的服务名是否与 Nacos 中注册的服务名一致
- 确保所有服务在同一个 Docker 网络中

### 7.3 健康检查一直显示不健康

**可能原因：**

- 健康检查接口返回非 200 状态码
- 应用未完全启动
- Actuator 端点未暴露

**解决方法：**

- 确保 `management.endpoints.web.exposure.include` 包含 `health`
- 检查 `/actuator/health` 端点是否可访问
- 增加服务启动等待时间

### 7.4 Nacos 控制台无法访问

**可能原因：**

- Nacos 服务未启动
- 端口映射错误
- 防火墙阻止访问

**解决方法：**

- 检查 Nacos 容器是否正常运行：`docker ps`
- 检查端口映射：`docker-compose ps`
- 检查防火墙设置

## 8. 最佳实践

### 8.1 命名空间和分组

- 使用命名空间隔离不同环境（开发、测试、生产）
- 使用分组管理不同业务模块的服务

### 8.2 服务实例管理

- 使用临时实例（ephemeral: true），便于自动上下线
- 配置合理的心跳间隔和超时时间
- 实现优雅关闭，确保服务下线前通知 Nacos

### 8.3 高可用部署

在生产环境中，建议使用 Nacos 集群部署，提高可用性：

- 至少部署 3 个节点
- 使用负载均衡器分发请求
- 配置持久化存储

### 8.4 监控和告警

- 监控 Nacos 服务器的 CPU、内存、磁盘使用情况
- 监控服务注册和健康状态
- 配置告警规则，及时发现异常情况

## 9. 参考资料

- [Nacos 官方文档](https://nacos.io/zh-cn/docs/quick-start.html)
- [Spring Cloud Alibaba 官方文档](https://sca.aliyun.com/docs/2024.x/user-guide/nacos/)
- [Spring Cloud 官方文档](https://spring.io/projects/spring-cloud)

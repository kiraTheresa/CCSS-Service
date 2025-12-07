# 校园选课系统（微服务版）

注（！）： 

> Nacos 部署和配置章节详见根目录下 `NACOS_DEPLOYMENT.md`

## 项目简介

- **项目名称**：course-cloud
- **版本号**：v1.1.0
- **基于版本**：course:v1.1.0（hw04b）
- **项目阶段**：微服务架构（初次拆分）

本项目将单体选课系统拆分为两个独立的微服务，实现了课程管理、学生管理和选课管理功能，并通过HTTP通信实现服务间协作。

## 架构图

```
客户端
↓
├─→ catalog-service (8081) → catalog_db (3307)
│   └── 课程管理
│
└─→ enrollment-service (8082) → enrollment_db (3308)
    ├── 学生管理
    ├── 选课管理
    └── HTTP调用 → catalog-service（验证课程）
```

## 技术栈

- Spring Boot 3.5
- Java 25
- MySQL 8.0
- Docker & Docker Compose
- RestTemplate（服务间通信）

## 环境要求

- JDK 25+
- Maven 3.8+
- Docker 20.10+
- Docker Compose 2.0+

## 构建和运行步骤

### 1. 本地构建项目

```bash
# 构建课程目录服务
cd catalog-service
mvn clean package -DskipTests

# 构建选课服务
cd ../enrollment-service
mvn clean package -DskipTests
cd ..
```

### 2. 使用Docker Compose启动所有服务

```bash
docker-compose up -d --build
```

### 3. 验证服务是否正常运行

```bash
# 查看容器状态
docker-compose ps

# 查看服务日志
docker-compose logs -f catalog-service
docker-compose logs -f enrollment-service
```

## API文档

### 课程目录服务（catalog-service）

| 方法   | URL                      | 描述           |
| ------ | ------------------------ | -------------- |
| GET    | /api/courses             | 获取所有课程   |
| GET    | /api/courses/{id}        | 获取单个课程   |
| GET    | /api/courses/code/{code} | 按课程代码查询 |
| POST   | /api/courses             | 创建课程       |
| PUT    | /api/courses/{id}        | 更新课程       |
| DELETE | /api/courses/{id}        | 删除课程       |

### 选课服务（enrollment-service）

#### 学生管理

| 方法   | URL                | 描述         |
| ------ | ------------------ | ------------ |
| GET    | /api/students      | 获取所有学生 |
| GET    | /api/students/{id} | 获取单个学生 |
| POST   | /api/students      | 创建学生     |
| PUT    | /api/students/{id} | 更新学生     |
| DELETE | /api/students/{id} | 删除学生     |

#### 选课管理

| 方法   | URL                                  | 描述             |
| ------ | ------------------------------------ | ---------------- |
| GET    | /api/enrollments                     | 获取所有选课记录 |
| GET    | /api/enrollments/course/{courseId}   | 按课程查询选课   |
| GET    | /api/enrollments/student/{studentId} | 按学生查询选课   |
| POST   | /api/enrollments                     | 学生选课         |
| DELETE | /api/enrollments/{id}                | 学生退课         |

## 测试说明

### 使用测试脚本

```bash
# 给测试脚本添加执行权限
chmod +x test-services.sh

# 执行测试脚本
./test-services.sh
```

### 手动测试示例

#### 1. 创建课程

```bash
curl -X POST http://localhost:8081/api/courses \
-H "Content-Type: application/json" \
-d '{
"code": "CS101",
"title": "计算机科学导论",
"instructor": {
"id": "T001",
"name": "张教授",
"email": "zhang@example.edu.cn"
},
"schedule": {
"dayOfWeek": "MONDAY",
"startTime": "08:00",
"endTime": "10:00",
"expectedAttendance": 50
},
"capacity": 60,
"enrolled": 0
}'
```

#### 2. 创建学生

```bash
curl -X POST http://localhost:8082/api/students \
-H "Content-Type: application/json" \
-d '{
"studentId": "2024001",
"name": "张三",
"major": "计算机科学与技术",
"grade": 2024,
"email": "zhangsan@example.edu.cn"
}'
```

#### 3. 学生选课

```bash
# 先获取课程ID
COURSE_ID=$(curl -s http://localhost:8081/api/courses | jq -r '.data[0].id')

# 执行选课
curl -X POST http://localhost:8082/api/enrollments \
-H "Content-Type: application/json" \
-d "{
\"courseId\": \"$COURSE_ID\",
\"studentId\": \"2024001\"
}"
```

## 遇到的问题和解决方案

1. **服务间调用失败**

   - 问题：enrollment-service无法连接到catalog-service
   - 解决方案：检查Docker网络配置，确保两个服务在同一网络中，并使用服务名称作为主机名
2. **数据库初始化失败**

   - 问题：服务启动时无法连接到数据库
   - 解决方案：在docker-compose.yml中配置服务依赖关系，使用healthcheck确保数据库完全启动后再启动应用服务
3. **课程容量更新问题**

   - 问题：选课后课程已选人数未更新
   - 解决方案：在enrollment-service中添加了课程容量自动更新逻辑，选课后通过HTTP调用catalog-service更新课程已选人数

## 项目结构

```
course-cloud/
├── README.md # 项目文档
├── docker-compose.yml # Docker编排文件
├── test-services.sh # 测试脚本
├── VERSION # 版本号文件
│
├── catalog-service/ # 课程目录服务
│   ├── src/ # 源代码
│   ├── Dockerfile # Docker构建文件
│   ├── openapi.yaml # OpenAPI文档
│   └── pom.xml # Maven配置文件
│
└── enrollment-service/ # 选课服务
    ├── src/ # 源代码
    ├── Dockerfile # Docker构建文件
    ├── openapi.yaml # OpenAPI文档
    └── pom.xml # Maven配置文件
```

## 停止服务

```bash
docker-compose down -v
```

## 查看日志

```bash
docker-compose logs -f
```

## 常见问题

### Q: 如何查看服务运行状态？

A: 使用 `docker-compose ps` 命令查看所有服务的状态。

### Q: 服务启动失败怎么办？

A: 查看服务日志 `docker-compose logs -f <service-name>`，根据错误信息进行排查。

### Q: 如何进入容器内部？

A: 使用 `docker-compose exec <service-name> bash` 命令进入容器。

### Q: 如何测试数据库连接？

A: 使用 `docker-compose exec <db-service-name> mysql -u <username> -p<password> <database-name>` 命令测试数据库连接。

## 版本管理

- 版本标签：v1.0.0
- Git分支：main

## 开发建议

1. 先本地测试：使用H2内存数据库快速验证逻辑
2. 逐步容器化：先容器化单个服务，验证成功后再容器化其他服务
3. 版本管理：每完成一个重要步骤就提交代码
4. 配置管理：使用Spring Profile区分开发环境和Docker环境

## 参考资源

- Spring Boot官方文档：https://spring.io/projects/spring-boot
- Docker Compose官方文档：https://docs.docker.com/compose/
- RestTemplate使用指南：https://docs.spring.io/spring-framework/reference/integration/rest-clients.html

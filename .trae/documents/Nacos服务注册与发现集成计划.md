# Nacos服务注册与发现集成计划

## 1. 添加Nacos Discovery依赖

为每个服务添加Spring Cloud Alibaba Nacos Discovery依赖，需要修改以下文件：

- `catalog-service/pom.xml`
- `enrollment-service/pom.xml`

添加内容包括：
- Spring Cloud和Spring Cloud Alibaba依赖管理
- Spring Cloud Alibaba Nacos Discovery依赖
- Spring Boot Actuator依赖（用于健康检查）

## 2. 更新服务配置文件

修改每个服务的配置文件，添加Nacos配置：

### 2.1 catalog-service
- `src/main/resources/application.yml`
- `src/main/resources/application-docker.yml`

### 2.2 enrollment-service
- `src/main/resources/application.yml`
- `src/main/resources/application-docker.yml`

配置内容包括：
- 服务名
- Nacos服务器地址
- 命名空间
- 分组
- 健康检查配置

## 3. 修改服务调用方式

修改enrollment-service，使用DiscoveryClient + RestTemplate通过服务名调用其他服务：

- `src/main/java/com/zjgsu/ms/hxy/enrollment/service/EnrollmentService.java`
  - 删除`@Value("${catalog-service.url}")`注解
  - 添加`DiscoveryClient`依赖
  - 修改`enrollCourse`和`withdrawCourse`方法，通过服务名调用catalog-service

## 4. 更新Docker Compose配置

修改`docker-compose.yml`文件：

- 添加Nacos服务配置
- 修改各服务的depends_on，添加对Nacos的依赖
- 确保所有服务在同一个网络中

## 5. 创建测试脚本

在`scripts`目录下创建`nacos-test.sh`脚本，包含：

- 启动所有服务
- 等待服务启动
- 检查Nacos控制台
- 检查服务注册情况
- 测试服务调用
- 查看容器状态

## 6. 提交和标记

完成所有修改后：

- 执行`git add .`
- 执行`git commit -m "feat(nacos): integrate nacos for service discovery"`
- 执行`git tag v1.1.0`

## 实施顺序

1. 首先修改pom.xml文件，添加依赖
2. 然后修改配置文件，添加Nacos配置
3. 接着修改服务调用代码
4. 更新Docker Compose配置
5. 创建测试脚本
6. 最后提交并标记版本

## 预期结果

- 所有服务能够成功注册到Nacos
- 服务间通过服务名进行调用
- Nacos能够准确监控服务健康状态
- 支持多实例启动和负载均衡
- 能够通过测试脚本验证功能
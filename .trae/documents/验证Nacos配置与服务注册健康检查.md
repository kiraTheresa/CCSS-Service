# 验证Nacos配置与服务注册健康检查

## 1. 检查配置完整性

已确认两个服务的配置文件和依赖都符合要求：

* ✅ catalog-service 和 enrollment-service 的 application.yml 和 application-docker.yml 中已添加Nacos配置

* ✅ 两个服务都已添加Spring Boot Actuator依赖

* ✅ 健康检查端点已配置

* ✅ docker-compose.yml中Nacos服务已配置

## 2. 验证步骤

### 2.1 启动Nacos服务

```bash
docker-compose up -d nacos
```

### 2.2 验证Nacos控制台可访问

访问 <http://localhost:8848/nacos，使用用户名/密码> nacos/nacos 登录

### 2.3 启动数据库服务

```bash
docker-compose up -d catalog-db enrollment-db
```

### 2.4 启动应用服务

```bash
docker-compose up -d catalog-service enrollment-service
```

### 2.5 验证服务注册

* 通过Nacos控制台查看服务列表，确认两个服务已注册

* 通过API验证服务注册：

  ```bash
  curl -X GET "http://localhost:8848/nacos/v1/ns/instance/list?serviceName=catalog-service&namespaceId=dev"
  curl -X GET "http://localhost:8848/nacos/v1/ns/instance/list?serviceName=enrollment-service&namespaceId=dev"
  ```

### 2.6 验证健康检查

* 访问服务的健康检查端点：

  ```bash
  curl http://localhost:8081/actuator/health
  curl http://localhost:8082/actuator/health
  ```

* 通过Nacos API获取健康实例：

  ```bash
  curl -s "http://localhost:8848/nacos/v1/ns/instance/list?serviceName=catalog-service&namespaceId=dev&healthyOnly=true"
  ```

### 2.7 测试服务间通信

* 使用enrollment-service调用catalog-service验证服务发现功能

### 2.8 运行测试脚本

```bash
chmod +x scripts/nacos-test.sh
./scripts/nacos-test.sh
```

## 3. 预期结果

* ✅ Nacos服务成功启动

* ✅ 两个应用服务成功注册到Nacos

* ✅ 健康检查状态正常

* ✅ 服务间通信正常

* ✅ 测试脚本执行通过

## 4. 故障排查

如果出现问题，按照以下步骤排查：

1. 检查Nacos日志：`docker logs nacos`
2. 检查应用服务日志：`docker logs catalog-service` 和 `docker logs enrollment-service`
3. 验证网络连接：确保所有服务在同一个Docker网络中
4. 检查配置文件：确认Nacos服务器地址、命名空间和分组配置正确
5. 检查健康检查端点：确认/actuator/health可访问

## 5. 清理资源

测试完成后，清理资源：

```bash
```


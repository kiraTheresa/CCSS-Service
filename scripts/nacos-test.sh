#!/bin/bash
echo "启动所有服务..."
docker-compose up -d

echo "等待服务启动..."
sleep 30

echo "检查 Nacos 控制台..."
curl http://localhost:8848/nacos/ 2>/dev/null || echo "Nacos 控制台无法访问"

echo "检查服务注册情况..."
curl -X GET "http://localhost:8848/nacos/v1/ns/instance/list?serviceName=catalog-service&namespaceId=dev" 2>/dev/null || echo "无法获取 catalog-service 实例列表"
curl -X GET "http://localhost:8848/nacos/v1/ns/instance/list?serviceName=enrollment-service&namespaceId=dev" 2>/dev/null || echo "无法获取 enrollment-service 实例列表"

echo "测试服务调用..."
echo "测试1: 调用 enrollment-service 健康检查接口"
curl http://localhost:8082/actuator/health 2>/dev/null || echo "enrollment-service 健康检查失败"

echo "测试2: 调用 catalog-service 健康检查接口"
curl http://localhost:8081/actuator/health 2>/dev/null || echo "catalog-service 健康检查失败"

echo "查看容器状态..."
docker-compose ps

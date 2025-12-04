# 创建Docker环境配置文件计划

## 1. 问题分析

目前docker-compose.yml中使用的是`SPRING_PROFILES_ACTIVE: prod`，但实际项目中没有`application-prod.yml`文件，导致服务在Docker环境中无法使用正确的配置。

## 2. 解决方案

1. 为每个微服务创建`application-docker.yml`文件，配置Docker环境下的特定参数
2. 修改docker-compose.yml，将环境变量`SPRING_PROFILES_ACTIVE`从`prod`改为`docker`
3. 确保配置文件中使用环境变量，而不是硬编码的值

## 3. 实施步骤

### 3.1 创建catalog-service的application-docker.yml

- 在`catalog-service/src/main/resources/`目录下创建`application-docker.yml`文件
- 配置数据源，使用环境变量`DB_URL`、`DB_USERNAME`和`DB_PASSWORD`
- 保留其他基础配置

### 3.2 创建enrollment-service的application-docker.yml

- 在`enrollment-service/src/main/resources/`目录下创建`application-docker.yml`文件
- 配置数据源，使用环境变量`DB_URL`、`DB_USERNAME`和`DB_PASSWORD`
- 配置服务间通信，使用环境变量`CATALOG_SERVICE_URL`
- 保留其他基础配置

### 3.3 修改docker-compose.yml

- 将`SPRING_PROFILES_ACTIVE`的值从`prod`改为`docker`
- 确保所有环境变量都正确传递

## 4. 预期结果

- 每个微服务都有对应的Docker环境配置文件
- docker-compose.yml使用正确的环境配置
- 服务在Docker环境中能够使用正确的配置启动


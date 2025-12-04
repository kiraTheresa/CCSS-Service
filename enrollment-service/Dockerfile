# ----------- Builder 阶段 -----------
FROM maven:3.9.6-eclipse-temurin-17 AS builder
WORKDIR /app

# 1. 复制 pom.xml 并提前下载依赖（可缓存）
COPY pom.xml .
RUN mvn dependency:go-offline -B

# 2. 复制全部源码并开始构建
COPY src ./src
RUN mvn clean package -DskipTests


# ----------- Runtime 阶段 -----------
FROM eclipse-temurin:17-jre
WORKDIR /app

# 自动匹配 target 下的唯一 JAR（不用写死名称）
COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8082
ENTRYPOINT ["java", "-jar", "app.jar"]

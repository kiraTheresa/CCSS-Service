# ----------- Builder 阶段 -----------
FROM maven:3.9.6-eclipse-temurin-17 AS builder
WORKDIR /app

# 1. 先 copy pom.xml，下载依赖（利用 Docker 缓存）
COPY pom.xml .
RUN mvn dependency:go-offline -B

# 2. 再 copy 源码
COPY src ./src

# 3. 打包
RUN mvn clean package -DskipTests


# ----------- Runtime 阶段 -----------
FROM eclipse-temurin:17-jre
WORKDIR /app

# 自动匹配 target 下的 jar（不用写死名称）
COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]

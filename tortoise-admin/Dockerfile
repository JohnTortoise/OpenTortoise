# 第一阶段：构建阶段
FROM maven:3.6.3-jdk-8-slim AS builder

# 设置工作目录
WORKDIR /app

# 复制pom.xml文件并下载依赖
COPY pom.xml .
RUN mvn dependency:go-offline -B

# 复制源代码并构建项目
COPY src ./src
RUN mvn package -DskipTests

# 第二阶段：运行阶段 - 使用指定版本的 OpenJDK
FROM openjdk:8u342-jre-slim

# 设置工作目录
WORKDIR /app

# 从构建阶段复制构建好的jar包
COPY --from=builder /app/target/tortoise-admin-1.0.0.jar /app/app.jar

# 暴露端口
EXPOSE 8080

# 启动命令
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
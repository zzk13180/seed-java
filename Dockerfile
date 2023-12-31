# ========================================
# 多阶段构建 - 构建阶段
# ========================================
FROM maven:3.9-eclipse-temurin-21-alpine AS builder

WORKDIR /app

# 复制 Maven 配置
COPY pom.xml .
COPY common/pom.xml common/
COPY common/common-core/pom.xml common/common-core/
COPY common/common-web/pom.xml common/common-web/
COPY common/common-security/pom.xml common/common-security/
COPY common/common-redis/pom.xml common/common-redis/
COPY samples/pom.xml samples/
COPY samples/samples-interface/pom.xml samples/samples-interface/
COPY samples/samples-db/pom.xml samples/samples-db/
COPY samples/samples-service/pom.xml samples/samples-service/
COPY samples/samples-controller/pom.xml samples/samples-controller/

# 下载依赖 (利用Docker缓存层)
RUN --mount=type=cache,target=/root/.m2 \
    mvn dependency:go-offline -B || true

# 复制源代码
COPY common common
COPY samples samples
COPY sql sql

# 构建应用
RUN --mount=type=cache,target=/root/.m2 \
    mvn clean package -DskipTests -B

# ========================================
# 运行阶段
# ========================================
FROM eclipse-temurin:21-jre-alpine

# 安装必要工具
RUN apk add --no-cache tzdata curl && \
    cp /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && \
    echo "Asia/Shanghai" > /etc/timezone && \
    apk del tzdata

# 创建非root用户
RUN addgroup -S spring && adduser -S spring -G spring

WORKDIR /app

# 复制构建产物
COPY --from=builder /app/samples/samples-controller/target/*-exec.jar app.jar

# 创建日志目录
RUN mkdir -p /app/logs && chown -R spring:spring /app

USER spring:spring

# 健康检查
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# JVM 配置
ENV JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/app/logs/heapdump.hprof"
ENV SPRING_PROFILES_ACTIVE="prod"

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar app.jar"]

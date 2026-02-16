# ========================================
# Seed Cloud Microservices - 通用 Dockerfile
# ========================================
#
# 用途说明:
#   这个 Dockerfile 用于构建 Seed Java 微服务项目的各个模块。
#   采用多阶段构建策略，将构建环境和运行环境分离，提高镜像效率。
#
# 支持的模块:
#   - apps/platform/gateway: API 网关服务
#   - apps/domains/identity/auth-service: 认证授权服务
#   - apps/domains/administration/system-service: 系统管理服务
#
# 构建参数:
#   - MODULE: 指定要构建的模块路径 (默认: apps/platform/gateway)
#   - SKIP_TESTS: 是否跳过测试 (默认: true)
#
# 使用示例:
#   docker build --build-arg MODULE=apps/platform/gateway -t seed-cloud/gateway:latest .
#   docker build --build-arg MODULE=apps/domains/identity/auth-service -t seed-cloud/auth:latest .
#   docker build --build-arg MODULE=apps/domains/administration/system-service -t seed-cloud/system:latest .
#
# ========================================

# ========================================
# 多阶段构建 - 构建阶段 (Builder Stage)
# ========================================
# 使用 Maven 和 Eclipse Temurin JDK 21 的 Alpine 镜像作为构建环境
# Alpine 镜像轻量，适合构建阶段
FROM maven:3.9-eclipse-temurin-21-alpine AS builder

# 设置工作目录，所有后续操作都在 /app 目录下进行
WORKDIR /app

# ========================================
# 第一步: 复制 Maven 项目结构和配置文件
# ========================================
# 复制根 pom.xml 文件，定义项目整体结构和依赖管理
COPY pom.xml .

# 复制所有子模块的 pom.xml 文件
# 这是为了利用 Docker 层缓存：pom.xml 变化不频繁，先复制它们可以缓存依赖下载

# libs: 共享库
COPY libs/core/pom.xml libs/core/
COPY libs/infrastructure/datasource/pom.xml libs/infrastructure/datasource/
COPY libs/infrastructure/redis/pom.xml libs/infrastructure/redis/
COPY libs/infrastructure/rpc/pom.xml libs/infrastructure/rpc/
COPY libs/components/security/pom.xml libs/components/security/
COPY libs/components/web/pom.xml libs/components/web/
COPY libs/components/log/pom.xml libs/components/log/
COPY libs/components/file/pom.xml libs/components/file/
COPY libs/components/swagger/pom.xml libs/components/swagger/
COPY libs/api/system-api/pom.xml libs/api/system-api/

# apps: 可部署应用
COPY apps/platform/gateway/pom.xml apps/platform/gateway/
COPY apps/domains/identity/auth-service/pom.xml apps/domains/identity/auth-service/
COPY apps/domains/administration/system-service/pom.xml apps/domains/administration/system-service/

# ========================================
# 第二步: 下载项目依赖 (利用 Docker 缓存层)
# ========================================
# 在复制源代码之前先下载依赖，这样如果源代码改变但依赖不变，可以利用缓存
# --mount=type=cache,target=/root/.m2: 将宿主机的 Maven 本地仓库挂载为缓存
# -B: 批处理模式，不显示进度条
# || true: 如果失败，继续执行（某些依赖可能暂时不可用）
RUN --mount=type=cache,target=/root/.m2 \
  mvn dependency:go-offline -B || true

# ========================================
# 第三步: 复制源代码
# ========================================
# 复制所有源代码到容器中
COPY libs libs
COPY apps apps
# 复制数据库初始化脚本
COPY ops/deployment/db ops/deployment/db

# ========================================
# 第四步: 构建应用
# ========================================
# ARG 指令定义构建时参数，可以通过 --build-arg 传递
ARG MODULE=apps/platform/gateway
ARG SKIP_TESTS=true
ARG PORT=8080

# 编译指定的模块并复制 JAR 到固定位置
# -pl ${MODULE}: 只构建指定模块
# -am: also-make，构建指定模块及其依赖
# -DskipTests=${SKIP_TESTS}: 根据参数决定是否跳过测试
# --mount=type=cache,target=/root/.m2: 使用缓存加速构建
# && cp: 将构建好的 JAR 复制到固定位置 /app/app.jar，便于运行阶段复制
RUN --mount=type=cache,target=/root/.m2 \
  mvn clean package -pl ${MODULE} -am -DskipTests=${SKIP_TESTS} -B && \
  cp ${MODULE}/target/*.jar /app/app.jar

# ========================================
# 运行阶段 (Runtime Stage)
# ========================================
# 使用 Eclipse Temurin JRE 21 Alpine 镜像作为运行环境
# 只包含 JRE，不包含 JDK，镜像更小
FROM eclipse-temurin:21-jre-alpine

# ========================================
# 第一步: 安装系统依赖和配置时区
# ========================================
# 安装时区数据包和 curl 工具
RUN apk add --no-cache tzdata curl && \
  # 设置时区为亚洲上海
  cp /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && \
  echo "Asia/Shanghai" > /etc/timezone && \
  # 删除时区数据包以减小镜像大小
  apk del tzdata

# ========================================
# 第二步: 创建非 root 用户
# ========================================
# 安全最佳实践：不要使用 root 用户运行应用
# 创建 seed 用户组和用户
RUN addgroup -S seed && adduser -S seed -G seed

# 设置工作目录
WORKDIR /app

# ========================================
# 第三步: 复制构建产物
# ========================================
# 从构建阶段复制编译好的 JAR 文件（已复制到固定位置）
COPY --from=builder /app/app.jar app.jar

# 复制数据库初始化脚本（如果需要的话）
COPY --from=builder /app/ops/deployment/db /app/db

# ========================================
# 第四步: 创建日志目录并设置权限
# ========================================
# 创建日志目录，并将所有权授予 seed 用户
RUN mkdir -p /app/logs && chown -R seed:seed /app

# 切换到非 root 用户
USER seed

# ========================================
# 第五步: 配置健康检查
# ========================================
# Docker 健康检查配置
# --interval=30s: 每 30 秒检查一次
# --timeout=10s: 检查超时时间 10 秒
# --start-period=60s: 容器启动后 60 秒开始检查
# --retries=3: 连续失败 3 次认为不健康
# 重新声明构建参数，使其在运行阶段可用（多阶段构建中 ARG 不跨阶段传递）
ARG PORT=8080
ENV PORT=${PORT}
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:${PORT}/actuator/health || exit 1

# ========================================
# 第六步: 配置环境变量
# ========================================
# JVM 优化参数
ENV JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/app/logs/heapdump.hprof"
# Spring Boot 激活的生产环境配置
ENV SPRING_PROFILES_ACTIVE="prod"
# 日志文件路径
ENV LOG_PATH="/app/logs"

# ========================================
# 第七步: 暴露端口和设置启动命令
# ========================================
# 注意: EXPOSE 不支持 ARG 变量展开，此处仅作文档声明
# 实际端口由各服务的 application.yml 和 docker-compose 的 PORT 构建参数决定
# gateway: 8080, auth: 9100, system: 9200
EXPOSE ${PORT}

# 容器启动命令
# 使用 shell 形式确保环境变量被正确展开
# -Djava.security.egd=file:/dev/./urandom: 优化随机数生成性能
ENTRYPOINT ["sh", "-c", "mkdir -p /app/logs && java $JAVA_OPTS -Dserver.port=${PORT} -Djava.security.egd=file:/dev/./urandom -jar app.jar"]

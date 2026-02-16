# ========================================
# Seed Cloud Frontend - 多阶段 Dockerfile
# ========================================
#
# 用途: 构建 Vue 3 前端并使用 Nginx 静态托管
#
# 构建示例:
#   docker build -t seed-cloud/frontend:latest -f apps/frontend/Dockerfile .
#
# 运行示例:
#   docker run -p 80:80 \
#     -e BACKEND_URL=gateway:8080 \
#     seed-cloud/frontend:latest
#
# ========================================

# ========================================
# 阶段一: 构建 (Builder Stage)
# ========================================
FROM node:22-alpine AS builder

# 安装 pnpm
RUN corepack enable && corepack prepare pnpm@latest --activate

WORKDIR /app

# 先复制依赖描述文件，利用 Docker 缓存层
COPY package.json pnpm-lock.yaml pnpm-workspace.yaml ./
COPY apps/frontend/package.json apps/frontend/

# 安装依赖
RUN pnpm install --frozen-lockfile --filter @seed-cloud/frontend...

# 复制前端源码
COPY apps/frontend/ apps/frontend/

# 构建生产包
RUN pnpm --filter @seed-cloud/frontend run build

# ========================================
# 阶段二: 运行 (Runtime Stage)
# ========================================
FROM nginx:1.27-alpine

# 删除默认配置
RUN rm /etc/nginx/conf.d/default.conf

# 复制自定义 Nginx 配置（模板）
COPY apps/frontend/docker/nginx.conf /etc/nginx/templates/default.conf.template

# 从构建阶段复制打包产物
COPY --from=builder /app/apps/frontend/dist /usr/share/nginx/html

# 默认后端地址（可通过环境变量覆盖）
ENV BACKEND_URL=gateway:8080

EXPOSE 80

# 健康检查：每 30 秒检查一次，超时 3 秒，启动后等待 5 秒，连续 3 次失败则不健康
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:80/ || exit 1

# Nginx 模板会自动替换 ${BACKEND_URL} 环境变量
CMD ["nginx", "-g", "daemon off;"]

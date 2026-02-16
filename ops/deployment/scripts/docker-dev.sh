#!/bin/bash
# ========================================
# Docker Compose 开发环境启动脚本
# ========================================

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
DOCKER_DIR="$SCRIPT_DIR/../docker"

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 打印带颜色的消息
info() { echo -e "${BLUE}ℹ ${NC}$1"; }
success() { echo -e "${GREEN}✔ ${NC}$1"; }
warn() { echo -e "${YELLOW}⚠ ${NC}$1"; }
error() { echo -e "${RED}✖ ${NC}$1"; exit 1; }

# 检查 Docker 是否安装
check_docker() {
    if ! command -v docker &> /dev/null; then
        error "Docker 未安装。请先安装 Docker: https://docs.docker.com/get-docker/"
    fi

    if ! docker info &> /dev/null; then
        error "Docker 未运行。请先启动 Docker Desktop 或 Docker 服务。"
    fi

    success "Docker 已就绪"
}

# 检查 Docker Compose 是否可用
check_compose() {
    if ! docker compose version &> /dev/null; then
        error "Docker Compose 不可用。请确保已安装 Docker Compose V2。"
    fi
    success "Docker Compose 已就绪"
}

# 显示帮助信息
show_help() {
    echo ""
    echo "用法: $0 <命令>"
    echo ""
    echo "命令:"
    echo "  infra       启动基础设施服务 (nacos, redis, postgres, minio, sentinel)"
    echo "  app         启动全部服务 (基础设施 + gateway, auth, system)"
    echo "  dev         启动开发模式 (与 app 相同)"
    echo "  stop        停止所有服务"
    echo "  down        停止并删除所有容器"
    echo "  logs        查看所有服务日志"
    echo "  ps          查看服务状态"
    echo "  clean       清理所有数据卷 (⚠️ 危险操作)"
    echo ""
    echo "示例:"
    echo "  $0 infra    # 仅启动基础设施，用于本地开发后端服务"
    echo "  $0 app      # 启动完整的微服务环境"
    echo ""
}

# 主逻辑
main() {
    cd "$DOCKER_DIR" || error "无法进入目录: $DOCKER_DIR"

    case "${1:-help}" in
        infra)
            check_docker
            check_compose
            info "正在启动基础设施服务..."
            docker compose --profile infra up -d
            success "基础设施服务已启动"
            echo ""
            # 初始化 Nacos 管理员用户（Nacos v2.4+ 首次启动时需要手动初始化）
            info "正在等待 Nacos 就绪并初始化管理员用户..."
            NACOS_READY=false
            for i in $(seq 1 30); do
                if curl -sf http://localhost:8848/nacos/v1/console/health/readiness > /dev/null 2>&1; then
                    NACOS_READY=true
                    break
                fi
                sleep 2
            done
            if [ "$NACOS_READY" = true ]; then
                NACOS_IDENTITY_VALUE=$(grep -E '^NACOS_AUTH_IDENTITY_VALUE=' .env 2>/dev/null | cut -d= -f2)
                NACOS_IDENTITY_KEY=$(grep -E '^NACOS_AUTH_IDENTITY_KEY=' .env 2>/dev/null | cut -d= -f2)
                INIT_RESULT=$(curl -s -o /dev/null -w "%{http_code}" -X POST \
                    "http://localhost:8848/nacos/v1/auth/users/admin?username=nacos&password=nacos" \
                    -H "${NACOS_IDENTITY_KEY:-serverIdentity}: ${NACOS_IDENTITY_VALUE:-security}" 2>&1)
                if [ "$INIT_RESULT" = "200" ]; then
                    success "Nacos 管理员用户已初始化 (nacos/nacos)"
                elif [ "$INIT_RESULT" = "409" ]; then
                    success "Nacos 管理员用户已存在"
                else
                    warn "Nacos 管理员初始化返回状态码: $INIT_RESULT，请手动检查"
                fi
            else
                warn "Nacos 未在 60 秒内就绪，请手动初始化管理员用户"
            fi
            echo ""
            info "服务访问地址:"
            echo "  - Nacos:    http://localhost:8848/nacos"
            echo "  - Redis:    localhost:6379"
            echo "  - Postgres: localhost:5432"
            echo "  - MinIO:    http://localhost:9001"
            echo "  - Sentinel: http://localhost:8858"
            ;;
        app)
            check_docker
            check_compose
            info "正在启动全部服务 (基础设施 + 应用服务)..."
            docker compose --profile app up -d
            success "全部服务已启动"
            echo ""
            info "服务访问地址:"
            echo "  - Gateway:  http://localhost:8080"
            echo "  - Auth:     http://localhost:9100"
            echo "  - System:   http://localhost:9200"
            ;;
        dev)
            check_docker
            check_compose
            info "正在启动开发模式..."
            docker compose --profile dev up -d
            success "开发模式已启动"
            ;;
        stop)
            info "正在停止所有服务..."
            docker compose --profile infra --profile app --profile dev stop
            success "所有服务已停止"
            ;;
        down)
            info "正在停止并删除所有容器..."
            docker compose --profile infra --profile app --profile dev down
            success "所有容器已删除"
            ;;
        logs)
            docker compose --profile infra --profile app --profile dev logs -f
            ;;
        ps)
            docker compose --profile infra --profile app --profile dev ps
            ;;
        clean)
            warn "此操作将删除所有数据卷，包括数据库数据！"
            read -p "确定要继续吗？(yes/no): " confirm
            if [ "$confirm" = "yes" ]; then
                docker compose --profile infra --profile app --profile dev down -v
                success "所有容器和数据卷已删除"
            else
                info "操作已取消"
            fi
            ;;
        help|--help|-h)
            show_help
            ;;
        *)
            error "未知命令: $1\n运行 '$0 help' 查看帮助信息"
            ;;
    esac
}

main "$@"

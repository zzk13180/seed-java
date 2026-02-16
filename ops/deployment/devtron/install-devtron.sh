#!/bin/bash
#==============================================================================
# Devtron 安装和配置脚本
#
# 功能: 在 Kubernetes 集群中安装、配置和管理 Devtron CI/CD 平台
# 支持: Kind 本地集群 / 云端 Kubernetes 集群
#
# 使用方法:
#   bash install-devtron.sh <命令>
#
# 常用命令:
#   install   - 安装 Devtron
#   status    - 查看安装状态
#   password  - 获取管理员密码
#   dashboard - 获取 Dashboard 访问方式
#
# 环境要求:
#   - kubectl (已配置集群访问)
#   - helm 3.x
#   - 可选: kind (本地开发)
#
# 网络说明:
#   Devtron 镜像托管在 quay.io，国内网络可能需要代理
#   Kind 集群可通过配置 containerd 代理解决镜像拉取问题
#==============================================================================
set -e  # 遇到错误立即退出

#------------------------------------------------------------------------------
# 配置变量
#------------------------------------------------------------------------------
DEVTRON_NAMESPACE="devtroncd"    # Devtron 安装的命名空间
SEED_NAMESPACE="seed"            # Seed Cloud 应用的命名空间
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"  # 脚本所在目录

#------------------------------------------------------------------------------
# 终端颜色定义
#------------------------------------------------------------------------------
RED='\033[0;31m'     # 错误信息
GREEN='\033[0;32m'   # 成功信息
BLUE='\033[0;34m'    # 提示信息
NC='\033[0m'         # 重置颜色

#------------------------------------------------------------------------------
# 日志输出函数
#------------------------------------------------------------------------------
log_info() { echo -e "${BLUE}[INFO]${NC} $1"; }      # 普通信息
log_success() { echo -e "${GREEN}[OK]${NC} $1"; }    # 成功信息
log_error() { echo -e "${RED}[ERROR]${NC} $1"; }     # 错误信息

#==============================================================================
# 辅助函数
#==============================================================================

# 显示帮助信息
show_help() {
    echo "用法: $0 <命令>"
    echo ""
    echo "安装管理:"
    echo "  install      安装 Devtron (使用 Helm)"
    echo "  upgrade      升级 Devtron 到最新版本"
    echo "  uninstall    卸载 Devtron 并清理资源"
    echo ""
    echo "状态查看:"
    echo "  status       查看 Pod 和服务状态"
    echo "  password     获取管理员密码"
    echo "  dashboard    获取 Dashboard 访问地址"
    echo "  wait         等待所有 Pod 就绪"
    echo "  images       检查镜像拉取状态"
    echo ""
    echo "项目配置:"
    echo "  configure    配置 Seed Cloud 项目命名空间"
    echo "  setup-keys   设置组件加密密钥"
    echo ""
    echo "示例:"
    echo "  $0 install       # 安装 Devtron"
    echo "  $0 status        # 查看状态"
    echo "  $0 password      # 获取密码"
    echo "  $0 setup-keys    # 设置加密密钥"
}

# 检查前置依赖
# 确保 kubectl 和 helm 已安装，且能连接到集群
check_prerequisites() {
    command -v kubectl &> /dev/null || { log_error "kubectl 未安装"; exit 1; }
    command -v helm &> /dev/null || { log_error "helm 未安装"; exit 1; }
    kubectl cluster-info &> /dev/null || { log_error "无法连接到集群"; exit 1; }
}

#==============================================================================
# 核心功能函数
#==============================================================================

# 安装 Devtron
# 流程:
#   1. 检测环境 (Kind/云端)
#   2. 添加 Helm 仓库
#   3. 使用 Helm 安装 devtron-operator
#   4. 等待 Secret 创建并显示访问信息
#
# 注意:
#   - 不使用 --wait 参数，避免镜像拉取超时导致安装失败
#   - 首次安装需要拉取约 20 个镜像，耗时 10-30 分钟
#   - Kind 集群可能需要配置代理才能拉取 quay.io 镜像
install_devtron() {
    log_info "安装 Devtron..."
    check_prerequisites

    # 检测 Kind 集群环境
    # Kind 节点名称包含 "control-plane"，以此判断是否为 Kind 集群
    if kubectl get nodes -o jsonpath='{.items[0].metadata.name}' 2>/dev/null | grep -q "control-plane"; then
        log_info "检测到 Kind 集群环境"
        KIND_CLUSTER=$(kubectl config current-context | sed 's/kind-//')
        log_info "Kind 集群名称: ${KIND_CLUSTER}"
        log_info "提示: Kind 集群如遇镜像拉取问题，可配置 containerd 代理"
    fi

    # 添加 Devtron 官方 Helm 仓库
    log_info "添加 Devtron Helm 仓库..."
    helm repo add devtron https://helm.devtron.ai --force-update 2>/dev/null || true
    helm repo update

    # 检查是否已安装，避免重复安装
    if helm status devtron -n ${DEVTRON_NAMESPACE} &>/dev/null; then
        log_info "Devtron 已安装，使用 upgrade 命令升级或 uninstall 命令卸载"
        check_status
        return 0
    fi

    # 使用 Helm 安装 Devtron
    # --set installer.modules={cicd} : 安装 CI/CD 模块
    # --timeout=1800s : 设置超时时间为 30 分钟
    # 不使用 --wait : 避免镜像拉取慢导致 Helm 报错
    log_info "使用 Helm 安装 Devtron..."
    helm install devtron devtron/devtron-operator \
        --create-namespace \
        --namespace ${DEVTRON_NAMESPACE} \
        --set installer.modules={cicd} \
        --timeout=1800s

    log_success "Helm 安装命令已执行"
    log_info "正在等待核心组件启动..."
    log_info "提示: 首次安装需要拉取镜像，可能需要 10-30 分钟"
    log_info "运行 '$0 status' 查看状态"
    log_info "运行 '$0 images' 检查镜像拉取问题"
    echo ""
    get_dashboard_url
    echo ""

    # 等待密码 Secret 创建 (最多等待 60 秒)
    log_info "等待密码 Secret 创建..."
    for i in {1..30}; do
        if kubectl get secret devtron-secret -n ${DEVTRON_NAMESPACE} &>/dev/null; then
            get_admin_password
            break
        fi
        sleep 2
    done

    # 设置 git-sensor 加密密钥
    setup_git_sensor_encryption_key

    # 安装 Argo Rollouts (Devtron Rollout Deployment chart 需要)
    install_argo_rollouts

    # 设置 url attribute (CI 触发必需)
    setup_url_attribute
}

# 获取 Dashboard 访问地址
# 通过 kubectl port-forward 将服务暴露到本地 30080 端口
get_dashboard_url() {
    log_info "Dashboard 访问方式:"
    echo "  1. 启动端口转发:"
    echo "     kubectl port-forward svc/devtron-service -n ${DEVTRON_NAMESPACE} 30080:80"
    echo "  2. 浏览器访问: http://localhost:30080"
    echo "  3. 用户名: admin"
}

# 获取管理员密码
# 密码存储在 devtron-secret 的 ADMIN_PASSWORD 字段中 (Base64 编码)
# 注意: 密码在数据库迁移完成后才会生成
get_admin_password() {
    log_info "管理员密码:"
    local password
    password=$(kubectl get secret devtron-secret -n ${DEVTRON_NAMESPACE} -o jsonpath='{.data.ADMIN_PASSWORD}' 2>/dev/null | base64 -d)

    if [ -n "$password" ]; then
        echo "$password"
    else
        log_info "密码尚未生成，请等待 Devtron 服务完全启动"
        log_info "运行 '$0 status' 检查 Pod 状态"
        log_info "所有 Pod 为 Running 后再次运行 '$0 password'"
    fi
}

# 安装 Argo Rollouts
# Devtron 的 Rollout Deployment chart 使用 Argo Rollouts CRD
# 不安装会导致 CD 部署报错: no matches for kind "Rollout"
install_argo_rollouts() {
    log_info "检查 Argo Rollouts..."

    if kubectl get namespace argo-rollouts &>/dev/null; then
        log_info "Argo Rollouts 已安装"
        return 0
    fi

    log_info "安装 Argo Rollouts..."
    kubectl create namespace argo-rollouts 2>/dev/null || true
    kubectl apply -n argo-rollouts -f https://github.com/argoproj/argo-rollouts/releases/latest/download/install.yaml
    log_success "Argo Rollouts 安装完成"
}

# 设置 url attribute
# Devtron CI 触发时从 attributes 表读取 url 键
# 默认安装缺失此记录，会导致 CI 触发 500 panic (nil pointer dereference)
setup_url_attribute() {
    log_info "设置 url attribute..."

    # 获取 PostgreSQL 密码
    local pg_password
    pg_password=$(kubectl get secret postgresql-postgresql -n ${DEVTRON_NAMESPACE} -o jsonpath='{.data.postgresql-password}' 2>/dev/null | base64 -d)
    if [ -z "$pg_password" ]; then
        log_info "PostgreSQL Secret 未就绪，跳过 (可稍后通过 Dashboard 或 API 手动设置)"
        return 0
    fi

    kubectl exec -n ${DEVTRON_NAMESPACE} postgresql-postgresql-0 -c postgres -- \
        env PGPASSWORD="$pg_password" psql -U postgres -d orchestrator -c \
        "INSERT INTO attributes (key, value, active) VALUES ('url', 'http://devtron-service.devtroncd:80', true) ON CONFLICT DO NOTHING;" 2>/dev/null || {
        log_info "url attribute 设置跳过 (数据库可能未就绪)"
        return 0
    }

    log_success "url attribute 已设置"
}

# 升级 Devtron 到最新版本
upgrade_devtron() {
    log_info "升级 Devtron..."
    check_prerequisites

    helm repo update
    helm upgrade devtron devtron/devtron-operator -n ${DEVTRON_NAMESPACE} --set installer.modules={cicd}

    log_success "Devtron 升级完成"
}

# 设置 git-sensor 加密密钥
# git-sensor 需要 AES 密钥来加密 Git 凭据
# 如果密钥缺失，会导致 "crypto/aes: invalid key size 0" 错误
setup_git_sensor_encryption_key() {
    log_info "检查 git-sensor 加密密钥..."

    # 检查密钥是否已存在
    if kubectl get secret git-sensor-secret -n ${DEVTRON_NAMESPACE} -o jsonpath='{.data.ENCRYPTION_KEY}' 2>/dev/null | grep -q .; then
        log_info "git-sensor 加密密钥已存在"
        return 0
    fi

    # 生成随机 AES 密钥 (32 字节，Base64 编码)
    local key
    key=$(openssl rand -base64 32 2>/dev/null) || {
        log_error "无法生成随机密钥，请确保 openssl 已安装"
        return 1
    }

    # 添加到 secret
    kubectl patch secret git-sensor-secret -n ${DEVTRON_NAMESPACE} --type merge -p "{\"data\":{\"ENCRYPTION_KEY\":\"$(echo -n "$key" | base64)\"}}" 2>/dev/null || {
        log_error "设置 git-sensor 加密密钥失败"
        return 1
    }

    log_success "git-sensor 加密密钥已设置"

    # 重启 git-sensor Pod
    kubectl delete pod -n ${DEVTRON_NAMESPACE} -l app=git-sensor --ignore-not-found
    log_info "已重启 git-sensor Pod"
}

# 配置 Seed Cloud 项目
# 创建应用所需的命名空间并应用全局配置
configure_seed_cloud() {
    log_info "配置 Seed Cloud..."
    check_prerequisites

    # 创建各环境命名空间: seed (prod), seed-dev, seed-staging
    for ns in seed seed-dev seed-staging; do
        kubectl create namespace $ns --dry-run=client -o yaml | kubectl apply -f -
    done

    # 应用全局配置 (如果存在)
    kubectl apply -f ${SCRIPT_DIR}/global-config/configmap.yaml -n ${SEED_NAMESPACE} 2>/dev/null || true

    log_success "配置完成"
}

# 查看安装状态
# 显示 Pod、Service 和命名空间信息
check_status() {
    echo "=== Devtron Pods ==="
    kubectl get pods -n ${DEVTRON_NAMESPACE} 2>/dev/null || echo "Devtron 未安装"
    echo ""
    echo "=== Devtron Services ==="
    kubectl get svc -n ${DEVTRON_NAMESPACE} 2>/dev/null || true
    echo ""
    echo "=== 相关命名空间 ==="
    kubectl get ns | grep -E "^seed|^devtron" || true
}

# 卸载 Devtron
# 使用 Helm 卸载并删除命名空间
uninstall_devtron() {
    read -p "确定要卸载 Devtron? [y/N]: " confirm
    [ "$confirm" != "y" ] && exit 0

    log_info "卸载 Devtron..."
    helm uninstall devtron -n ${DEVTRON_NAMESPACE} 2>/dev/null || true
    kubectl delete namespace ${DEVTRON_NAMESPACE} --ignore-not-found
    log_success "Devtron 已卸载"
}

#==============================================================================
# 诊断工具函数
#==============================================================================

# 等待所有 Pod 就绪
# 每 10 秒检查一次，最长等待 30 分钟
# 用于自动化脚本或 CI/CD 流程
wait_for_ready() {
    log_info "等待所有 Pod 就绪 (最长 30 分钟)..."
    local timeout=1800   # 30 分钟超时
    local interval=10    # 每 10 秒检查一次
    local elapsed=0

    while [ $elapsed -lt $timeout ]; do
        local total=$(kubectl get pods -n ${DEVTRON_NAMESPACE} --no-headers 2>/dev/null | wc -l)
        local ready=$(kubectl get pods -n ${DEVTRON_NAMESPACE} --no-headers 2>/dev/null | grep -c "Running\|Completed" || true)
        local pending=$(kubectl get pods -n ${DEVTRON_NAMESPACE} --no-headers 2>/dev/null | grep -c "ImagePullBackOff\|ErrImagePull\|Pending\|Init:" || true)

        log_info "进度: ${ready}/${total} 就绪, ${pending} 等待中 (${elapsed}s)"

        # 所有 Pod 就绪时退出
        if [ "$ready" -eq "$total" ] && [ "$total" -gt 0 ]; then
            log_success "所有 Pod 已就绪!"
            return 0
        fi

        sleep $interval
        elapsed=$((elapsed + interval))
    done

    log_error "等待超时"
    check_image_status
    return 1
}

# 检查镜像拉取状态
# 显示失败的 Pod 和所需的镜像列表
# 用于诊断 ImagePullBackOff 问题
check_image_status() {
    log_info "检查镜像拉取状态..."
    echo ""
    echo "=== 镜像拉取失败的 Pod ==="
    kubectl get pods -n ${DEVTRON_NAMESPACE} --no-headers 2>/dev/null | grep -E "ImagePullBackOff|ErrImagePull" || echo "无"
    echo ""
    echo "=== 所需镜像列表 ==="
    kubectl get pods -n ${DEVTRON_NAMESPACE} -o jsonpath='{range .items[*]}{range .spec.containers[*]}{.image}{"\n"}{end}{range .spec.initContainers[*]}{.image}{"\n"}{end}{end}' 2>/dev/null | sort -u
    echo ""
    log_info "Kind 集群镜像加载方法:"
    echo "  docker pull <image>"
    echo "  kind load docker-image <image> --name <cluster-name>"
    echo ""
    log_info "Kind 集群代理配置方法:"
    echo "  docker exec <node> mkdir -p /etc/systemd/system/containerd.service.d"
    echo "  # 创建 http-proxy.conf 配置 HTTP_PROXY/HTTPS_PROXY"
    echo "  docker exec <node> systemctl daemon-reload"
    echo "  docker exec <node> systemctl restart containerd"
}

#==============================================================================
# 主入口 - 根据命令行参数执行对应函数
#==============================================================================
case "${1:-help}" in
    install)   install_devtron ;;       # 安装
    configure) configure_seed_cloud ;;  # 配置项目
    setup-keys) setup_git_sensor_encryption_key ;;  # 设置密钥
    status)    check_status ;;          # 查看状态
    password)  get_admin_password ;;    # 获取密码
    dashboard) get_dashboard_url ;;     # Dashboard 地址
    upgrade)   upgrade_devtron ;;       # 升级
    uninstall) uninstall_devtron ;;     # 卸载
    wait)      wait_for_ready ;;        # 等待就绪
    images)    check_image_status ;;    # 检查镜像
    *)         show_help ;;             # 显示帮助
esac

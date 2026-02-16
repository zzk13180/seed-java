# Devtron CI/CD 部署指南

使用 [Devtron](https://devtron.ai/) 在 Kind 本地集群中搭建 Seed Cloud 的 CI/CD 流水线。

> 以下步骤已在 macOS (Apple Silicon) + Kind v0.31.0 + Devtron Helm Chart 0.23.0 环境下完整验证。

## 前置要求

| 工具 | 最低版本 | 安装 |
|------|----------|------|
| Docker | 24+ | [docker.com](https://docs.docker.com/get-docker/) |
| Kind | 0.20+ | `brew install kind` |
| kubectl | 1.28+ | `brew install kubectl` |
| Helm | 3.12+ | `brew install helm` |

## 快速开始

```bash
# 1. 创建 Kind 集群
kind create cluster --name devtron --config kind-config.yaml

# 2. 启动本地 Docker Registry (与 Kind 同网络)
docker run -d --restart=always --name kind-registry --network kind -p 5001:5000 registry:2

# 3. 配置 containerd 信任 HTTP Registry (见下方详细步骤)

# 4. 安装 Devtron + Argo Rollouts
bash install-devtron.sh install

# 5. 获取密码 & 访问 Dashboard
bash install-devtron.sh password
kubectl port-forward svc/devtron-service -n devtroncd 30080:80
# 浏览器: http://localhost:30080  用户名: admin
```

## 配置 containerd 信任 HTTP Registry

Kind 节点的 containerd 默认用 HTTPS；本地 Registry 只有 HTTP，需手动配置：

```bash
docker exec -it devtron-control-plane bash

mkdir -p /etc/containerd/certs.d/kind-registry:5000
cat > /etc/containerd/certs.d/kind-registry:5000/hosts.toml << 'EOF'
server = "http://kind-registry:5000"

[host."http://kind-registry:5000"]
  capabilities = ["pull", "resolve", "push"]
  skip_verify = true
EOF

systemctl restart containerd
exit

kubectl get nodes  # 等待 Ready
```

## 安装后必需配置

### 设置 `url` attribute

Devtron CI 触发依赖 `attributes` 表的 `url` 键。默认安装缺失，**不设置会导致 CI 触发 500 panic**。

```bash
# 通过 PostgreSQL 直接插入 (PG 密码从 postgresql-postgresql secret 获取)
kubectl exec -n devtroncd postgresql-postgresql-0 -c postgres -- \
  env PGPASSWORD=<pg-password> psql -U postgres -d orchestrator -c \
  "INSERT INTO attributes (key, value, active) VALUES ('url', 'http://devtron-service.devtroncd:80', true) ON CONFLICT DO NOTHING;"
```

### 创建命名空间

```bash
kubectl create namespace seed-dev
kubectl create namespace seed-staging
kubectl create namespace seed
```

### 添加 Git Provider (Dashboard: Global Configuration → Git Accounts)

```bash
curl -X POST http://localhost:30080/orchestrator/git/provider \
  -H "token: $TOKEN" -H "Content-Type: application/json" \
  -d '{
    "name": "Local Git",
    "url": "http://host.docker.internal:3000/",
    "authMode": "USERNAME_PASSWORD",
    "userName": "<username>", "password": "<password>",
    "active": true
  }'
```

> Kind 节点访问宿主机服务通过 `host.docker.internal`。

### 添加 Docker Registry

```bash
curl -X POST http://localhost:30080/orchestrator/docker/registry \
  -H "token: $TOKEN" -H "Content-Type: application/json" \
  -d '{
    "id": "kind-registry",
    "pluginId": "artifact-registry",
    "registryType": "other",
    "registryUrl": "http://kind-registry:5000",
    "isDefault": true,
    "connection": "INSECURE",
    "username": "anonymous", "password": "anonymous",
    "ipsConfig": {"ignoredImages": []},
    "active": true
  }'
```

> Registry 即使无认证，`username`/`password` 也必须设置（否则 CI 报 `non TTY` 登录错误）。

## 创建 CI/CD 流水线

### 应用配置参考

| 应用 | Dockerfile | 端口 | 构建参数 |
|------|-----------|------|---------|
| seed-frontend | `apps/frontend/Dockerfile` | 80 | — |
| seed-gateway | `ops/deployment/docker/Dockerfile` | 8080 | `MODULE=apps/platform/gateway` |
| seed-auth | `ops/deployment/docker/Dockerfile` | 9100 | `MODULE=apps/domains/identity/auth-service` |
| seed-system | `ops/deployment/docker/Dockerfile` | 9200 | `MODULE=apps/domains/administration/system-service` |

### CI Pipeline 关键点

- `ciBuildType` 必须用 `"self-dockerfile-build"`（不是 `"self-dockerfile-build-type"`）
- Git Material 的 `checkoutPath` 设为 `"./"`
- Java 服务需要设置 `dockerBuildConfig.buildArgs`

### Deployment Template 关键点

- 使用 `Rollout Deployment` chart（需要 Argo Rollouts）
- 前端需设置 `EnvVariables: BACKEND_URL=<gateway-address>:8080`
- Java 服务需设置 `EnvVariables: SPRING_PROFILES_ACTIVE`, `NACOS_SERVER_ADDR`, `JAVA_OPTS` 等

### CD Pipeline 配置

| 环境 | 命名空间 | 触发方式 | 策略 |
|------|----------|----------|------|
| dev | seed-dev | AUTOMATIC | ROLLING |
| staging | seed-staging | MANUAL | ROLLING |
| prod | seed | MANUAL | BLUE_GREEN |

## 脚本命令

```bash
bash install-devtron.sh <命令>
```

| 命令 | 说明 |
|------|------|
| `install` | 安装 Devtron + Argo Rollouts + 设置加密密钥 |
| `status` | 查看 Pod / Service 状态 |
| `password` | 获取管理员密码 |
| `dashboard` | 获取 Dashboard 访问方式 |
| `wait` | 等待所有 Pod 就绪 (最长 30 分钟) |
| `images` | 检查镜像拉取状态 |
| `upgrade` | 升级到最新版本 |
| `uninstall` | 卸载并清理资源 |
| `configure` | 创建 Seed Cloud 命名空间 |
| `setup-keys` | 设置 git-sensor 加密密钥 |

## 故障排除

| 错误 | 原因 | 修复 |
|------|------|------|
| `ImagePullBackOff: http: server gave HTTP response to HTTPS client` | containerd 对 HTTP Registry 用 HTTPS | 配置 `hosts.toml`，见上方 |
| CI 触发 500 `nil pointer dereference` | `attributes` 表缺 `url` 键 | INSERT url attribute |
| CI Docker build `"/package.json": not found` | `.dockerignore` 排除了 Node.js 文件 | 更新 `.dockerignore` |
| `Cannot perform an interactive login from a non TTY device` | Registry 空 credentials | 设置 anonymous/anonymous |
| nginx `host not found in upstream` | `BACKEND_URL` 未设值 | 设置 EnvVariables |
| `no matches for kind "Rollout"` | Argo Rollouts 未安装 | `install-devtron.sh install` 自动安装 |
| `crypto/aes: invalid key size 0` | git-sensor 缺加密密钥 | `install-devtron.sh setup-keys` |
| 密码为空 | 数据库迁移未完成 | 等待所有 Pod Running 后重试 |

## 目录结构

```
devtron/
├── kind-config.yaml      # Kind 集群配置 (端口映射 + containerd registry)
├── install-devtron.sh    # 安装管理脚本
├── README.md             # 本文档
└── global-config/        # K8s ConfigMap / Secret 模板
    ├── configmap.yaml
    └── secrets.yaml
```

## 常用命令

```bash
# Devtron Pod 状态
kubectl get pods -n devtroncd

# CI Pod 监控
kubectl get pods -n devtron-ci -w

# 应用部署状态
kubectl get pods,svc,rollout -n seed-dev

# 获取管理员密码
kubectl -n devtroncd get secret devtron-secret -o jsonpath='{.data.ADMIN_PASSWORD}' | base64 -d

# Dashboard 端口转发
kubectl port-forward svc/devtron-service -n devtroncd 30080:80

# Registry 镜像列表
curl -s http://localhost:5001/v2/_catalog
```

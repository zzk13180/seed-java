# Seed Cloud 企业级微服务种子项目

企业级微服务种子项目 — Java 21 / Spring Boot 3.4.1 / Spring Cloud 2024.0.0 / Vue 3.5 / TypeScript 5.8

## 目录
- [Seed Cloud 企业级微服务种子项目](#seed-cloud-企业级微服务种子项目)
  - [目录](#目录)
  - [技术栈与依赖](#技术栈与依赖)
  - [环境要求](#环境要求)
  - [快速开始](#快速开始)
  - [架构全景与项目结构](#架构全景与项目结构)
    - [目录结构](#目录结构)
    - [后端服务拓扑](#后端服务拓扑)
    - [认证架构：双模式策略](#认证架构双模式策略)
    - [前端架构](#前端架构)
  - [共享库 (libs)](#共享库-libs)
  - [常用命令](#常用命令)
  - [部署与基础设施](#部署与基础设施)
    - [基础设施服务 (`pnpm infra`)](#基础设施服务-pnpm-infra)
    - [环境变量](#环境变量)
    - [Docker Compose (本地/测试)](#docker-compose-本地测试)
    - [Kubernetes (Devtron CI/CD)](#kubernetes-devtron-cicd)
  - [测试策略](#测试策略)
    - [后端（七层测试金字塔）](#后端七层测试金字塔)
    - [前端测试](#前端测试)
    - [覆盖率门槛](#覆盖率门槛)
  - [代码质量](#代码质量)
    - [后端工具链](#后端工具链)
    - [前端](#前端)
  - [安全](#安全)
    - [✅ 已实现](#-已实现)
    - [⚠️ 已知限制](#️-已知限制)
  - [API 示例](#api-示例)
  - [故障排除与环境清理](#故障排除与环境清理)
    - [故障排除](#故障排除)
    - [环境清理](#环境清理)
  - [项目规模与模块依赖](#项目规模与模块依赖)
    - [模块依赖关系](#模块依赖关系)

## 技术栈与依赖

| 层级 | 技术 |
|------|------|
| 后端 | Java 21, Spring Boot 3.4.1, Spring Cloud Gateway, MyBatis-Plus 3.5.9 |
| 前端 | Vue 3.5.13, TypeScript 5.8.3, Vite 7.0.0, Element Plus 2.11.1, Tailwind CSS 4.1.10, Pinia 3.0.3 |
| 认证 | Sa-Token 1.39.0 (可切换 OAuth2/OIDC) |
| 基础设施 | Nacos v2.4.3, Redis 7, PostgreSQL 15, MinIO 8.6.0, Sentinel 1.8.6 |
| 构建 | Nx 22.4.4, Maven 3.9+, pnpm |
| 部署 | Docker Compose, Devtron (K8s CI/CD) |

## 环境要求

| 工具 | 最低版本 |
|------|----------|
| Node.js | 18+ |
| pnpm | 10+ |
| Java JDK | 21+ |
| Maven | 3.9+ |
| Docker | 24+ |

## 快速开始

```bash
# 1. 克隆并安装依赖
git clone <repo-url> && cd seed-cloud
pnpm install

# 2. 启动基础设施 (PostgreSQL + Redis + Nacos + MinIO + Sentinel)
cp ops/deployment/docker/.env.example ops/deployment/docker/.env
pnpm infra

# 3. 启动后端
pnpm dev:backend   # 或单独: pnpm nx dev gateway

# 4. 启动前端
pnpm dev:frontend
```

访问 http://localhost:5173，使用 `admin / admin123` 登录。

## 架构全景与项目结构

### 目录结构

```
seed-cloud/
├── apps/
│   ├── frontend/                # Vue 3 SPA（Vite 7 + Element Plus）(:5173)
│   ├── domains/
│   │   ├── identity/auth-service/       # 认证服务 (dev :8081 / Docker :9100)
│   │   └── administration/system-service/  # 系统管理 (dev :8082 / Docker :9200)
│   └── platform/gateway/               # API 网关 (:8080)
├── libs/
│   ├── core/                    # 基础抽象：常量/异常/DTO/SPI（28 个 Java 文件）
│   ├── infrastructure/          # datasource / redis / rpc
│   ├── components/              # security / web / log / file / swagger
│   └── api/system-api/          # 跨服务 RPC 契约
├── ops/
│   ├── deployment/              # Docker Compose / DB / Devtron / 脚本
│   └── tooling/                 # Checkstyle / PMD / SpotBugs 规则
└── nx.json + pom.xml            # Nx 22.4.4 + Maven 双构建系统
```

### 后端服务拓扑

```
          ┌────────────────────────────┐
          │   Gateway (:8080)          │
          │   Spring Cloud Gateway     │
          └─────┬──────────────┬───────┘
                │              │
     ┌──────────▼───┐  ┌──────▼──────────┐
     │ auth-service  │  │ system-service   │
     │ 认证/登录     │──▶│ 用户/角色/菜单   │
     │ (dev :8081   │RPC│ (dev :8082      │
     │  Docker :9100)│  │  Docker :9200)  │
     └──────────────┘  └─────────────────┘
                │              │
     ┌──────────▼──────────────▼──────────┐
     │     libs/ 共享库层                  │
     │ core → infrastructure → components │
     └────────────────────────────────────┘
```

> 注：本地开发端口为 8081/8082（`application.yml`），Docker 部署时通过 Dockerfile 的 `-Dserver.port=${PORT}` 覆盖为 9100/9200。
> **路由规则**：`/auth/**` → seed-auth，`/system/**` → seed-system
> **分层约束 (ArchUnit)**：Controller → Service → Mapper → Database，禁止反向依赖与循环依赖。
> **Java 21 特性**：所有三个服务均启用了虚拟线程（`spring.threads.virtual.enabled: true`）。

### 认证架构：双模式策略

通过 `seed.auth.provider` 配置切换，全链路支持：
- `satoken`（默认）→ Sa-Token 1.39.0 + Redis Session
- `oauth2` → JWT + OIDC (Logto)

开发环境通过 `application-dev.yml` 禁用 OAuth2 自动配置，使用 Sa-Token 本地认证。
安全策略：5 次失败锁定 30 分钟（失败计数 10 分钟重置），IP 限流 60 秒/10 次。

### 前端架构

```
src/
├── api/        → 纯函数式 API 请求层（auth/menu/stats/user 4 个模块）
├── core/       → 自研 fetch HttpClient（拦截器链、重试、超时、401 续期）
│                 + error.service / logger.service / network.service
├── stores/     → Pinia 3 Store（app + user），直连 API，无中间层
├── layout/     → Sidebar + Navbar + Content 经典后台布局（5 组件）
├── pages/      → 路由定义 + ErrorPage
├── views/      → login / dashboard / users(含 UserFormDialog/SearchBar/Table) / profile
├── components/ → SvgIcon / TheLogo
└── utils/      → token 管理 / 工具函数
```

## 共享库 (libs)

| 模块           | 职责                                         | 关键技术                         |
| -------------- | -------------------------------------------- | -------------------------------- |
| **core**       | 基础抽象（ApiResult、BaseEntity、异常、SPI） | 纯 Java（28 文件）                |
| **datasource** | 审计字段填充、分页、动态数据源               | MyBatis-Plus 3.5.9 + Druid 1.2.24 + Dynamic-DS |
| **redis**      | 缓存 + 分布式锁 + 限流                       | Redisson 3.40.2                  |
| **rpc**        | 微服务间调用 + 负载均衡                      | Spring HTTP Interface + Sentinel + LoadBalancer |
| **security**   | 认证鉴权 + 内部调用签名                     | Sa-Token 1.39.0 + OAuth2 双模式 + InnerAuth HMAC |
| **web**        | Web 基础 + 可观测性                          | Micrometer + Zipkin + Actuator   |
| **log**        | 异步操作日志                                 | AOP + Spring Event               |
| **file**       | 文件存储                                    | MinIO 8.6.0 + OkHttp 4.12.0     |
| **swagger**    | API 文档                                     | SpringDoc OpenAPI 2.7.0          |
| **system-api** | 跨服务 RPC 契约                              | `@HttpExchange` + Record DTO（6 Record类） |

**依赖层次**：core → infrastructure → components → api

**核心抽象接口**（libs/core 6 个）：
`AuthProvider` (认证提供者 SPI), `UserContextHolder` (当前用户上下文), `IRepository<T,K>` (仓储抽象), `IBaseService<T,K>` (服务层抽象), `ICache<K,V>` (缓存抽象), `IDistributedLock` (分布式锁抽象)。

## 常用命令

```bash
# 构建
pnpm nx build @seed-cloud/frontend     # 构建前端
pnpm nx run-many -t build              # 构建所有
pnpm nx affected -t build              # 仅构建受影响项目

# 测试
pnpm nx test @seed-cloud/frontend -- --run  # 前端测试 (113 个)
mvn test                               # 后端测试 (52 个)
ops/deployment/scripts/run-tests.sh all     # 完整测试套件

# 代码质量
mvn spotless:apply                     # 自动格式化
mvn checkstyle:check pmd:check spotbugs:check  # 静态分析
pnpm nx lint @seed-cloud/frontend      # 前端 ESLint

# Docker
pnpm infra                             # 启动基础设施
ops/deployment/scripts/docker-dev.sh stop   # 停止服务
ops/deployment/scripts/docker-dev.sh clean  # 清理数据卷
```

## 部署与基础设施

### 基础设施服务 (`pnpm infra`)

| 服务 | 地址 | 凭据 |
|------|------|------|
| Nacos 控制台 | http://localhost:8848/nacos | `nacos / nacos` |
| PostgreSQL | `localhost:5432` | `postgres / postgres` |
| Redis | `localhost:6379` | 无密码 |
| MinIO 控制台 | http://localhost:9001 | `minioadmin / minioadmin123` |
| Sentinel | http://localhost:8858 | `sentinel / sentinel` |

### 环境变量

关键配置（`ops/deployment/docker/.env.example`）：

| 变量 | 说明 |
|------|------|
| `NACOS_AUTH_TOKEN` | Nacos JWT 密钥 (≥32字节 Base64) |
| `NACOS_AUTH_IDENTITY_KEY` | Nacos 身份标识键 |
| `NACOS_AUTH_IDENTITY_VALUE` | Nacos 身份标识值 |
| `POSTGRES_PASSWORD` | PostgreSQL 密码 |

> ⚠️ 生产环境必须修改所有默认密码。

### Docker Compose (本地/测试)

```bash
cd ops/deployment/docker
cp .env.example .env

# 仅基础设施
docker compose --profile infra up -d

# 全栈部署
docker compose --profile app up -d --build
```

### Kubernetes (Devtron CI/CD)

项目集成 [Devtron](https://devtron.ai/) CI/CD 平台，已完成端到端验证。

```bash
# 安装 Devtron
bash ops/deployment/devtron/install-devtron.sh install

# 获取密码
bash ops/deployment/devtron/install-devtron.sh password

# 访问 Dashboard
kubectl port-forward svc/devtron-service -n devtroncd 30080:80
```

**CI/CD 配置摘要**：

| 环境 | 命名空间 | 触发 | 策略 |
|------|----------|------|------|
| dev | `seed-dev` | 自动 | Rolling |
| staging | `seed-staging` | 手动审批 | Rolling |
| prod | `seed` | 双人审批 | Blue-Green |

> 📖 完整配置指南见 [ops/deployment/devtron/README.md](./ops/deployment/devtron/README.md)

## 测试策略

### 后端（七层测试金字塔）

| 层次     | 工具                        | 说明                |
| -------- | --------------------------- | ------------------- |
| 单元测试 | JUnit 5 + Mockito + AssertJ | 默认执行            |
| 集成测试 | Testcontainers (PG+Redis)   | Tag: `integration`  |
| 契约测试 | Pact 4.6.5                  | Tag: `contract`     |
| 架构测试 | ArchUnit 1.2.1              | Tag: `architecture` |
| E2E 测试 | REST-assured 5.4.0          | Tag: `e2e`          |
| 变异测试 | Pitest 1.15.3 (≥60% 变异分数) | —                 |
| 基准测试 | JMH 1.37                    | —                   |

### 前端测试
包含 11 个测试文件，覆盖 core, stores, api, views, layout, components, pages。

### 覆盖率门槛
- **后端**：JaCoCo 行 ≥60% / 分支 ≥50%（`haltOnFailure=true`），Pitest 变异 ≥60% / 覆盖 ≥70%
- **前端**：Lines ≥40%，Branches ≥40%，Functions ≥40%，Statements ≥40%（vitest.config.ts 统一 40%）

## 代码质量

### 后端工具链
- **Spotless 2.44.0**: Google Java Format 1.24.0 + import 排序 + POM/Markdown/YAML/JSON 格式化
- **Checkstyle 10.21.0**: 风格检查（行长100, 圈复杂度≤20）
- **PMD 7.9.0**: 静态分析 + 重复代码检测（CPD, ≥100 token）
- **SpotBugs 4.8.7**: Bug 检测 + FindSecBugs 1.13.0 + sb-contrib 7.6.8 安全扫描
- **JaCoCo 0.8.11**: 覆盖率门槛

### 前端
- **ESLint 9.28.0**：Flat Config + TypeScript 类型检查 + Vue 规则 + Import 排序
- **vue-tsc 2.2.10**：strict 模式 + noUnusedLocals + noUnusedParameters
- **Vitest 3.2.4**：happy-dom 环境 + V8 coverage

## 安全

### ✅ 已实现
- BCrypt 密码加密
- 暴力破解防护（5 次锁定 30 分钟）+ IP 限流（60 秒 10 次）
- Sa-Token / OAuth2 Token 管理（HttpOnly Cookie + Bearer Header 双模式）
- 生产密码 Vault 管理（Devtron ExternalSecret → Vault）
- 非 root 容器运行（用户 `seed`）
- HSTS 启用（含 preload，31536000 秒）
- `@InnerAuth` HMAC-SHA256 签名验证（共享密钥 + 时间戳防重放）
- 密码本地验证，通过 `getUserCredentials` 获取 BCrypt 哈希后在 auth-service 本地验证，不通过 RPC 传输明文密码
- CSP 强化（script-src 'self'，无 unsafe-inline/unsafe-eval；base-uri / form-action / object-src / upgrade-insecure-requests / Permissions-Policy）
- `.gitignore` 全局通配排除私钥文件（*.key / *.pem / *.p12 / *.keystore 等）
- Vue 模板禁止 `v-html`（ESLint `vue/no-v-html: error`）
- 全局错误处理（Vue errorHandler + unhandledrejection）
- 安全扫描集成（FindSecBugs + OWASP Dependency Check in CI）

### ⚠️ 已知限制
- CSP `style-src` 含 `'unsafe-inline'`（Element Plus 运行时动态样式注入的已知需求，仅影响 CSS，不影响脚本安全）
- 生产环境需配置 `INNER_AUTH_SECRET` 环境变量（开发环境使用默认值 `dev-inner-auth-secret-change-in-production`）
- 建议生产环境启用服务间 mTLS 作为纵深防御
- Nacos 认证在开发环境通过环境变量 `NACOS_AUTH_TOKEN` 等配置，docker-compose 中标记为 `required`

## API 示例

```bash
# 登录
curl -X POST http://localhost:8080/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"username":"admin","password":"admin123"}'

# 获取用户信息
curl http://localhost:8080/system/user/profile \
  -H 'Authorization: Bearer <token>'
```

## 故障排除与环境清理

### 故障排除
| 问题 | 解决 |
|------|------|
| Nacos 管理员未初始化 | `pnpm infra` 自动处理，或手动 POST `/nacos/v1/auth/users/admin` |
| Sentinel 容器 unhealthy | ARM 架构模拟运行正常现象，不影响功能 |
| Netty DNS 警告 | macOS 已知问题，不影响功能 |
| 端口占用 | `lsof -tiTCP:8080 -sTCP:LISTEN \| xargs kill` |

### 环境清理
```bash
# 完全重置
cd ops/deployment/docker && docker compose --profile infra --profile app down -v && cd ../../..
mvn clean && rm -rf .nx/cache node_modules
pnpm install && cp ops/deployment/docker/.env.example ops/deployment/docker/.env
pnpm infra
```

## 项目规模与模块依赖

| 维度              | 数量 |
| ----------------- | ---- |
| Java 源文件       | 105  |
| Java 测试文件     | 36   |
| 前端 TS/Vue 源文件 | 36  |
| 前端测试文件      | 11   |
| Maven 模块        | 13   |
| 数据库表          | 8    |
| Docker Compose 服务 | 9（5 基础设施 + 3 后端 + 1 前端） |
| API 端点（system-api） | 5（@HttpExchange） |

### 模块依赖关系

```
              common-core
         ┌────────┼────────┐
    datasource   redis    rpc
         └────────┼────────┘
              components
           (security/web/log/file/swagger)
                  │
             system-api
            ┌─────┴──────┐
       auth-service  system-service
            └─────┬──────┘
              gateway
                 ↑
             frontend
```

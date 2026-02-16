# Pact 契约测试指南

## 概述

本项目使用 [Pact](https://pact.io/) 框架（v4.6.5）进行微服务间的**契约测试 (Contract Testing)**，确保 `auth-service`（消费者）与 `system-service`（提供者）之间的 API 接口契约始终一致。

## 什么是 Pact 契约测试？

### 核心概念

```
┌─────────────────┐                          ┌─────────────────┐
│   auth-service   │  ── HTTP 调用 ──>       │  system-service  │
│   (Consumer)     │  GET /user/info/{name}   │   (Provider)     │
│                  │  POST /user/oauth2       │                  │
└─────────────────┘                          └─────────────────┘
        │                                            │
   1. Consumer 测试                            3. Provider 验证
   生成 Pact 文件（契约）                       根据 Pact 文件验证
        │                                     自身 API 是否满足契约
        ▼                                            ▲
┌─────────────────────────────────────────────────────┐
│              Pact 文件 (JSON)                        │
│  描述了 Consumer 期望的请求格式和响应结构              │
│  存放位置: src/test/resources/pacts/*.json           │
└─────────────────────────────────────────────────────┘
```

### 工作流程（两阶段）

| 阶段 | 角色 | 做什么 | 产物 |
|------|------|--------|------|
| **1. Consumer 测试** | auth-service | 定义"我期望 Provider 提供怎样的 API"，Pact 框架启动 Mock Server 模拟 Provider | `target/pacts/auth-service-system-service.json` |
| **2. Provider 验证** | system-service | 读取 Pact 文件，逐一回放 Consumer 定义的请求，验证真实 API 返回是否匹配 | 测试通过/失败 |

### 与传统集成测试的区别

| 维度 | 集成测试 | Pact 契约测试 |
|------|---------|--------------|
| 依赖 | 需要两个服务同时运行 | Consumer 和 Provider **独立运行** |
| 速度 | 慢（需启动完整环境） | 快（Consumer 用 Mock，Provider 单独启动） |
| 发现问题 | 部署后才发现 | 开发阶段即可发现接口不兼容 |
| 维护 | 容易遗忘更新 | **契约即文档**，强制双方同步 |

## 项目配置

### 依赖（已在父 POM 统一管理版本）

```xml
<!-- Consumer 端 (auth-service/pom.xml) -->
<dependency>
  <groupId>au.com.dius.pact.consumer</groupId>
  <artifactId>junit5</artifactId>
  <scope>test</scope>
</dependency>

<!-- Provider 端 (system-service/pom.xml) -->
<dependency>
  <groupId>au.com.dius.pact.provider</groupId>
  <artifactId>junit5</artifactId>
  <scope>test</scope>
</dependency>
```

### 目录结构

```
apps/domains/
├── identity/auth-service/                      # Consumer（消费者）
│   └── src/test/java/.../contract/
│       ├── AuthSystemContractTest.java          # 用户查询契约
│       └── AuthSystemUserCreateContractTest.java # 用户创建契约
│
└── administration/system-service/               # Provider（提供者）
    ├── pacts/                                   # 根目录 pacts（可用于 CI/CD 共享）
    └── src/test/
        ├── java/.../contract/
        │   ├── SystemProviderContractTest.java          # 用户查询验证
        │   └── SystemProviderUserCreateContractTest.java # 用户创建验证
        └── resources/pacts/
            └── auth-service-system-service.json  # Pact 契约文件
```

## 使用方法

### 第一步：运行 Consumer 测试（生成 Pact 文件）

```bash
# 在项目根目录执行
mvn test -Dtest.groups="contract" -Dtest.excludedGroups="" \
    -pl apps/domains/identity/auth-service -B
```

运行成功后，Pact 文件会自动生成到：
```
apps/domains/identity/auth-service/target/pacts/auth-service-system-service.json
```

### 第二步：复制 Pact 文件到 Provider

```bash
# 将生成的 Pact 文件复制到 Provider 的测试资源目录
cp apps/domains/identity/auth-service/target/pacts/auth-service-system-service.json \
   apps/domains/administration/system-service/src/test/resources/pacts/
```

> **提示：** 在正式项目中，通常使用 [Pact Broker](https://docs.pact.io/pact_broker) 自动共享 Pact 文件，无需手动复制。

### 第三步：运行 Provider 验证

```bash
# 需要先启动测试环境（数据库等）
# docker compose --profile test up -d

mvn test -Dtest.groups="contract" -Dtest.excludedGroups="" \
    -pl apps/domains/administration/system-service -B
```

### 快捷方式：使用项目脚本

```bash
# 运行所有契约测试
./ops/deployment/scripts/run-tests.sh contract
```

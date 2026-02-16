#!/bin/bash

# ============================================================
# Seed Cloud 测试运行脚本
# ============================================================

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 打印带颜色的消息
print_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# 帮助信息
show_help() {
    echo "Seed Cloud 测试运行脚本"
    echo ""
    echo "用法: $0 [命令]"
    echo ""
    echo "命令:"
    echo "  unit          运行单元测试"
    echo "  integration   运行集成测试"
    echo "  contract      运行契约测试"
    echo "  e2e           运行 E2E 测试"
    echo "  architecture  运行架构合规性测试"
    echo "  mutation      运行变异测试 (评估测试质量)"
    echo "  all           运行所有测试"
    echo "  coverage      运行测试并生成覆盖率报告"
    echo "  benchmark     运行性能基准测试"
    echo "  help          显示帮助信息"
    echo ""
    echo "示例:"
    echo "  $0 unit        # 只运行单元测试"
    echo "  $0 coverage    # 运行测试并生成覆盖率报告"
    echo "  $0 mutation    # 运行变异测试"
    echo ""
}

# 获取项目根目录
get_project_root() {
    local script_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
    echo "$(cd "$script_dir/../../.." && pwd)"
}

# 运行单元测试
run_unit_tests() {
    print_info "运行单元测试..."
    cd "$(get_project_root)"
    # 使用默认配置，排除 integration,e2e,contract,architecture
    mvn test -B
    print_success "单元测试完成"
}

# 运行集成测试
run_integration_tests() {
    print_info "运行集成测试..."
    print_info "确保 Docker 正在运行（Testcontainers 需要）"
    cd "$(get_project_root)"

    # 检查 Docker 是否可用
    if ! docker info > /dev/null 2>&1; then
        print_error "Docker 未运行，集成测试需要 Docker (Testcontainers)"
        exit 1
    fi

    # 使用属性覆盖: 只运行 integration 标记的测试
    mvn test -Dtest.groups="integration" -Dtest.excludedGroups="" -B
    print_success "集成测试完成"
}

# 运行契约测试
run_contract_tests() {
    print_info "运行契约测试..."
    cd "$(get_project_root)"
    # 使用属性覆盖: 只运行 contract 标记的测试
    mvn test -Dtest.groups="contract" -Dtest.excludedGroups="" -B
    print_success "契约测试完成"

    # 显示生成的 Pact 文件
    print_info "Pact 文件位置:"
    find . -name "*.json" -path "*/pacts/*" 2>/dev/null || echo "未找到 Pact 文件"
}

# 运行 E2E 测试
run_e2e_tests() {
    print_info "运行 E2E 测试..."

    SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
    DOCKER_DIR="$SCRIPT_DIR/../docker"
    COMPOSE_FILE="$DOCKER_DIR/docker-compose.yml"
    COMPOSE_TEST_FILE="$DOCKER_DIR/docker-compose.test.yml"

    if [ -f "$COMPOSE_FILE" ] && [ -f "$COMPOSE_TEST_FILE" ]; then
        print_info "启动测试环境 (使用 test profile)..."
        docker compose -f "$COMPOSE_FILE" -f "$COMPOSE_TEST_FILE" --profile test up -d --build

        print_info "等待服务就绪..."
        # 等待 gateway 健康检查通过
        timeout 120 bash -c 'until curl -sf http://localhost:18080/actuator/health; do sleep 5; done' || {
            print_error "服务启动超时"
            docker compose -f "$COMPOSE_FILE" -f "$COMPOSE_TEST_FILE" --profile test logs
            docker compose -f "$COMPOSE_FILE" -f "$COMPOSE_TEST_FILE" --profile test down -v
            exit 1
        }

        print_info "运行 E2E 测试..."
        GATEWAY_URL="http://localhost:18080" mvn test -Dtest.groups="e2e" -Dtest.excludedGroups="" -B || TEST_RESULT=$?

        print_info "停止测试环境..."
        docker compose -f "$COMPOSE_FILE" -f "$COMPOSE_TEST_FILE" --profile test down -v

        if [ "${TEST_RESULT:-0}" -ne 0 ]; then
            print_error "E2E 测试失败"
            exit $TEST_RESULT
        fi
    else
        print_warning "Docker Compose 配置文件不存在，直接运行测试..."
        mvn test -Dgroups="e2e" -B
    fi

    print_success "E2E 测试完成"
}

# 运行架构测试
run_architecture_tests() {
    print_info "运行架构合规性测试..."
    cd "$(get_project_root)"
    # 使用属性覆盖: 只运行 architecture 标记的测试
    mvn test -Dtest.groups="architecture" -Dtest.excludedGroups="" -B
    print_success "架构测试完成"
}

# 运行所有测试 (不包含 E2E，E2E 需要单独运行)
run_all_tests() {
    print_info "运行所有测试 (不含 E2E)..."
    print_warning "E2E 测试需要完整的服务栈，请单独运行: $0 e2e"
    echo ""

    local failed=0

    run_unit_tests || failed=1

    if [ $failed -eq 0 ]; then
        run_contract_tests || failed=1
    fi

    if [ $failed -eq 0 ]; then
        run_architecture_tests || failed=1
    fi

    if [ $failed -eq 0 ]; then
        run_integration_tests || failed=1
    fi

    if [ $failed -ne 0 ]; then
        print_error "部分测试失败"
        exit 1
    fi

    print_success "所有测试完成"
}

# 运行测试并生成覆盖率报告
run_coverage() {
    print_info "运行测试并生成覆盖率报告..."
    cd "$(get_project_root)"

    # 运行单元测试和集成测试，生成覆盖率
    mvn clean verify -DexcludedGroups="e2e" -B

    print_success "覆盖率报告生成完成"
    print_info "报告位置:"
    find . -name "index.html" -path "*/jacoco/*" 2>/dev/null | while read report; do
        echo "  - $report"
    done

    # 尝试打开报告
    FIRST_REPORT=$(find . -name "index.html" -path "*/jacoco/*" 2>/dev/null | head -1)
    if [ -n "$FIRST_REPORT" ]; then
        print_info "提示: 使用以下命令打开报告:"
        echo "  open $FIRST_REPORT"
    fi
}

# 运行变异测试
run_mutation_tests() {
    print_info "运行变异测试 (Mutation Testing)..."
    print_warning "变异测试需要较长时间，因为会生成大量代码变体"
    echo ""
    print_info "变异测试说明:"
    echo "  - 变异测试通过在代码中引入微小错误来评估测试质量"
    echo "  - 变异分数 = 被杀死的变异体 / 总变异体 × 100%"
    echo "  - 分数越高，测试质量越好"
    echo ""

    cd "$(get_project_root)"

    # 先编译项目
    mvn clean test-compile -DskipTests -B

    # 运行 Pitest
    mvn org.pitest:pitest-maven:mutationCoverage -B

    MUTATION_REPORT=$(find . -name "index.html" -path "*/pit-reports/*" 2>/dev/null | head -1)

    if [ -n "$MUTATION_REPORT" ]; then
        print_success "变异测试完成"
        print_info "变异测试报告位置:"
        echo "  $MUTATION_REPORT"
        echo ""
        print_info "可以在浏览器中打开报告查看详细结果:"
        echo "  open $MUTATION_REPORT"
    else
        print_warning "未找到变异测试报告，请检查输出日志"
    fi
}

# 运行性能基准测试
run_benchmark() {
    print_info "运行性能基准测试..."
    print_warning "基准测试可能需要较长时间"

    mvn clean install -DskipTests -B

    # 查找并运行基准测试
    if [ -f "target/benchmarks.jar" ]; then
        java -jar target/benchmarks.jar
    else
        print_warning "未找到 benchmarks.jar，请先配置 JMH 打包插件"
        print_info "可以单独运行基准测试类:"
        echo "  mvn exec:java -Dexec.mainClass=\"com.zhangzhankui.seed.common.benchmark.StringOperationsBenchmark\""
    fi

    print_success "性能基准测试完成"
}

# 主入口
main() {
    case "$1" in
        unit)
            run_unit_tests
            ;;
        integration)
            run_integration_tests
            ;;
        contract)
            run_contract_tests
            ;;
        e2e)
            run_e2e_tests
            ;;
        architecture)
            run_architecture_tests
            ;;
        mutation)
            run_mutation_tests
            ;;
        all)
            run_all_tests
            ;;
        coverage)
            run_coverage
            ;;
        benchmark)
            run_benchmark
            ;;
        help|--help|-h)
            show_help
            ;;
        *)
            print_error "未知命令: $1"
            show_help
            exit 1
            ;;
    esac
}

# 如果没有参数，显示帮助
if [ $# -eq 0 ]; then
    show_help
    exit 0
fi

main "$@"

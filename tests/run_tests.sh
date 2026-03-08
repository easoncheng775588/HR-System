#!/bin/bash
# 测试运行脚本 - 运行所有测试用例

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 项目根目录
PROJECT_ROOT="/Users/silver/Vibe Coding/HR System"
TESTS_DIR="$PROJECT_ROOT/tests"
VENV_DIR="$TESTS_DIR/venv"

# 进入测试目录
cd "$TESTS_DIR" || exit 1

# 激活虚拟环境
if [ -d "$VENV_DIR" ]; then
    source "$VENV_DIR/bin/activate"
else
    echo -e "${RED}错误: 虚拟环境不存在，请先创建虚拟环境${NC}"
    echo -e "${YELLOW}运行: python3 -m venv venv${NC}"
    exit 1
fi

echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}HR系统测试执行脚本${NC}"
echo -e "${GREEN}========================================${NC}"
echo ""

# 检查后端服务
echo -e "${YELLOW}检查后端服务...${NC}"
if ! curl -s http://localhost:8080/api/health &> /dev/null; then
    echo -e "${RED}警告: 后端服务未运行，部分API测试可能会失败${NC}"
    echo -e "${YELLOW}提示: 请先启动后端服务: cd backend/recruitment-system && mvn spring-boot:run${NC}"
fi

# 检查前端服务
echo -e "${YELLOW}检查前端服务...${NC}"
if ! curl -s http://localhost:5173 &> /dev/null; then
    echo -e "${RED}警告: 前端服务未运行，UI测试可能会失败${NC}"
    echo -e "${YELLOW}提示: 请先启动前端服务: cd frontend && npm run dev${NC}"
fi

echo ""
echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}开始执行测试${NC}"
echo -e "${GREEN}========================================${NC}"
echo ""

# 测试类型选择
if [ $# -eq 0 ]; then
    echo "请选择测试类型:"
    echo "1. API测试"
    echo "2. UI测试"
    echo "3. 数据一致性测试"
    echo "4. 消息管理测试"
    echo "5. 全部测试"
    echo ""
    read -p "请输入选项 (1-5): " choice
else
    choice=$1
fi

case $choice in
    1)
        echo -e "${YELLOW}执行API测试...${NC}"
        pytest api/ -v --tb=short
        ;;
    2)
        echo -e "${YELLOW}执行UI测试...${NC}"
        pytest ui/ -v --tb=short
        ;;
    3)
        echo -e "${YELLOW}执行数据一致性测试...${NC}"
        pytest api/test_data_consistency.py -v --tb=short
        ;;
    4)
        echo -e "${YELLOW}执行消息管理测试...${NC}"
        pytest api/test_message_management.py -v --tb=short
        ;;
    5)
        echo -e "${YELLOW}执行全部测试...${NC}"
        pytest . -v --tb=short
        ;;
    *)
        echo -e "${RED}无效选项${NC}"
        exit 1
        ;;
esac

# 检查测试结果
if [ $? -eq 0 ]; then
    echo ""
    echo -e "${GREEN}========================================${NC}"
    echo -e "${GREEN}测试执行完成 - 全部通过${NC}"
    echo -e "${GREEN}========================================${NC}"
else
    echo ""
    echo -e "${RED}========================================${NC}"
    echo -e "${RED}测试执行完成 - 存在失败${NC}"
    echo -e "${RED}========================================${NC}"
    exit 1
fi
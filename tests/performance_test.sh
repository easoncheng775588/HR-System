#!/bin/bash
# 测试性能比较脚本

echo "=========================================="
echo "测试性能比较"
echo "=========================================="
echo ""

# 激活虚拟环境
source venv/bin/activate

# 1. 顺序执行所有测试
echo "1. 顺序执行所有测试..."
echo "=========================================="
START_TIME=$(date +%s)
pytest api/test_data_consistency.py -v --tb=short
END_TIME=$(date +%s)
SEQUENTIAL_TIME=$((END_TIME - START_TIME))
echo ""
echo "顺序执行时间: ${SEQUENTIAL_TIME}秒"
echo ""

# 2. 只并行执行API和数据库测试（不包含UI测试）
echo "2. 并行执行API和数据库测试（不包含UI测试）..."
echo "=========================================="
START_TIME=$(date +%s)
pytest api/test_data_consistency.py -n 2 -v --tb=short -k "not (frontend_display or param_display or approval_status_display or team_field_display or technical_platform_display)"
END_TIME=$(date +%s)
PARALLEL_API_TIME=$((END_TIME - START_TIME))
echo ""
echo "并行执行时间（API和数据库测试）: ${PARALLEL_API_TIME}秒"
echo ""

# 3. 顺序执行UI测试
echo "3. 顺序执行UI测试..."
echo "=========================================="
START_TIME=$(date +%s)
pytest api/test_data_consistency.py -v --tb=short -k "frontend_display or param_display or approval_status_display or team_field_display or technical_platform_display"
END_TIME=$(date +%s)
SEQUENTIAL_UI_TIME=$((END_TIME - START_TIME))
echo ""
echo "顺序执行时间（UI测试）: ${SEQUENTIAL_UI_TIME}秒"
echo ""

# 计算性能提升
echo "=========================================="
echo "性能提升统计"
echo "=========================================="
echo "顺序执行所有测试时间: ${SEQUENTIAL_TIME}秒"
echo "并行执行API和数据库测试时间: ${PARALLEL_API_TIME}秒"
echo "顺序执行UI测试时间: ${SEQUENTIAL_UI_TIME}秒"
echo ""

OPTIMIZED_TOTAL_TIME=$((PARALLEL_API_TIME + SEQUENTIAL_UI_TIME))
echo "优化后总时间（API并行 + UI顺序）: ${OPTIMIZED_TOTAL_TIME}秒"

if [ $SEQUENTIAL_TIME -gt 0 ]; then
    IMPROVEMENT=$(echo "scale=2; ($SEQUENTIAL_TIME - $OPTIMIZED_TOTAL_TIME) / $SEQUENTIAL_TIME * 100" | bc)
    echo "性能提升: ${IMPROVEMENT}%"
fi
echo "=========================================="
#!/bin/bash
# 性能对比测试脚本：Selenium vs Playwright

echo "=========================================="
echo "性能对比测试：Selenium vs Playwright"
echo "=========================================="
echo ""

# 激活虚拟环境
source venv/bin/activate

echo "1. 运行Selenium测试..."
echo "----------------------------------------"
start_time=$(date +%s)
pytest api/test_data_consistency.py -v --tb=short
selenium_exit_code=$?
end_time=$(date +%s)
selenium_time=$((end_time - start_time))
echo ""
echo "Selenium测试耗时: ${selenium_time}秒"
echo ""

echo "=========================================="
echo ""

echo "2. 运行Playwright测试..."
echo "----------------------------------------"
start_time=$(date +%s)
pytest api/test_data_consistency_playwright.py -v --tb=short
playwright_exit_code=$?
end_time=$(date +%s)
playwright_time=$((end_time - start_time))
echo ""
echo "Playwright测试耗时: ${playwright_time}秒"
echo ""

echo "=========================================="
echo "性能对比结果"
echo "=========================================="
echo "Selenium测试耗时:  ${selenium_time}秒"
echo "Playwright测试耗时: ${playwright_time}秒"
echo ""

if [ $selenium_time -gt $playwright_time ]; then
    improvement=$((selenium_time - playwright_time))
    percentage=$((improvement * 100 / selenium_time))
    echo "Playwright比Selenium快 ${improvement}秒 (${percentage}%)"
elif [ $playwright_time -gt $selenium_time ]; then
    slower=$((playwright_time - selenium_time))
    percentage=$((slower * 100 / playwright_time))
    echo "Selenium比Playwright快 ${slower}秒 (${percentage}%)"
else
    echo "两者耗时相同"
fi
echo ""

echo "测试结果:"
echo "  Selenium: $([ $selenium_exit_code -eq 0 ] && echo '通过' || echo '失败')"
echo "  Playwright: $([ $playwright_exit_code -eq 0 ] && echo '通过' || echo '失败')"
echo ""
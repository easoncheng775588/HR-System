#!/bin/bash

# 测试运行脚本

echo "===== 开始执行自动化测试 ====="

# 创建并激活虚拟环境
echo "\n1. 创建并激活虚拟环境..."
python3 -m venv venv
source venv/bin/activate

# 检查并安装依赖
echo "\n2. 检查并安装测试依赖..."
python3 -m pip install -r requirements.txt

# 执行API测试
echo "\n3. 执行后端API测试..."
python3 -m pytest -xvs api/

# 执行UI测试（可选）
echo "\n4. 执行前端UI测试（可选）..."
# python3 -m pytest -xvs ui/

# 停用虚拟环境
deactivate

echo "\n===== 测试执行完成 ====="
echo "测试报告已生成在 reports/ 目录下"

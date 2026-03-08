#!/bin/bash
# 消息管理API测试脚本

API_BASE_URL="http://localhost:8080/api"

echo "========================================"
echo "消息管理API测试"
echo "========================================"

# 测试1: 获取消息列表
echo ""
echo "1. 获取消息列表"
curl -s "$API_BASE_URL/message/list" | python3 -m json.tool | head -30

# 测试2: 创建消息
echo ""
echo "2. 创建消息"
MESSAGE_DATA='{
  "title": "测试消息",
  "content": "这是一条测试消息",
  "type": "SYSTEM",
  "status": "UNREAD",
  "priority": "NORMAL",
  "targetUserId": "1001",
  "createUserId": "1001",
  "createUserName": "系统用户"
}'

echo "请求数据: $MESSAGE_DATA"
curl -s -X POST -H "Content-Type: application/json" -d "$MESSAGE_DATA" "$API_BASE_URL/message/create" | python3 -m json.tool

# 测试3: 获取未读数量
echo ""
echo "3. 获取未读消息数量"
curl -s "$API_BASE_URL/message/unread-count/1001" | python3 -m json.tool

# 测试4: 获取用户消息
echo ""
echo "4. 获取用户消息列表"
curl -s "$API_BASE_URL/message/user/1001" | python3 -m json.tool | head -30

# 测试5: 获取系统消息
echo ""
echo "5. 获取系统消息"
curl -s "$API_BASE_URL/message/type/SYSTEM" | python3 -m json.tool | head -30

echo ""
echo "========================================"
echo "测试完成"
echo "========================================"
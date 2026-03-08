#!/usr/bin/env python3
import requests
import json

API_BASE_URL = "http://localhost:8080/api"

def test_message_api():
    print("测试消息管理API")
    print("=" * 50)
    
    # 测试1: 获取消息列表
    print("\n1. 获取消息列表")
    response = requests.get(f"{API_BASE_URL}/message/list")
    print(f"状态码: {response.status_code}")
    data = response.json()
    print(f"返回码: {data.get('returnCode')}")
    print(f"消息数量: {len(data.get('body', []))}")
    
    # 测试2: 创建消息
    print("\n2. 创建消息")
    message_data = {
        "title": "测试消息",
        "content": "这是一条测试消息",
        "type": "SYSTEM",
        "status": "UNREAD",
        "priority": "NORMAL",
        "targetUserId": "1001",
        "createUserId": "1001",
        "createUserName": "系统用户"
    }
    response = requests.post(f"{API_BASE_URL}/message/create", json=message_data)
    print(f"状态码: {response.status_code}")
    data = response.json()
    print(f"返回码: {data.get('returnCode')}")
    
    if data.get("returnCode") == "SUC0000":
        message_id = data.get("body", {}).get("messageId")
        print(f"消息ID: {message_id}")
        
        # 测试3: 获取未读数量
        print("\n3. 获取未读消息数量")
        response = requests.get(f"{API_BASE_URL}/message/unread-count/1001")
        print(f"状态码: {response.status_code}")
        data = response.json()
        print(f"返回码: {data.get('returnCode')}")
        print(f"未读数量: {data.get('body', {}).get('unreadCount')}")
        
        # 测试4: 标记为已读
        print("\n4. 标记消息为已读")
        response = requests.post(f"{API_BASE_URL}/message/{message_id}/mark-read")
        print(f"状态码: {response.status_code}")
        data = response.json()
        print(f"返回码: {data.get('returnCode')}")
        
        # 测试5: 获取未读数量（应该减少）
        print("\n5. 获取未读消息数量（标记后）")
        response = requests.get(f"{API_BASE_URL}/message/unread-count/1001")
        print(f"状态码: {response.status_code}")
        data = response.json()
        print(f"返回码: {data.get('returnCode')}")
        print(f"未读数量: {data.get('body', {}).get('unreadCount')}")
        
        # 测试6: 删除消息
        print("\n6. 删除消息")
        response = requests.delete(f"{API_BASE_URL}/message/{message_id}")
        print(f"状态码: {response.status_code}")
        data = response.json()
        print(f"返回码: {data.get('returnCode')}")
    
    print("\n" + "=" * 50)
    print("测试完成！")

if __name__ == "__main__":
    test_message_api()
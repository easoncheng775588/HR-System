import unittest
import requests
from datetime import datetime, timedelta
import sys
import os

# 添加项目根目录到Python搜索路径
sys.path.insert(0, os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
from common.config import config

class TestMessageManagement(unittest.TestCase):
    """消息管理功能测试"""

    def setUp(self):
        self.api_client = requests.Session()
        self.api_client.headers.update({
            'Content-Type': 'application/json'
        })

    def tearDown(self):
        self.api_client.close()

    def test_create_system_message(self):
        """测试创建系统消息 (TC-111)"""
        message_data = {
            "title": "测试系统消息",
            "content": "这是一条测试系统消息",
            "type": "SYSTEM",
            "status": "UNREAD",
            "priority": "NORMAL",
            "targetUserId": None,
            "targetUserRole": None,
            "createUserId": "1001",
            "createUserName": "系统用户"
        }

        response = self.api_client.post(
            f"{config.API_BASE_URL}/api/message/create",
            json=message_data
        )

        self.assertEqual(response.status_code, 200, "创建系统消息失败")
        
        data = response.json()
        self.assertEqual(data.get("returnCode"), "SUC0000", "返回码不正确")
        
        message = data.get("body", {})
        self.assertIn("messageId", message, "消息缺少messageId字段")
        self.assertEqual(message.get("title"), "测试系统消息", "消息标题不正确")
        self.assertEqual(message.get("type"), "SYSTEM", "消息类型不正确")

    def test_create_approval_message(self):
        """测试创建审批通知消息 (TC-112)"""
        message_data = {
            "title": "审批通知",
            "content": "您有一个新的用人申请需要审批",
            "type": "APPROVAL",
            "status": "UNREAD",
            "priority": "HIGH",
            "targetUserId": "1004",
            "targetUserRole": None,
            "createUserId": "1001",
            "createUserName": "系统用户"
        }

        response = self.api_client.post(
            f"{config.API_BASE_URL}/api/message/create",
            json=message_data
        )

        self.assertEqual(response.status_code, 200, "创建审批通知失败")
        
        data = response.json()
        self.assertEqual(data.get("returnCode"), "SUC0000", "返回码不正确")
        
        message = data.get("body", {})
        self.assertEqual(message.get("type"), "APPROVAL", "消息类型不正确")
        self.assertEqual(message.get("targetUserId"), "1004", "目标用户ID不正确")

    def test_view_message_list(self):
        """测试查看消息列表 (TC-113)"""
        response = self.api_client.get(f"{config.API_BASE_URL}/api/message/list")
        
        self.assertEqual(response.status_code, 200, "获取消息列表失败")
        
        data = response.json()
        self.assertEqual(data.get("returnCode"), "SUC0000", "返回码不正确")
        
        messages = data.get("body", [])
        self.assertIsInstance(messages, list, "消息列表应该是列表")
        
        if messages:
            message = messages[0]
            self.assertIn("messageId", message, "消息缺少messageId字段")
            self.assertIn("title", message, "消息缺少title字段")
            self.assertIn("content", message, "消息缺少content字段")

    def test_view_user_messages(self):
        """测试查看用户消息列表 (TC-114)"""
        user_id = "1001"
        response = self.api_client.get(f"{config.API_BASE_URL}/api/message/user/{user_id}")
        
        self.assertEqual(response.status_code, 200, "获取用户消息列表失败")
        
        data = response.json()
        self.assertEqual(data.get("returnCode"), "SUC0000", "返回码不正确")
        
        messages = data.get("body", [])
        self.assertIsInstance(messages, list, "用户消息列表应该是列表")

    def test_mark_message_as_read(self):
        """测试标记消息为已读 (TC-115)"""
        # 先创建一条消息
        message_data = {
            "title": "测试标记已读",
            "content": "测试消息",
            "type": "SYSTEM",
            "status": "UNREAD",
            "priority": "NORMAL",
            "targetUserId": "1001",
            "createUserId": "1001",
            "createUserName": "系统用户"
        }

        create_response = self.api_client.post(
            f"{config.API_BASE_URL}/api/message/create",
            json=message_data
        )

        if create_response.status_code == 200:
            create_data = create_response.json()
            if create_data.get("returnCode") == "SUC0000":
                message_id = create_data.get("body", {}).get("messageId")
                
                if message_id:
                    # 标记为已读
                    mark_response = self.api_client.post(
                        f"{config.API_BASE_URL}/api/message/{message_id}/mark-read"
                    )
                    
                    self.assertEqual(mark_response.status_code, 200, "标记消息为已读失败")
                    
                    mark_data = mark_response.json()
                    self.assertEqual(mark_data.get("returnCode"), "SUC0000", "返回码不正确")

    def test_delete_message(self):
        """测试删除消息 (TC-116)"""
        # 先创建一条消息
        message_data = {
            "title": "测试删除消息",
            "content": "测试消息",
            "type": "SYSTEM",
            "status": "UNREAD",
            "priority": "NORMAL",
            "targetUserId": "1001",
            "createUserId": "1001",
            "createUserName": "系统用户"
        }

        create_response = self.api_client.post(
            f"{config.API_BASE_URL}/api/message/create",
            json=message_data
        )

        if create_response.status_code == 200:
            create_data = create_response.json()
            if create_data.get("returnCode") == "SUC0000":
                message_id = create_data.get("body", {}).get("messageId")
                
                if message_id:
                    # 删除消息
                    delete_response = self.api_client.delete(
                        f"{config.API_BASE_URL}/api/message/{message_id}"
                    )
                    
                    self.assertEqual(delete_response.status_code, 200, "删除消息失败")
                    
                    delete_data = delete_response.json()
                    self.assertEqual(delete_data.get("returnCode"), "SUC0000", "返回码不正确")

    def test_get_unread_count(self):
        """测试获取未读消息数量 (TC-117)"""
        user_id = "1001"
        response = self.api_client.get(
            f"{config.API_BASE_URL}/api/message/unread-count/{user_id}"
        )
        
        self.assertEqual(response.status_code, 200, "获取未读消息数量失败")
        
        data = response.json()
        self.assertEqual(data.get("returnCode"), "SUC0000", "返回码不正确")
        
        result = data.get("body", {})
        self.assertIn("unreadCount", result, "响应缺少unreadCount字段")
        self.assertIsInstance(result.get("unreadCount"), int, "unreadCount应该是整数")

    def test_batch_mark_as_read(self):
        """测试批量标记消息为已读 (TC-118)"""
        # 先创建多条消息
        message_ids = []
        
        for i in range(3):
            message_data = {
                "title": f"测试批量标记{i}",
                "content": "测试消息",
                "type": "SYSTEM",
                "status": "UNREAD",
                "priority": "NORMAL",
                "targetUserId": "1001",
                "createUserId": "1001",
                "createUserName": "系统用户"
            }

            create_response = self.api_client.post(
                f"{config.API_BASE_URL}/api/message/create",
                json=message_data
            )

            if create_response.status_code == 200:
                create_data = create_response.json()
                if create_data.get("returnCode") == "SUC0000":
                    message_id = create_data.get("body", {}).get("messageId")
                    if message_id:
                        message_ids.append(message_id)
        
        if message_ids:
            # 批量标记为已读
            batch_response = self.api_client.post(
                f"{config.API_BASE_URL}/api/message/batch-mark-read",
                json=message_ids
            )
            
            self.assertEqual(batch_response.status_code, 200, "批量标记消息为已读失败")
            
            batch_data = batch_response.json()
            self.assertEqual(batch_data.get("returnCode"), "SUC0000", "返回码不正确")

    def test_message_priority(self):
        """测试消息优先级 (TC-119)"""
        priorities = ["HIGH", "NORMAL", "LOW"]
        
        for priority in priorities:
            message_data = {
                "title": f"测试优先级-{priority}",
                "content": "测试消息",
                "type": "SYSTEM",
                "status": "UNREAD",
                "priority": priority,
                "targetUserId": "1001",
                "createUserId": "1001",
                "createUserName": "系统用户"
            }

            response = self.api_client.post(
                f"{config.API_BASE_URL}/api/message/create",
                json=message_data
            )

            if response.status_code == 200:
                data = response.json()
                if data.get("returnCode") == "SUC0000":
                    message = data.get("body", {})
                    self.assertEqual(message.get("priority"), priority, f"消息优先级{priority}不正确")

    def test_message_type_filter(self):
        """测试消息类型筛选 (TC-120)"""
        message_types = ["SYSTEM", "APPROVAL", "INTERVIEW", "OTHER"]
        
        for msg_type in message_types:
            response = self.api_client.get(
                f"{config.API_BASE_URL}/api/message/type/{msg_type}"
            )
            
            self.assertEqual(response.status_code, 200, f"获取{msg_type}类型消息失败")
            
            data = response.json()
            self.assertEqual(data.get("returnCode"), "SUC0000", "返回码不正确")
            
            messages = data.get("body", [])
            for message in messages:
                self.assertEqual(message.get("type"), msg_type, f"消息类型{msg_type}不正确")

    def test_message_status_filter(self):
        """测试消息状态筛选 (TC-121)"""
        statuses = ["UNREAD", "READ"]
        
        for status in statuses:
            response = self.api_client.get(
                f"{config.API_BASE_URL}/api/message/status/{status}"
            )
            
            self.assertEqual(response.status_code, 200, f"获取{status}状态消息失败")
            
            data = response.json()
            self.assertEqual(data.get("returnCode"), "SUC0000", "返回码不正确")
            
            messages = data.get("body", [])
            for message in messages:
                self.assertEqual(message.get("status"), status, f"消息状态{status}不正确")

    def test_message_for_role(self):
        """测试角色消息 (TC-122)"""
        message_data = {
            "title": "测试角色消息",
            "content": "这是给室经理的消息",
            "type": "SYSTEM",
            "status": "UNREAD",
            "priority": "NORMAL",
            "targetUserId": None,
            "targetUserRole": "ROOM_MANAGER",
            "createUserId": "1001",
            "createUserName": "系统用户"
        }

        create_response = self.api_client.post(
            f"{config.API_BASE_URL}/api/message/create",
            json=message_data
        )

        if create_response.status_code == 200:
            create_data = create_response.json()
            if create_data.get("returnCode") == "SUC0000":
                # 获取角色消息
                role_response = self.api_client.get(
                    f"{config.API_BASE_URL}/api/message/role/ROOM_MANAGER"
                )
                
                self.assertEqual(role_response.status_code, 200, "获取角色消息失败")
                
                role_data = role_response.json()
                self.assertEqual(role_data.get("returnCode"), "SUC0000", "返回码不正确")

    def test_message_time_order(self):
        """测试消息时间排序 (TC-123)"""
        response = self.api_client.get(f"{config.API_BASE_URL}/api/message/list")
        
        if response.status_code == 200:
            data = response.json()
            if data.get("returnCode") == "SUC0000":
                messages = data.get("body", [])
                
                if len(messages) >= 2:
                    # 验证消息按创建时间降序排列
                    for i in range(len(messages) - 1):
                        current_time = messages[i].get("createTime")
                        next_time = messages[i + 1].get("createTime")
                        
                        if current_time and next_time:
                            self.assertGreaterEqual(
                                current_time,
                                next_time,
                                "消息应该按创建时间降序排列"
                            )

    def test_message_detail(self):
        """测试查看消息详情 (TC-124)"""
        # 先创建一条消息
        message_data = {
            "title": "测试消息详情",
            "content": "测试消息内容",
            "type": "SYSTEM",
            "status": "UNREAD",
            "priority": "NORMAL",
            "targetUserId": "1001",
            "createUserId": "1001",
            "createUserName": "系统用户"
        }

        create_response = self.api_client.post(
            f"{config.API_BASE_URL}/api/message/create",
            json=message_data
        )

        if create_response.status_code == 200:
            create_data = create_response.json()
            if create_data.get("returnCode") == "SUC0000":
                message_id = create_data.get("body", {}).get("messageId")
                
                if message_id:
                    # 获取消息详情
                    detail_response = self.api_client.get(
                        f"{config.API_BASE_URL}/api/message/{message_id}"
                    )
                    
                    self.assertEqual(detail_response.status_code, 200, "获取消息详情失败")
                    
                    detail_data = detail_response.json()
                    self.assertEqual(detail_data.get("returnCode"), "SUC0000", "返回码不正确")
                    
                    message = detail_data.get("body", {})
                    self.assertIn("messageId", message, "消息详情缺少messageId字段")
                    self.assertIn("title", message, "消息详情缺少title字段")
                    self.assertIn("content", message, "消息详情缺少content字段")
                    self.assertIn("type", message, "消息详情缺少type字段")
                    self.assertIn("status", message, "消息详情缺少status字段")
                    self.assertIn("priority", message, "消息详情缺少priority字段")

    def test_update_message(self):
        """测试更新消息 (TC-125)"""
        # 先创建一条消息
        message_data = {
            "title": "测试更新消息",
            "content": "原始内容",
            "type": "SYSTEM",
            "status": "UNREAD",
            "priority": "NORMAL",
            "targetUserId": "1001",
            "createUserId": "1001",
            "createUserName": "系统用户"
        }

        create_response = self.api_client.post(
            f"{config.API_BASE_URL}/api/message/create",
            json=message_data
        )

        if create_response.status_code == 200:
            create_data = create_response.json()
            if create_data.get("returnCode") == "SUC0000":
                message_id = create_data.get("body", {}).get("messageId")
                
                if message_id:
                    # 更新消息
                    update_data = {
                        "messageId": message_id,
                        "title": "更新后的标题",
                        "content": "更新后的内容"
                    }
                    
                    update_response = self.api_client.put(
                        f"{config.API_BASE_URL}/api/message/update",
                        json=update_data
                    )
                    
                    self.assertEqual(update_response.status_code, 200, "更新消息失败")
                    
                    update_result = update_response.json()
                    self.assertEqual(update_result.get("returnCode"), "SUC0000", "返回码不正确")
                    
                    updated_message = update_result.get("body", {})
                    self.assertEqual(updated_message.get("title"), "更新后的标题", "消息标题未更新")

if __name__ == "__main__":
    unittest.main()
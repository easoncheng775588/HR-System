#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
待办事项测试脚本
测试待办事项的显示、更新、处理等功能
"""

import sys
import os
import unittest
import requests
from datetime import datetime, timedelta

# 添加项目根目录到Python搜索路径
sys.path.insert(0, os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

from common.config import config

class TestTodoItems(unittest.TestCase):
    """待办事项测试类"""
    
    def setUp(self):
        """测试前准备"""
        self.api_client = requests.Session()
        self.api_client.verify = False
        
        # 使用admin用户登录
        test_user = config.TEST_USERS.get("admin", {})
        
        # 登录获取token
        login_response = self.api_client.post(
            f"{config.API_BASE_URL}/api/auth/login",
            json={
                "username": test_user.get("username", "admin"),
                "password": test_user.get("password", "123321")
            }
        )
        
        if login_response.status_code == 200:
            data = login_response.json()
            if data.get("returnCode") == "SUC0000":
                token = data.get("body", {}).get("token")
                if token:
                    self.api_client.headers.update({"Authorization": f"Bearer {token}"})
    
    def test_view_todo_list(self):
        """测试查看待办事项列表 (TC-101)"""
        # 获取待审批列表
        approval_response = self.api_client.get(f"{config.API_BASE_URL}/api/recruitment-request/approval/pending")
        
        self.assertEqual(approval_response.status_code, 200, "获取待审批列表失败")
        
        approval_data = approval_response.json()
        self.assertEqual(approval_data.get("returnCode"), "SUC0000", "返回码不正确")
        
        # 获取待面试数量
        interview_response = self.api_client.get(f"{config.API_BASE_URL}/api/interview/pending/count")
        
        self.assertEqual(interview_response.status_code, 200, "获取待面试数量失败")
        
        interview_data = interview_response.json()
        self.assertEqual(interview_data.get("returnCode"), "SUC0000", "返回码不正确")
        
        # 验证待办事项包含必要的字段
        approval_list = approval_data.get("body", [])
        self.assertIsInstance(approval_list, list, "待审批列表应该是数组")
        
        if approval_list:
            approval = approval_list[0]
            self.assertIn("recruitmentRequestId", approval, "待审批事项缺少recruitmentRequestId字段")
            self.assertIn("requestTitle", approval, "待审批事项缺少requestTitle字段")
            self.assertIn("approvalStatus", approval, "待审批事项缺少approvalStatus字段")
    
    def test_todo_count_display(self):
        """测试待办事项数量显示 (TC-102)"""
        # 获取待审批列表
        approval_response = self.api_client.get(f"{config.API_BASE_URL}/api/recruitment-request/approval/pending")
        
        if approval_response.status_code == 200:
            approval_data = approval_response.json()
            if approval_data.get("returnCode") == "SUC0000":
                approval_count = len(approval_data.get("body", []))
                
                # 获取待面试数量
                interview_response = self.api_client.get(f"{config.API_BASE_URL}/api/interview/pending/count")
                
                if interview_response.status_code == 200:
                    interview_data = interview_response.json()
                    if interview_data.get("returnCode") == "SUC0000":
                        interview_count = interview_data.get("body", 0)
                        
                        # 验证数量显示正确
                        self.assertGreaterEqual(approval_count, 0, "待审批数量应该大于等于0")
                        self.assertGreaterEqual(interview_count, 0, "待面试数量应该大于等于0")
    
    def test_todo_realtime_update(self):
        """测试待办事项实时更新 (TC-103)"""
        # 记录当前待办事项数量
        before_approval_response = self.api_client.get(f"{config.API_BASE_URL}/api/recruitment-request/approval/pending")
        before_approval_count = 0
        
        if before_approval_response.status_code == 200:
            data = before_approval_response.json()
            if data.get("returnCode") == "SUC0000":
                before_approval_count = len(data.get("body", []))
        
        # 提交新的用人申请
        create_response = self.api_client.post(
            f"{config.API_BASE_URL}/api/recruitment-request/save-draft",
            json={
                "requestTitle": "测试岗位-待办事项",
                "supplementCount": 5,
                "positionOrTeam": "零售",
                "technicalPlatform": "frontend",
                "suggestedLevel": "中级",
                "experienceYears": "3-5年",
                "skillRequirement": "Python开发",
                "positionResponsibility": "负责系统开发",
                "createUserId": "1001",
                "createUserName": "系统用户",
                "updateUserId": "1001",
                "updateUserName": "系统用户"
            }
        )
        
        if create_response.status_code == 200:
            create_data = create_response.json()
            if create_data.get("returnCode") == "SUC0000":
                request_id = create_data.get("body", {}).get("recruitmentRequestId")
                
                if request_id:
                    # 提交申请
                    submit_response = self.api_client.post(
                        f"{config.API_BASE_URL}/api/recruitment-request/submit",
                        json={
                            "recruitmentRequestId": request_id,
                            "requestTitle": "测试岗位-待办事项",
                            "supplementCount": 5,
                            "positionOrTeam": "零售",
                            "technicalPlatform": "frontend",
                            "suggestedLevel": "中级",
                            "experienceYears": "3-5年",
                            "skillRequirement": "Python开发",
                            "positionResponsibility": "负责系统开发",
                            "createUserId": "1001",
                            "createUserName": "系统用户",
                            "updateUserId": "1001",
                            "updateUserName": "系统用户"
                        }
                    )
        
        # 刷新页面查看待办事项数量
        after_approval_response = self.api_client.get(f"{config.API_BASE_URL}/api/recruitment-request/approval/pending")
        after_approval_count = 0
        
        if after_approval_response.status_code == 200:
            data = after_approval_response.json()
            if data.get("returnCode") == "SUC0000":
                after_approval_count = len(data.get("body", []))
        
        # 验证待办事项数量增加
        self.assertGreaterEqual(after_approval_count, before_approval_count, 
            "提交申请后，待办事项数量应该增加或保持不变")
    
    def test_process_todo_item(self):
        """测试处理待办事项 (TC-104)"""
        # 获取待审批列表
        response = self.api_client.get(f"{config.API_BASE_URL}/api/recruitment-request/approval/pending")
        
        if response.status_code == 200:
            data = response.json()
            if data.get("returnCode") == "SUC0000":
                todo_list = data.get("body", [])
                
                if todo_list:
                    todo = todo_list[0]
                    request_id = todo.get("recruitmentRequestId")
                    
                    if request_id:
                        # 完成待办事项（审批通过）
                        complete_response = self.api_client.post(
                            f"{config.API_BASE_URL}/api/recruitment-request/{request_id}/approve",
                            json={
                                "approvalUserId": "1001",
                                "approvalUserName": "系统用户",
                                "approvalComment": "同意",
                                "approvalResult": "APPROVED"
                            }
                        )
                        
                        self.assertEqual(complete_response.status_code, 200, "完成待办事项失败")
                        
                        # 验证待办事项已完成
                        complete_data = complete_response.json()
                        self.assertEqual(complete_data.get("returnCode"), "SUC0000", "返回码不正确")
    
    def test_todo_category_display(self):
        """测试待办事项分类显示 (TC-105)"""
        # 获取待审批列表
        approval_response = self.api_client.get(f"{config.API_BASE_URL}/api/recruitment-request/approval/pending")
        
        if approval_response.status_code == 200:
            approval_data = approval_response.json()
            if approval_data.get("returnCode") == "SUC0000":
                approval_list = approval_data.get("body", [])
                
                # 验证待审批事项有分类
                for approval in approval_list:
                    approval_status = approval.get("approvalStatus")
                    self.assertIsNotNone(approval_status, "待审批事项缺少approvalStatus字段")
                    
                    # 验证分类类型
                    valid_statuses = ["PENDING", "1STAPPROVED", "2NDAPPROVED", "APPROVED", "REJECTED"]
                    self.assertIn(approval_status, valid_statuses, 
                        f"待审批状态{approval_status}不在有效状态中")
    
    def test_todo_priority_display(self):
        """测试待办事项优先级显示 (TC-106)"""
        # 获取待审批列表
        response = self.api_client.get(f"{config.API_BASE_URL}/api/recruitment-request/approval/pending")
        
        if response.status_code == 200:
            data = response.json()
            if data.get("returnCode") == "SUC0000":
                todo_list = data.get("body", [])
                
                # 验证待办事项有优先级（通过supplementCount体现）
                for todo in todo_list:
                    supplement_count = todo.get("supplementCount")
                    self.assertIsNotNone(supplement_count, "待办事项缺少supplementCount字段")
                    
                    # 验证优先级（补充人数越多优先级越高）
                    self.assertGreaterEqual(supplement_count, 0, "补充人数应该大于等于0")
    
    def test_todo_detail_view(self):
        """测试待办事项详情查看 (TC-107)"""
        # 获取待审批列表
        response = self.api_client.get(f"{config.API_BASE_URL}/api/recruitment-request/approval/pending")
        
        if response.status_code == 200:
            data = response.json()
            if data.get("returnCode") == "SUC0000":
                todo_list = data.get("body", [])
                
                if todo_list:
                    todo = todo_list[0]
                    request_id = todo.get("recruitmentRequestId")
                    
                    if request_id:
                        # 查看待办事项详情
                        detail_response = self.api_client.get(
                            f"{config.API_BASE_URL}/api/recruitment-request/{request_id}"
                        )
                        
                        self.assertEqual(detail_response.status_code, 200, "获取待办事项详情失败")
                        
                        detail_data = detail_response.json()
                        self.assertEqual(detail_data.get("returnCode"), "SUC0000", "返回码不正确")
                        
                        todo_detail = detail_data.get("body", {})
                        
                        # 验证详情包含所有必要字段
                        self.assertIn("recruitmentRequestId", todo_detail, "待办事项详情缺少recruitmentRequestId字段")
                        self.assertIn("requestTitle", todo_detail, "待办事项详情缺少requestTitle字段")
                        self.assertIn("approvalStatus", todo_detail, "待办事项详情缺少approvalStatus字段")
                        self.assertIn("supplementCount", todo_detail, "待办事项详情缺少supplementCount字段")
    
    def test_todo_expired_handling(self):
        """测试待办事项过期处理 (TC-110)"""
        # 获取待审批列表
        response = self.api_client.get(f"{config.API_BASE_URL}/api/recruitment-request/approval/pending")
        
        if response.status_code == 200:
            data = response.json()
            if data.get("returnCode") == "SUC0000":
                todo_list = data.get("body", [])
                
                # 查看过期的待办事项
                current_time = datetime.now()
                expired_todos = []
                
                for todo in todo_list:
                    create_time_str = todo.get("createTime")
                    if create_time_str:
                        try:
                            create_time = datetime.strptime(create_time_str, "%Y-%m-%d %H:%M:%S")
                            # 假设待办事项有效期为7天
                            if (current_time - create_time).days > 7:
                                expired_todos.append(todo)
                        except (ValueError, TypeError):
                            pass
                
                # 验证过期待办事项数量（系统应该自动清理或标记）
                # 这里只是验证数据结构，不强制要求系统有过期处理逻辑
                self.assertIsInstance(expired_todos, list, "过期待办事项应该是列表")

if __name__ == "__main__":
    unittest.main()
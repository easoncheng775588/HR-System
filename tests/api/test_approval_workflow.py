#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
分级审批流程测试脚本
测试用人申请的三级审批流程：编制审批、团队审批、分管总审批
"""

import sys
import os
import unittest
import requests
from datetime import datetime, timedelta

# 添加项目根目录到Python搜索路径
sys.path.insert(0, os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

from common.config import config

class TestApprovalWorkflow(unittest.TestCase):
    """分级审批流程测试类"""
    
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
    
    def create_test_request(self, status="PENDING"):
        """创建测试数据"""
        create_response = self.api_client.post(
            f"{config.API_BASE_URL}/api/recruitment-request/save-draft",
            json={
                "requestTitle": f"测试岗位-{status}",
                "team": "零售",
                "totalRecruitmentCount": 10,
                "vacancyCount": 1,
                "supplementCount": 5,
                "technicalPlatform": "frontend",
                "proposedLevel": "中级",
                "experienceYears": "3-5年",
                "skillRequirement": "Python开发",
                "positionResponsibility": "负责系统开发",
                "urgentRequirement": "否",
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
                
                # 如果不是PENDING状态，需要先提交
                if status != "DRAFT":
                    submit_response = self.api_client.post(
                        f"{config.API_BASE_URL}/api/recruitment-request/submit",
                        json={
                            "recruitmentRequestId": request_id,
                            "requestTitle": f"测试岗位-{status}",
                            "team": "零售",
                            "totalRecruitmentCount": 10,
                            "vacancyCount": 1,
                            "supplementCount": 5,
                            "technicalPlatform": "frontend",
                            "proposedLevel": "中级",
                            "experienceYears": "3-5年",
                            "skillRequirement": "Python开发",
                            "positionResponsibility": "负责系统开发",
                            "urgentRequirement": "否",
                            "createUserId": "1001",
                            "createUserName": "系统用户",
                            "updateUserId": "1001",
                            "updateUserName": "系统用户"
                        }
                    )
                    
                    if submit_response.status_code == 200:
                        submit_data = submit_response.json()
                        if submit_data.get("returnCode") == "SUC0000":
                            # 如果需要更高状态，进行审批
                            if status == "1STAPPROVED":
                                self.api_client.post(
                                    f"{config.API_BASE_URL}/api/recruitment-request/{request_id}/three-level/approve",
                                    json={
                                        "approvalUserId": "1001",
                                        "approvalUserName": "系统用户",
                                        "approvalComment": "同意"
                                    }
                                )
                            elif status == "2NDAPPROVED":
                                self.api_client.post(
                                    f"{config.API_BASE_URL}/api/recruitment-request/{request_id}/three-level/approve",
                                    json={
                                        "approvalUserId": "1001",
                                        "approvalUserName": "系统用户",
                                        "approvalComment": "同意"
                                    }
                                )
                                self.api_client.post(
                                    f"{config.API_BASE_URL}/api/recruitment-request/{request_id}/three-level/approve",
                                    json={
                                        "approvalUserId": "1001",
                                        "approvalUserName": "系统用户",
                                        "approvalComment": "同意"
                                    }
                                )
                
                return request_id
        
        return None
    
    def test_submit_recruitment_request(self):
        """测试提交用人申请 (TC-126)"""
        # 创建用人申请草稿
        create_response = self.api_client.post(
            f"{config.API_BASE_URL}/api/recruitment-request/save-draft",
            json={
                "requestTitle": "测试岗位-分级审批",
                "team": "零售",
                "totalRecruitmentCount": 10,
                "vacancyCount": 1,
                "supplementCount": 5,
                "technicalPlatform": "frontend",
                "proposedLevel": "中级",
                "experienceYears": "3-5年",
                "skillRequirement": "Python开发",
                "positionResponsibility": "负责系统开发",
                "urgentRequirement": "否",
                "createUserId": "1001",
                "createUserName": "系统用户",
                "updateUserId": "1001",
                "updateUserName": "系统用户"
            }
        )
        
        self.assertEqual(create_response.status_code, 200, "创建用人申请失败")
        
        create_data = create_response.json()
        
        # 添加错误信息输出
        if create_data.get("returnCode") != "SUC0000":
            print(f"\n创建草稿失败:")
            print(f"  返回码: {create_data.get('returnCode')}")
            print(f"  错误信息: {create_data.get('errorMsg')}")
            print(f"  响应数据: {create_data}\n")
        
        self.assertEqual(create_data.get("returnCode"), "SUC0000", "返回码不正确")
        
        # 提交申请
        request_id = create_data.get("body", {}).get("recruitmentRequestId")
        if request_id:
            submit_response = self.api_client.post(
                f"{config.API_BASE_URL}/api/recruitment-request/submit",
                json={
                    "recruitmentRequestId": request_id,
                    "requestTitle": "测试岗位-分级审批",
                    "team": "零售",
                    "totalRecruitmentCount": 10,
                    "vacancyCount": 1,
                    "supplementCount": 5,
                    "technicalPlatform": "frontend",
                    "proposedLevel": "中级",
                    "experienceYears": "3-5年",
                    "skillRequirement": "Python开发",
                    "positionResponsibility": "负责系统开发",
                    "urgentRequirement": "否",
                    "createUserId": "1001",
                    "createUserName": "系统用户",
                    "updateUserId": "1001",
                    "updateUserName": "系统用户"
                }
            )
            
            self.assertEqual(submit_response.status_code, 200, "提交用人申请失败")
            
            submit_data = submit_response.json()
            self.assertEqual(submit_data.get("returnCode"), "SUC0000", "返回码不正确")
            
            # 验证状态变为"待审批"
            request = submit_data.get("body", {})
            self.assertEqual(request.get("approvalStatus"), "PENDING", 
                "提交后状态应该变为PENDING（待审批）")
    
    def test_quota_approval_first_level(self):
        """测试编制审批（第一级） (TC-127)"""
        # 获取待审批的用人申请
        response = self.api_client.get(f"{config.API_BASE_URL}/api/recruitment-request/pending")
        
        if response.status_code == 200:
            data = response.json()
            if data.get("returnCode") == "SUC0000":
                pending_requests = data.get("body", [])
                
                if pending_requests:
                    request = pending_requests[0]
                    request_id = request.get("recruitmentRequestId")
                    
                    # 编制审批同意
                    approve_response = self.api_client.post(
                        f"{config.API_BASE_URL}/api/recruitment-request/{request_id}/three-level/approve",
                        json={
                            "approvalUserId": "1001",
                            "approvalUserName": "系统用户",
                            "approvalComment": "同意"
                        }
                    )
                    
                    self.assertEqual(approve_response.status_code, 200, "编制审批失败")
                    
                    approve_data = approve_response.json()
                    self.assertEqual(approve_data.get("returnCode"), "SUC0000", "返回码不正确")
                    
                    # 验证状态变为"编制审批通过"
                    approved_request = approve_data.get("body", {})
                    self.assertEqual(approved_request.get("approvalStatus"), "1STAPPROVED", 
                        "编制审批后状态应该变为1STAPPROVED（编制审批通过）")
    
    def test_team_approval_second_level(self):
        """测试团队审批（第二级） (TC-128)"""
        # 创建一个第一级审批通过的用人申请
        request_id = self.create_test_request("1STAPPROVED")
        
        if request_id:
            # 团队审批同意
            approve_response = self.api_client.post(
                f"{config.API_BASE_URL}/api/recruitment-request/{request_id}/three-level/approve",
                json={
                    "approvalUserId": "1001",
                    "approvalUserName": "系统用户",
                    "approvalComment": "同意"
                }
            )
            
            self.assertEqual(approve_response.status_code, 200, "团队审批失败")
            
            approve_data = approve_response.json()
            
            # 添加错误信息输出
            if approve_data.get("returnCode") != "SUC0000":
                print(f"\n团队审批失败:")
                print(f"  返回码: {approve_data.get('returnCode')}")
                print(f"  错误信息: {approve_data.get('errorMsg')}")
                print(f"  响应数据: {approve_data}\n")
            
            self.assertEqual(approve_data.get("returnCode"), "SUC0000", "返回码不正确")
            
            # 验证状态变为"团队审批通过"
            approved_request = approve_data.get("body", {})
            self.assertEqual(approved_request.get("approvalStatus"), "2NDAPPROVED", 
                "团队审批后状态应该变为2NDAPPROVED（团队审批通过）")
    
    def test_department_head_approval_third_level(self):
        """测试分管总审批（第三级） (TC-129)"""
        # 创建一个第二级审批通过的用人申请
        request_id = self.create_test_request("2NDAPPROVED")
        
        if request_id:
            # 分管总审批同意
            approve_response = self.api_client.post(
                f"{config.API_BASE_URL}/api/recruitment-request/{request_id}/three-level/approve",
                json={
                    "approvalUserId": "1001",
                    "approvalUserName": "系统用户",
                    "approvalComment": "同意"
                }
            )
            
            self.assertEqual(approve_response.status_code, 200, "分管总审批失败")
            
            approve_data = approve_response.json()
            
            # 添加错误信息输出
            if approve_data.get("returnCode") != "SUC0000":
                print(f"\n分管总审批失败:")
                print(f"  返回码: {approve_data.get('returnCode')}")
                print(f"  错误信息: {approve_data.get('errorMsg')}")
                print(f"  响应数据: {approve_data}\n")
            
            self.assertEqual(approve_data.get("returnCode"), "SUC0000", "返回码不正确")
            
            # 验证状态变为"全部审批通过"
            approved_request = approve_data.get("body", {})
            self.assertEqual(approved_request.get("approvalStatus"), "3RDAPPROVED", 
                "分管总审批后状态应该变为3RDAPPROVED（全部审批通过）")
    
    def test_approval_reject(self):
        """测试审批拒绝 (TC-130)"""
        # 获取待审批的用人申请
        response = self.api_client.get(f"{config.API_BASE_URL}/api/recruitment-request/pending")
        
        if response.status_code == 200:
            data = response.json()
            if data.get("returnCode") == "SUC0000":
                pending_requests = data.get("body", [])
                
                if pending_requests:
                    request = pending_requests[0]
                    request_id = request.get("recruitmentRequestId")
                    
                    # 审批拒绝
                    reject_response = self.api_client.post(
                        f"{config.API_BASE_URL}/api/recruitment-request/{request_id}/approve",
                        json={
                            "approvalLevel": "QUOTA",
                            "approvalUserId": "1001",
                            "approvalUserName": "系统用户",
                            "approvalComment": "编制不足",
                            "approvalResult": "REJECTED"
                        }
                    )
                    
                    self.assertEqual(reject_response.status_code, 200, "审批拒绝失败")
                    
                    reject_data = reject_response.json()
                    self.assertEqual(reject_data.get("returnCode"), "SUC0000", "返回码不正确")
                    
                    # 验证状态变为"已拒绝"
                    rejected_request = reject_data.get("body", {})
                    self.assertEqual(rejected_request.get("status"), "REJECTED", 
                        "拒绝后状态应该变为REJECTED（已拒绝）")
    
    def test_view_approval_history(self):
        """测试查看审批历史 (TC-131)"""
        # 获取用人申请列表
        response = self.api_client.get(f"{config.API_BASE_URL}/api/recruitment-request/list")
        
        if response.status_code == 200:
            data = response.json()
            if data.get("returnCode") == "SUC0000":
                requests = data.get("body", [])
                
                if requests:
                    request = requests[0]
                    request_id = request.get("recruitmentRequestId")
                    
                    # 查看审批历史
                    history_response = self.api_client.get(
                        f"{config.API_BASE_URL}/api/recruitment-request/approval-history/{request_id}"
                    )
                    
                    self.assertEqual(history_response.status_code, 200, "获取审批历史失败")
                    
                    history_data = history_response.json()
                    self.assertEqual(history_data.get("returnCode"), "SUC0000", "返回码不正确")
                    
                    approval_history = history_data.get("body", [])
                    
                    # 验证审批历史包含必要字段
                    for approval in approval_history:
                        self.assertIn("approvalLevel", approval, "审批历史缺少approvalLevel字段")
                        self.assertIn("approverId", approval, "审批历史缺少approverId字段")
                        self.assertIn("approverName", approval, "审批历史缺少approverName字段")
                        self.assertIn("approvalTime", approval, "审批历史缺少approvalTime字段")
                        self.assertIn("approvalComment", approval, "审批历史缺少approvalComment字段")
                        self.assertIn("approvalStatus", approval, "审批历史缺少approvalStatus字段")
    
    def test_approval_status_display(self):
        """测试审批流程状态显示 (TC-132)"""
        # 获取用人申请列表
        response = self.api_client.get(f"{config.API_BASE_URL}/api/recruitment-request/list")
        
        if response.status_code == 200:
            data = response.json()
            if data.get("returnCode") == "SUC0000":
                requests = data.get("body", [])
                
                # 验证审批状态
                valid_statuses = ["DRAFT", "PENDING", "1STAPPROVED", "2NDAPPROVED", "3RDAPPROVED",
                               "APPROVED", "REJECTED"]
                
                for request in requests:
                    status = request.get("approvalStatus")
                    self.assertIsNotNone(status, "用人申请缺少approvalStatus字段")
                    self.assertIn(status, valid_statuses, 
                        f"审批状态{status}不在有效状态中")
    
    def test_cross_level_approval(self):
        """测试跨级审批 (TC-134)"""
        # 获取待审批的用人申请（未经过编制审批和团队审批）
        response = self.api_client.get(
            f"{config.API_BASE_URL}/api/recruitment-request/list",
            params={"status": "PENDING"}
        )
        
        if response.status_code == 200:
            data = response.json()
            if data.get("returnCode") == "SUC0000":
                requests = data.get("body", [])
                
                if requests:
                    request = requests[0]
                    request_id = request.get("recruitmentRequestId")
                    
                    # 尝试以分管总身份直接审批（跨级）
                    cross_level_response = self.api_client.post(
                        f"{config.API_BASE_URL}/api/recruitment-request/{request_id}/approve",
                        json={
                            "approvalLevel": "DEPARTMENT",
                            "approvalUserId": "1001",
                            "approvalUserName": "系统用户",
                            "approvalComment": "同意",
                            "approvalResult": "APPROVED"
                        }
                    )
                    
                    # 验证跨级审批被拒绝
                    self.assertEqual(cross_level_response.status_code, 200, "跨级审批请求失败")
                    
                    cross_level_data = cross_level_response.json()
                    # 系统应该返回错误，不允许跨级审批
                    self.assertNotEqual(cross_level_data.get("returnCode"), "SUC0000", 
                        "系统应该拒绝跨级审批")
    
    def test_approval_notification(self):
        """测试审批通知 (TC-135)"""
        # 提交用人申请
        create_response = self.api_client.post(
            f"{config.API_BASE_URL}/api/recruitment-request/save",
            json={
                "jobTitle": "测试岗位-审批通知",
                "totalHeadcount": 10,
                "vacantHeadcount": 5,
                "positionOrTeam": "零售",
                "technicalPlatform": "frontend",
                "additionalHeadcount": 5,
                "isRecentUrgent": 0,
                "suggestedLevel": "中级",
                "relatedExperienceYears": "3-5年",
                "skillRequirements": "Python开发",
                "jobResponsibilities": "负责系统开发",
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
                
                # 提交申请
                submit_response = self.api_client.post(
                    f"{config.API_BASE_URL}/api/recruitment-request/{request_id}/submit",
                    json={
                        "submitUserId": "1001",
                        "submitUserName": "系统用户"
                    }
                )
                
                if submit_response.status_code == 200:
                    # 查看待办事项
                    todo_response = self.api_client.get(f"{config.API_BASE_URL}/api/todo/list")
                    
                    if todo_response.status_code == 200:
                        todo_data = todo_response.json()
                        if todo_data.get("returnCode") == "SUC0000":
                            todo_list = todo_data.get("body", [])
                            
                            # 验证待办事项中包含审批任务
                            approval_todos = [t for t in todo_list 
                                           if t.get("type") == "APPROVAL"]
                            self.assertTrue(len(approval_todos) > 0, 
                                "提交申请后应该有审批待办事项")
    
    def test_duplicate_approval(self):
        """测试重复审批 (TC-140)"""
        # 获取已审批的用人申请
        response = self.api_client.get(
            f"{config.API_BASE_URL}/api/recruitment-request/list",
            params={"status": "1STAPPROVED"}
        )
        
        if response.status_code == 200:
            data = response.json()
            if data.get("returnCode") == "SUC0000":
                requests = data.get("body", [])
                
                if requests:
                    request = requests[0]
                    request_id = request.get("recruitmentRequestId")
                    
                    # 尝试再次审批同一申请
                    duplicate_response = self.api_client.post(
                        f"{config.API_BASE_URL}/api/recruitment-request/{request_id}/approve",
                        json={
                            "approvalLevel": "QUOTA",
                            "approvalUserId": "1001",
                            "approvalUserName": "系统用户",
                            "approvalComment": "再次审批",
                            "approvalResult": "APPROVED"
                        }
                    )
                    
                    # 验证重复审批被拒绝
                    self.assertEqual(duplicate_response.status_code, 200, "重复审批请求失败")
                    
                    duplicate_data = duplicate_response.json()
                    # 系统应该返回错误，不允许重复审批
                    self.assertNotEqual(duplicate_data.get("returnCode"), "SUC0000", 
                        "系统应该拒绝重复审批")

if __name__ == "__main__":
    unittest.main()
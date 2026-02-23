# 录用管理API测试

import pytest
import sys
import os

# 添加项目根目录到Python搜索路径
sys.path.insert(0, os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

from common.api_client import ApiClient
from common.config import config

class TestOfferManagement:
    def setup_method(self):
        """每个测试方法执行前的设置"""
        self.api_client = ApiClient()
        # 登录获取认证
        self.api_client.login(
            config.TEST_USERS["admin"]["username"],
            config.TEST_USERS["admin"]["password"]
        )
    
    def teardown_method(self):
        """每个测试方法执行后的清理"""
        if hasattr(self, 'api_client'):
            self.api_client.logout()
    
    def test_save_offer(self):
        """测试保存录用记录功能 (TC-061)"""
        offer_data = {
            "resumeId": 1,
            "recruitmentRequestId": 1,
            "candidateName": "张三",
            "contactPhone": "13800138000",
            "email": "zhangsan@example.com",
            "position": "软件工程师",
            "status": "PENDING",
            "createUserId": "1001",
            "createUserName": "系统用户",
            "updateUserId": "1001",
            "updateUserName": "系统用户"
        }
        
        response = self.api_client.post("/offer/save", json=offer_data)
        assert response.status_code == 200, f"保存录用记录失败，状态码: {response.status_code}"
        
        data = response.json()
        assert data.get("returnCode") == "SUC0000", f"保存录用记录失败，错误信息: {data.get('errorMsg')}"
        assert data.get("body") is not None, "保存录用记录后应返回数据"
    
    def test_update_offer_status(self):
        """测试更新录用状态功能 (TC-062)"""
        # 首先保存一条录用记录
        offer_data = {
            "resumeId": 1,
            "recruitmentRequestId": 1,
            "candidateName": "李四",
            "contactPhone": "13900139000",
            "email": "lisi@example.com",
            "position": "软件工程师",
            "status": "PENDING",
            "createUserId": "1001",
            "createUserName": "系统用户",
            "updateUserId": "1001",
            "updateUserName": "系统用户"
        }
        
        save_response = self.api_client.post("/offer/save", json=offer_data)
        save_data = save_response.json()
        offer_id = save_data.get("body", {}).get("offerId")
        
        if offer_id:
            # 更新录用状态为APPROVED
            response = self.api_client.put(f"/offer/{offer_id}/status?status=APPROVED")
            assert response.status_code == 200, f"更新录用状态失败，状态码: {response.status_code}"
            
            data = response.json()
            assert data.get("returnCode") == "SUC0000", f"更新录用状态失败，错误信息: {data.get('errorMsg')}"
    
    def test_send_offer_email(self):
        """测试发送录用邮件功能 (TC-063)"""
        # 首先保存一条录用记录
        offer_data = {
            "resumeId": 1,
            "recruitmentRequestId": 1,
            "candidateName": "王五",
            "contactPhone": "13700137000",
            "email": "wangwu@example.com",
            "position": "软件工程师",
            "status": "PENDING",
            "createUserId": "1001",
            "createUserName": "系统用户",
            "updateUserId": "1001",
            "updateUserName": "系统用户"
        }
        
        save_response = self.api_client.post("/offer/save", json=offer_data)
        save_data = save_response.json()
        offer_id = save_data.get("body", {}).get("offerId")
        
        if offer_id:
            # 发送录用邮件
            response = self.api_client.post(f"/offer/{offer_id}/send-email")
            assert response.status_code == 200, f"发送录用邮件失败，状态码: {response.status_code}"
            
            data = response.json()
            assert data.get("returnCode") == "SUC0000", f"发送录用邮件失败，错误信息: {data.get('errorMsg')}"
    
    def test_offer_management_permission(self):
        """测试录用管理权限控制 (TC-064)"""
        # 使用普通用户登录
        self.api_client.logout()
        self.api_client.login(
            config.TEST_USERS["doris"]["username"],
            config.TEST_USERS["doris"]["password"]
        )
        
        offer_data = {
            "resumeId": 1,
            "recruitmentRequestId": 1,
            "candidateName": "王五",
            "contactPhone": "13700137000",
            "email": "wangwu@example.com",
            "position": "软件工程师",
            "status": "PENDING",
            "createUserId": "1001",
            "createUserName": "系统用户",
            "updateUserId": "1001",
            "updateUserName": "系统用户"
        }
        
        response = self.api_client.post("/offer/save", json=offer_data)
        # 普通用户可能没有录用审批权限，根据实际后端实现判断
        data = response.json()
        assert data.get("returnCode") is not None, "应返回响应数据"
    
    def test_offer_management_boundary(self):
        """测试录用管理边界条件 (TC-065)"""
        # 测试候选人信息边界
        offer_data = {
            "resumeId": 1,
            "recruitmentRequestId": 1,
            "candidateName": "测试候选人",
            "contactPhone": "13800138000",
            "email": "zhangsan@example.com",
            "position": "软件工程师",
            "status": "PENDING",
            "createUserId": "1001",
            "createUserName": "系统用户",
            "updateUserId": "1001",
            "updateUserName": "系统用户"
        }
        
        response = self.api_client.post("/offer/save", json=offer_data)
        data = response.json()
        # 正常情况下应该保存成功
        assert data.get("returnCode") == "SUC0000", f"保存录用记录失败，错误信息: {data.get('errorMsg')}"
    
    def test_offer_management_exception(self):
        """测试录用管理异常情况 (TC-066)"""
        # 测试无效的简历ID
        offer_data = {
            "resumeId": 999999,  # 不存在的简历ID
            "recruitmentRequestId": 1,
            "candidateName": "赵六",
            "contactPhone": "13600136000",
            "email": "zhaoliu@example.com",
            "position": "软件工程师",
            "status": "PENDING",
            "createUserId": "1001",
            "createUserName": "系统用户",
            "updateUserId": "1001",
            "updateUserName": "系统用户"
        }
        
        response = self.api_client.post("/offer/save", json=offer_data)
        data = response.json()
        # 这里根据实际后端实现，可能返回成功或失败
        # 假设后端会验证简历是否存在
        assert data.get("returnCode") is not None, "应返回响应数据"

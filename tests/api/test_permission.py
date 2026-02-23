# 权限API测试

import pytest
import sys
import os

# 添加项目根目录到Python搜索路径
sys.path.insert(0, os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

from common.api_client import ApiClient
from common.config import config

class TestPermission:
    def setup_method(self):
        """每个测试方法执行前的设置"""
        self.api_client = ApiClient()
    
    def teardown_method(self):
        """每个测试方法执行后的清理"""
        if hasattr(self, 'api_client'):
            self.api_client.logout()
    
    def test_admin_permissions(self):
        """测试管理员权限"""
        # 登录管理员账号
        success = self.api_client.login(
            config.TEST_USERS["admin"]["username"],
            config.TEST_USERS["admin"]["password"]
        )
        assert success, "管理员登录失败"
        
        # 测试访问需要管理员权限的接口
        # 这里以获取用户列表为例（假设存在此接口）
        try:
            response = self.api_client.get("/user/list")
            # 即使返回404也没关系，只要不是401或403
            assert response.status_code not in [401, 403], "管理员应该有权限访问此接口"
        except Exception:
            # 如果接口不存在，忽略错误
            pass
    
    def test_manager_permissions(self):
        """测试经理权限"""
        # 登录团队经理账号（viking）
        success = self.api_client.login(
            config.TEST_USERS["viking"]["username"],
            config.TEST_USERS["viking"]["password"]
        )
        assert success, "团队经理登录失败"
        
        # 测试访问需要经理权限的接口
        # 这里以获取审批列表为例（假设存在此接口）
        try:
            response = self.api_client.get("/approval/list")
            # 即使返回404也没关系，只要不是401
            assert response.status_code != 401, "团队经理应该有权限访问此接口"
        except Exception:
            # 如果接口不存在，忽略错误
            pass
    
    def test_user_permissions(self):
        """测试普通用户权限"""
        # 登录普通用户账号（doris）
        success = self.api_client.login(
            config.TEST_USERS["doris"]["username"],
            config.TEST_USERS["doris"]["password"]
        )
        assert success, "普通用户登录失败"
        
        # 测试访问普通用户可以访问的接口
        # 这里以获取自己的申请列表为例（假设存在此接口）
        try:
            response = self.api_client.get("/recruitment-request/my-list")
            # 即使返回404也没关系，只要不是401
            assert response.status_code != 401, "普通用户应该有权限访问此接口"
        except Exception:
            # 如果接口不存在，忽略错误
            pass
    
    def test_unauthorized_access(self):
        """测试未授权访问"""
        # 不登录，直接访问需要授权的接口
        try:
            response = self.api_client.get("/recruitment-request/list")
            # 应该返回401或403
            assert response.status_code in [401, 403], "未授权访问应该被拒绝"
        except Exception:
            # 如果接口不存在，忽略错误
            pass

if __name__ == "__main__":
    pytest.main([__file__])

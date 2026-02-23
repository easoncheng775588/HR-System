# 用户管理API测试

import pytest
import sys
import os

# 添加项目根目录到Python搜索路径
sys.path.insert(0, os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

from common.api_client import ApiClient
from common.config import config

class TestUser:
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
    
    def test_get_user_list(self):
        """测试获取用户列表功能"""
        response = self.api_client.get("/users")
        assert response.status_code == 200, f"获取用户列表失败，状态码: {response.status_code}"
        
        data = response.json()
        assert data.get("returnCode") == "SUC0000", f"获取用户列表失败，错误信息: {data.get('errorMsg')}"
        assert data.get("body") is not None, "获取用户列表后应返回数据"
    
    def test_get_current_user(self):
        """测试获取当前用户信息功能"""
        response = self.api_client.get("/auth/user")
        assert response.status_code == 200, f"获取当前用户信息失败，状态码: {response.status_code}"
        
        data = response.json()
        assert data.get("returnCode") == "SUC0000", f"获取当前用户信息失败，错误信息: {data.get('errorMsg')}"
        assert data.get("body") is not None, "获取当前用户信息后应返回数据"

if __name__ == "__main__":
    pytest.main([__file__])

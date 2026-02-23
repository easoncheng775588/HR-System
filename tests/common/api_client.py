# API客户端模块，用于处理API请求

import requests
from .config import config

class ApiClient:
    def __init__(self):
        self.base_url = config.API_BASE_URL
        self.api_prefix = config.API_PREFIX
        self.token = None
        self.session = requests.Session()
        self.session.timeout = config.API_TIMEOUT
    
    def login(self, username, password):
        """
        登录获取认证token
        :param username: 用户名
        :param password: 密码
        :return: 是否登录成功
        """
        try:
            url = f"{self.base_url}{self.api_prefix}/auth/login"
            response = self.session.post(
                url,
                json={"username": username, "password": password}
            )
            response.raise_for_status()
            
            data = response.json()
            if data.get("returnCode") == "SUC0000":
                # 从body中获取token
                body = data.get("body", {})
                self.token = body.get("token", "test-token")
                # 设置默认的认证头
                if self.token:
                    self.session.headers.update({
                        "Authorization": f"Bearer {self.token}"
                    })
                return True
            return False
        except Exception as e:
            print(f"登录失败: {e}")
            return False
    
    def get(self, endpoint, **kwargs):
        """
        发送GET请求
        :param endpoint: API端点
        :param kwargs: 其他请求参数
        :return: 响应对象
        """
        url = f"{self.base_url}{self.api_prefix}{endpoint}"
        return self.session.get(url, **kwargs)
    
    def post(self, endpoint, data=None, json=None, **kwargs):
        """
        发送POST请求
        :param endpoint: API端点
        :param data: 表单数据
        :param json: JSON数据
        :param kwargs: 其他请求参数
        :return: 响应对象
        """
        url = f"{self.base_url}{self.api_prefix}{endpoint}"
        return self.session.post(url, data=data, json=json, **kwargs)
    
    def put(self, endpoint, data=None, json=None, **kwargs):
        """
        发送PUT请求
        :param endpoint: API端点
        :param data: 表单数据
        :param json: JSON数据
        :param kwargs: 其他请求参数
        :return: 响应对象
        """
        url = f"{self.base_url}{self.api_prefix}{endpoint}"
        return self.session.put(url, data=data, json=json, **kwargs)
    
    def delete(self, endpoint, **kwargs):
        """
        发送DELETE请求
        :param endpoint: API端点
        :param kwargs: 其他请求参数
        :return: 响应对象
        """
        url = f"{self.base_url}{self.api_prefix}{endpoint}"
        return self.session.delete(url, **kwargs)
    
    def logout(self):
        """
        登出，清除token
        """
        self.token = None
        if "Authorization" in self.session.headers:
            del self.session.headers["Authorization"]

# 实例化API客户端
api_client = ApiClient()

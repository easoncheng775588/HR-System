# 测试配置文件

class Config:
    # 后端API配置
    API_BASE_URL = "http://localhost:8080"
    API_PREFIX = "/api"
    
    # 前端UI配置
    FRONTEND_URL = "http://localhost:5173"
    
    # 数据库配置
    DB_HOST = "localhost"
    DB_PORT = 3306
    DB_USER = "root"
    DB_PASSWORD = "p@ssw0rd"
    DB_NAME = "hr_system"
    
    # 测试用户配置
    TEST_USERS = {
        "admin": {
            "username": "admin",
            "password": "123321",
            "role": "超级管理员",
            "position": "管理员"
        },
        "doris": {
            "username": "doris",
            "password": "123321",
            "role": "普通用户",
            "position": "编制管理岗"
        },
        "eric": {
            "username": "eric",
            "password": "123321",
            "role": "审批管理员",
            "position": "分管总"
        },
        "peate": {
            "username": "peate",
            "password": "123321",
            "role": "普通用户",
            "position": "室经理"
        },
        "simone": {
            "username": "simone",
            "password": "123321",
            "role": "普通用户",
            "position": "外包招聘岗"
        },
        "ultra": {
            "username": "ultra",
            "password": "123321",
            "role": "普通用户",
            "position": "室经理"
        },
        "viking": {
            "username": "viking",
            "password": "123321",
            "role": "普通用户",
            "position": "团队经理"
        }
    }
    
    # 测试超时配置
    API_TIMEOUT = 30  # API请求超时时间（秒）
    UI_TIMEOUT = 15   # UI操作超时时间（秒）
    UI_SHORT_TIMEOUT = 5  # UI快速操作超时时间（秒）
    
    # 测试报告配置
    REPORT_DIR = "reports"
    REPORT_TITLE = "HR System 自动化测试报告"

# 实例化配置对象
config = Config()

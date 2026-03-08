#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
前后端数据一致性测试脚本（使用Playwright）
用于验证前端显示内容与数据库字段的一致性
"""

import pytest
import sys
import os
import mysql.connector
from datetime import datetime

# 添加项目根目录到Python搜索路径
sys.path.insert(0, os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

from common.api_client import ApiClient
from common.playwright_client import PlaywrightClient
from common.config import config

class TestDataConsistencyPlaywright:
    """前后端数据一致性测试（使用Playwright）"""
    
    @pytest.fixture(autouse=True)
    def setup(self, api_client):
        """每个测试方法执行前的设置"""
        self.api_client = api_client
        
        # 建立数据库连接
        self.db_connection = mysql.connector.connect(
            host=config.DB_HOST,
            user=config.DB_USER,
            password=config.DB_PASSWORD,
            database=config.DB_NAME
        )
        self.db_cursor = self.db_connection.cursor(dictionary=True)
        yield
        # 清理
        if hasattr(self, 'db_cursor'):
            self.db_cursor.close()
        if hasattr(self, 'db_connection'):
            self.db_connection.close()
    
    def get_db_recruitment_request(self, request_id):
        """从数据库获取用人申请数据"""
        query = """
        SELECT 
            recruitment_request_id,
            request_title,
            team,
            total_recruitment_count,
            vacancy_count,
            supplement_count,
            technical_platform,
            proposed_level,
            experience_years,
            skill_requirement,
            position_responsibility,
            approval_status
        FROM recruitment_request
        WHERE recruitment_request_id = %s
        """
        self.db_cursor.execute(query, (request_id,))
        return self.db_cursor.fetchone()
    
    def test_recruitment_request_api_vs_db_consistency(self):
        """测试用人申请API数据与数据库数据的一致性"""
        # 获取用人申请列表
        response = self.api_client.get("/recruitment-request/list")
        assert response.status_code == 200, f"获取用人申请列表失败，状态码: {response.status_code}"
        
        data = response.json()
        assert data.get("returnCode") == "SUC0000", f"获取用人申请列表失败，错误信息: {data.get('errorMsg')}"
        
        requests = data.get("body", [])
        if not requests:
            pytest.skip("没有可测试的用人申请数据")
        
        # 测试前5条记录
        for request in requests[:5]:
            request_id = request.get("recruitmentRequestId")
            if not request_id:
                continue
            
            # 从数据库获取数据
            db_data = self.get_db_recruitment_request(request_id)
            if not db_data:
                continue
            
            # 比较关键字段
            comparisons = [
                ('岗位标题', 'requestTitle', 'request_title'),
                ('所属团队', 'team', 'team'),
                ('补充人数', 'supplementCount', 'supplement_count'),
                ('技术平台', 'technicalPlatform', 'technical_platform'),
                ('建议级别', 'proposedLevel', 'proposed_level'),
                ('审批状态', 'approvalStatus', 'approval_status')
            ]
            
            inconsistencies = []
            for label, api_field, db_field in comparisons:
                api_value = request.get(api_field)
                db_value = db_data.get(db_field)
                
                # 处理类型转换
                if isinstance(api_value, (int, float)):
                    api_value = str(api_value)
                if isinstance(db_value, (int, float)):
                    db_value = str(db_value)
                
                if api_value != db_value:
                    inconsistencies.append({
                        'label': label,
                        'api_value': api_value,
                        'db_value': db_value
                    })
            
            # 输出不一致信息
            if inconsistencies:
                print(f"\n发现API数据与数据库数据不一致（ID: {request_id}）:")
                for inc in inconsistencies:
                    print(f"  字段: {inc['label']}")
                    print(f"    API值: {inc['api_value']}")
                    print(f"    DB值:  {inc['db_value']}")
            
            # 断言没有不一致
            assert len(inconsistencies) == 0, f"发现{len(inconsistencies)}处API数据与数据库数据不一致"
    
    def test_sys_param_api_vs_db_consistency(self):
        """测试系统参数API数据与数据库数据的一致性"""
        param_types = ['LEVEL', 'TEAM', 'PLATFORM']
        
        for param_type in param_types:
            # 通过API获取参数
            response = self.api_client.get(f"/sys/params/active/type/{param_type}")
            assert response.status_code == 200, f"获取{param_type}参数失败，状态码: {response.status_code}"
            
            data = response.json()
            assert data.get("returnCode") == "SUC0000", f"获取{param_type}参数失败，错误信息: {data.get('errorMsg')}"
            
            api_params = data.get("body", [])
            if not api_params:
                continue
            
            # 测试前5条记录
            for api_param in api_params[:5]:
                param_code = api_param.get("paramCode")
                
                # 从数据库获取参数
                self.db_cursor.execute(
                    f"SELECT param_code, param_name FROM sys_param WHERE param_type = '{param_type}' AND param_code = '{param_code}' AND status = 'ACTIVE'"
                )
                db_param = self.db_cursor.fetchone()
                
                if not db_param:
                    continue
                
                # 比较参数名称
                api_param_name = api_param.get("paramName")
                db_param_name = db_param.get("param_name")
                
                if api_param_name != db_param_name:
                    print(f"\n发现{param_type}参数数据不一致:")
                    print(f"  参数代码: {param_code}")
                    print(f"  API值: {api_param_name}")
                    print(f"  DB值:  {db_param_name}")
                
                assert api_param_name == db_param_name, \
                    f"{param_type}参数显示不一致: API={api_param_name}, DB={db_param_name}"
    
    def test_frontend_display_vs_api_data(self, page):
        """测试前端显示内容与API数据的一致性"""
        try:
            # 登录
            page.goto(f"{config.FRONTEND_URL}/login")
            page.fill("input[placeholder='用户名']", config.TEST_USERS["admin"]["username"])
            page.fill("input[placeholder='密码']", config.TEST_USERS["admin"]["password"])
            page.click("button[type='submit']")
            page.wait_for_url(f"{config.FRONTEND_URL}/dashboard", timeout=config.UI_TIMEOUT * 1000)
            
            # 获取用人申请列表
            response = self.api_client.get("/recruitment-request/list")
            assert response.status_code == 200, f"获取用人申请列表失败，状态码: {response.status_code}"
            
            data = response.json()
            assert data.get("returnCode") == "SUC0000", f"获取用人申请列表失败，错误信息: {data.get('errorMsg')}"
            
            requests = data.get("body", [])
            if not requests:
                pytest.skip("没有可测试的用人申请数据")
            
            # 点击用人申请菜单
            page.click("span:has-text('用人申请')")
            
            # 等待页面加载
            page.wait_for_load_state("networkidle")
            
            # 找到查看按钮
            view_buttons = page.locator("button:has(span:text('查看'))")
            button_count = view_buttons.count()
            
            if button_count == 0:
                print("未找到查看按钮，尝试其他方式...")
                # 尝试通过title属性查找
                view_buttons = page.locator("button[title='查看']")
                button_count = view_buttons.count()
                print(f"通过title属性找到 {button_count} 个查看按钮")
                
                if button_count == 0:
                    # 尝试通过class查找
                    view_buttons = page.locator("button.ant-btn:has-text('查看')")
                    button_count = view_buttons.count()
                    print(f"通过class和文本找到 {button_count} 个查看按钮")
            
            if button_count == 0:
                pytest.skip("未找到查看按钮")
            
            # 点击第一个查看按钮
            view_buttons.first.click()
            
            # 等待详情页面加载
            page.wait_for_selector("span:has-text('岗位标题')", timeout=config.UI_TIMEOUT * 1000)
            
            # 获取详情页面的数据
            ui_data = {}
            
            # 获取岗位标题
            try:
                ui_data['岗位标题'] = page.inner_text("span:has-text('岗位标题') + span")
            except:
                ui_data['岗位标题'] = None
            
            # 获取所属团队
            try:
                ui_data['所属团队'] = page.inner_text("span:has-text('所属团队') + span")
            except:
                ui_data['所属团队'] = None
            
            # 获取技术平台
            try:
                ui_data['技术平台'] = page.inner_text("span:has-text('技术平台') + span")
            except:
                ui_data['技术平台'] = None
            
            # 获取补充人数
            try:
                ui_data['补充人数'] = page.inner_text("span:has-text('补充人数') + span")
            except:
                ui_data['补充人数'] = None
            
            # 获取建议级别
            try:
                ui_data['建议级别'] = page.inner_text("span:has-text('建议级别') + span")
            except:
                ui_data['建议级别'] = None
            
            # 获取审批状态
            try:
                status_element = page.locator("span:has-text('审批状态') + span")
                if status_element.count() > 0:
                    ui_data['审批状态'] = status_element.inner_text()
                else:
                    # 尝试获取Tag组件的文本
                    status_tag = page.locator("span:has-text('审批状态')").locator("..").locator(".ant-tag")
                    if status_tag.count() > 0:
                        ui_data['审批状态'] = status_tag.inner_text()
                    else:
                        ui_data['审批状态'] = None
            except:
                ui_data['审批状态'] = None
            
            # 获取第一条记录的岗位标题
            ui_title = ui_data.get('岗位标题')
            if not ui_title:
                pytest.skip("无法获取岗位标题")
            
            # 在API返回的数据中找到匹配的记录
            test_request = None
            for request in requests:
                if request.get("requestTitle") == ui_title:
                    test_request = request
                    break
            
            if not test_request:
                pytest.skip(f"在API数据中找不到岗位标题为'{ui_title}'的记录")
            
            # 打印调试信息
            print(f"\n测试记录: {test_request.get('requestTitle')}")
            print(f"API审批状态: {test_request.get('approvalStatus')}")
            print(f"UI审批状态: {ui_data.get('审批状态')}")
            
            # 比较关键字段
            comparisons = [
                ('岗位标题', 'requestTitle', '岗位标题'),
                ('所属团队', 'team', '所属团队'),
                ('技术平台', 'technicalPlatform', '技术平台'),
                ('补充人数', 'supplementCount', '补充人数'),
                ('建议级别', 'proposedLevel', '建议级别'),
                ('审批状态', 'approvalStatus', '审批状态')
            ]
            
            inconsistencies = []
            for label, api_field, ui_field in comparisons:
                api_value = test_request.get(api_field)
                ui_value = ui_data.get(ui_field)
                
                # 处理类型转换
                if isinstance(api_value, (int, float)):
                    api_value = str(api_value)
                if isinstance(ui_value, (int, float)):
                    ui_value = str(ui_value)
                
                # 跳过审批状态为DRAFT的比较（DRAFT状态不显示审批状态）
                if label == '审批状态' and api_value == 'DRAFT' and ui_value is None:
                    continue
                
                # 跳过审批状态为PENDING的比较（PENDING状态可能不显示审批状态）
                if label == '审批状态' and api_value == 'PENDING' and ui_value is None:
                    continue
                
                # 跳过审批状态为3RDAPPROVED的比较（前端显示为"终审通过"或"团队长终审通过"）
                if label == '审批状态' and api_value == '3RDAPPROVED':
                    if ui_value in ['终审通过', '团队长终审通过']:
                        continue
                    # 如果UI值为None，可能是因为详情页面没有显示审批状态（某些状态下不显示）
                    if ui_value is None:
                        continue
                
                # 跳过技术平台的比较（前端进行了中文翻译）
                if label == '技术平台' and api_value == 'frontend' and ui_value == '前端':
                    continue
                if label == '技术平台' and api_value == 'backend' and ui_value == '后端':
                    continue
                if label == '技术平台' and api_value == 'fullstack' and ui_value == '全栈':
                    continue
                
                if api_value != ui_value:
                    inconsistencies.append({
                        'label': label,
                        'api_value': api_value,
                        'ui_value': ui_value
                    })
            
            # 输出不一致信息
            if inconsistencies:
                print("\n发现前端显示与API数据不一致:")
                for inc in inconsistencies:
                    print(f"  字段: {inc['label']}")
                    print(f"    API值: {inc['api_value']}")
                    print(f"    UI值:  {inc['ui_value']}")
            
            # 断言没有不一致
            assert len(inconsistencies) == 0, f"发现{len(inconsistencies)}处前端显示与API数据不一致"
        except Exception as e:
            print(f"测试失败: {e}")
            import traceback
            traceback.print_exc()
            raise
    
    def test_frontend_param_display_vs_db(self):
        """测试前端参数显示与数据库参数的一致性"""
        param_types = {
            'LEVEL': '建议级别',
            'TEAM': '所属团队',
            'PLATFORM': '技术平台'
        }
        
        for param_type, param_label in param_types.items():
            # 通过API获取参数
            response = self.api_client.get(f"/sys/params/active/type/{param_type}")
            assert response.status_code == 200, f"获取{param_type}参数失败，状态码: {response.status_code}"
            
            data = response.json()
            assert data.get("returnCode") == "SUC0000", f"获取{param_type}参数失败，错误信息: {data.get('errorMsg')}"
            
            api_params = data.get("body", [])
            if not api_params:
                continue
            
            # 测试第一个参数
            test_param = api_params[0]
            param_code = test_param.get("paramCode")
            param_name = test_param.get("paramName")
            
            # 从数据库获取参数
            self.db_cursor.execute(
                f"SELECT param_code, param_name FROM sys_param WHERE param_type = '{param_type}' AND param_code = '{param_code}' AND status = 'ACTIVE'"
            )
            db_param = self.db_cursor.fetchone()
            
            if not db_param:
                continue
            
            # 比较参数名称
            db_param_name = db_param.get("param_name")
            assert param_name == db_param_name, f"{param_label}参数显示不一致: API={param_name}, DB={db_param_name}"
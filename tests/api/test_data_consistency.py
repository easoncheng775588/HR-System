#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
前后端数据一致性测试脚本
用于验证前端显示内容与数据库字段的一致性
"""

import pytest
import json
import sys
import os
import mysql.connector
from datetime import datetime

# 添加项目根目录到Python搜索路径
sys.path.insert(0, os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

from common.api_client import ApiClient
from common.config import config

class TestDataConsistency:
    """前后端数据一致性测试"""
    
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
            urgent_requirement,
            approval_status,
            create_time,
            update_time
        FROM recruitment_request
        WHERE recruitment_request_id = %s
        """
        self.db_cursor.execute(query, (request_id,))
        return self.db_cursor.fetchone()
    
    def get_db_sys_param(self, param_type, param_code):
        """从数据库获取系统参数"""
        query = """
        SELECT 
            param_id,
            param_type,
            param_code,
            param_name,
            param_value,
            status
        FROM sys_param
        WHERE param_type = %s AND param_code = %s AND status = 'ACTIVE'
        """
        self.db_cursor.execute(query, (param_type, param_code))
        return self.db_cursor.fetchone()
    
    def compare_api_vs_db(self, api_data, db_data, field_mapping):
        """
        比较API返回数据与数据库数据
        :param api_data: API返回的数据
        :param db_data: 数据库查询的数据
        :param field_mapping: 字段映射字典 {api_field: db_field}
        :return: 不一致的字段列表
        """
        inconsistencies = []
        
        for api_field, db_field in field_mapping.items():
            api_value = api_data.get(api_field)
            db_value = db_data.get(db_field)
            
            # 处理日期时间格式
            if isinstance(db_value, datetime):
                db_value = db_value.strftime('%Y-%m-%d %H:%M:%S')
            
            # 处理None值
            if api_value is None:
                api_value = ''
            if db_value is None:
                db_value = ''
            
            # 比较值
            if str(api_value) != str(db_value):
                inconsistencies.append({
                    'field': api_field,
                    'api_value': api_value,
                    'db_value': db_value
                })
        
        return inconsistencies
    
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
        
        # 测试第一条记录
        test_request = requests[0]
        request_id = test_request.get("recruitmentRequestId")
        
        # 从数据库获取对应记录
        db_request = self.get_db_recruitment_request(request_id)
        assert db_request is not None, f"数据库中未找到ID为{request_id}的记录"
        
        # 字段映射
        field_mapping = {
            'recruitmentRequestId': 'recruitment_request_id',
            'requestTitle': 'request_title',
            'team': 'team',
            'totalRecruitmentCount': 'total_recruitment_count',
            'vacancyCount': 'vacancy_count',
            'supplementCount': 'supplement_count',
            'technicalPlatform': 'technical_platform',
            'proposedLevel': 'proposed_level',
            'experienceYears': 'experience_years',
            'skillRequirement': 'skill_requirement',
            'positionResponsibility': 'position_responsibility',
            'urgentRequirement': 'urgent_requirement',
            'approvalStatus': 'approval_status'
        }
        
        # 比较数据
        inconsistencies = self.compare_api_vs_db(test_request, db_request, field_mapping)
        
        # 输出不一致信息
        if inconsistencies:
            print("\n发现数据不一致:")
            for inc in inconsistencies:
                print(f"  字段: {inc['field']}")
                print(f"    API值: {inc['api_value']}")
                print(f"    DB值:  {inc['db_value']}")
        
        # 断言没有不一致
        assert len(inconsistencies) == 0, f"发现{len(inconsistencies)}处数据不一致"
    
    def test_sys_param_api_vs_db_consistency(self):
        """测试系统参数API数据与数据库数据的一致性"""
        param_types = ['LEVEL', 'TEAM', 'PLATFORM', 'CATEGORY']
        
        for param_type in param_types:
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
            
            # 从数据库获取对应参数
            db_param = self.get_db_sys_param(param_type, param_code)
            assert db_param is not None, f"数据库中未找到类型为{param_type}、代码为{param_code}的参数"
            
            # 字段映射
            field_mapping = {
                'paramId': 'param_id',
                'paramType': 'param_type',
                'paramCode': 'param_code',
                'paramName': 'param_name',
                'paramValue': 'param_value',
                'status': 'status'
            }
            
            # 比较数据
            inconsistencies = self.compare_api_vs_db(test_param, db_param, field_mapping)
            
            # 输出不一致信息
            if inconsistencies:
                print(f"\n发现{param_type}参数数据不一致:")
                for inc in inconsistencies:
                    print(f"  字段: {inc['field']}")
                    print(f"    API值: {inc['api_value']}")
                    print(f"    DB值:  {inc['db_value']}")
            
            # 断言没有不一致
        assert len(inconsistencies) == 0, f"{param_type}参数发现{len(inconsistencies)}处数据不一致"
    
    def test_frontend_display_vs_api_data(self, ui_client):
        """测试前端显示内容与API数据的一致性"""
        self.ui_client = ui_client
        
        # 获取用人申请列表
        response = self.api_client.get("/recruitment-request/list")
        assert response.status_code == 200, f"获取用人申请列表失败，状态码: {response.status_code}"
        
        data = response.json()
        assert data.get("returnCode") == "SUC0000", f"获取用人申请列表失败，错误信息: {data.get('errorMsg')}"
        
        requests = data.get("body", [])
        if not requests:
            pytest.skip("没有可测试的用人申请数据")
        
        # 通过UI获取前端显示的数据（点击第一个查看按钮）
        ui_data = self.ui_client.get_recruitment_request_detail(None)
        
        if ui_data is None:
            pytest.skip("无法获取前端显示数据")
        
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
            
            # 跳过审批状态为3RDAPPROVED的比较（前端显示为"终审通过"或"团队长终审通过"）
            if label == '审批状态' and api_value == '3RDAPPROVED' and ui_value in ['终审通过', '团队长终审通过']:
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
    
    def test_frontend_param_display_vs_db(self, ui_client):
        """测试前端参数显示与数据库参数的一致性"""
        self.ui_client = ui_client
        
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
    
    def test_approval_status_display_consistency(self, ui_client):
        """测试审批状态显示的一致性"""
        self.ui_client = ui_client
        
        # 获取用人申请列表
        response = self.api_client.get("/recruitment-request/list")
        assert response.status_code == 200, f"获取用人申请列表失败，状态码: {response.status_code}"
        
        data = response.json()
        assert data.get("returnCode") == "SUC0000", f"获取用人申请列表失败，错误信息: {data.get('errorMsg')}"
        
        requests = data.get("body", [])
        if not requests:
            pytest.skip("没有可测试的用人申请数据")
        
        # 状态映射
        status_mapping = {
            'DRAFT': '未提交',
            'PENDING': '待审批',
            '1STAPPROVED': '编制审批通过',
            '2NDAPPROVED': '岗位内容审批通过',
            '3RDAPPROVED': '终审通过',
            'APPROVED': '通过',
            'REJECTED': '拒绝'
        }
        
        # 测试每条记录的审批状态
        for request in requests[:5]:  # 只测试前5条
            api_status = request.get("approvalStatus")
            if not api_status:
                continue
            
            # 通过UI获取前端显示的状态
            ui_status = self.ui_client.get_approval_status_display(request.get("recruitmentRequestId"))
            
            if ui_status is None:
                continue
            
            expected_status = status_mapping.get(api_status, api_status)
            
            # 比较状态显示
            assert expected_status == ui_status, \
                f"审批状态显示不一致: API={api_status}({expected_status}), UI={ui_status}"
    
    def test_team_field_display_consistency(self, ui_client):
        """测试所属团队字段显示的一致性"""
        self.ui_client = ui_client
        
        # 获取用人申请列表
        response = self.api_client.get("/recruitment-request/list")
        assert response.status_code == 200, f"获取用人申请列表失败，状态码: {response.status_code}"
        
        data = response.json()
        assert data.get("returnCode") == "SUC0000", f"获取用人申请列表失败，错误信息: {data.get('errorMsg')}"
        
        requests = data.get("body", [])
        if not requests:
            pytest.skip("没有可测试的用人申请数据")
        
        # 测试每条记录的所属团队
        for request in requests[:5]:  # 只测试前5条
            request_id = request.get("recruitmentRequestId")
            api_team = request.get("team")
            
            if not api_team:
                continue
            
            # 通过UI获取前端显示的团队
            ui_team = self.ui_client.get_team_display(request_id)
            
            if ui_team is None:
                continue
            
            # 比较团队显示
            assert api_team == ui_team, \
                f"所属团队显示不一致: API={api_team}, UI={ui_team}"
    
    def test_technical_platform_display_consistency(self, ui_client):
        """测试技术平台字段显示的一致性"""
        self.ui_client = ui_client
        
        # 获取用人申请列表
        response = self.api_client.get("/recruitment-request/list")
        assert response.status_code == 200, f"获取用人申请列表失败，状态码: {response.status_code}"
        
        data = response.json()
        assert data.get("returnCode") == "SUC0000", f"获取用人申请列表失败，错误信息: {data.get('errorMsg')}"
        
        requests = data.get("body", [])
        if not requests:
            pytest.skip("没有可测试的用人申请数据")
        
        # 技术平台映射
        platform_mapping = {
            'frontend': '前端',
            'backend': '后端',
            'mobile': '移动端',
            'ai': '人工智能'
        }
        
        # 测试每条记录的技术平台
        for request in requests[:5]:  # 只测试前5条
            request_id = request.get("recruitmentRequestId")
            api_platform = request.get("technicalPlatform")
            
            if not api_platform:
                continue
            
            # 通过UI获取前端显示的技术平台
            ui_platform = self.ui_client.get_technical_platform_display(request_id)
            
            if ui_platform is None:
                continue
            
            # 如果API返回的是英文代码，转换为中文
            expected_platform = platform_mapping.get(api_platform.lower(), api_platform)
            
            # 比较技术平台显示
            assert expected_platform == ui_platform, \
                f"技术平台显示不一致: API={api_platform}({expected_platform}), UI={ui_platform}"

if __name__ == "__main__":
    pytest.main([__file__, "-v", "-s"])

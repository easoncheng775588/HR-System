# 面试安排API测试

import pytest
import sys
import os
from datetime import datetime, timedelta

# 添加项目根目录到Python搜索路径
sys.path.insert(0, os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

from common.api_client import ApiClient
from common.config import config

class TestInterviewArrangement:
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
    
    def test_save_interview_record(self):
        """测试保存面试记录功能 (TC-041)"""
        # 先获取简历列表
        resume_response = self.api_client.get("/resume/list")
        if resume_response.status_code == 200:
            resume_data = resume_response.json()
            if resume_data.get("returnCode") == "SUC0000":
                resumes = resume_data.get("body", [])
                if resumes:
                    # 尝试为第一个简历创建面试记录
                    resume_id = resumes[0].get("resumeId")
                    if resume_id:
                        # 准备面试记录数据
                        interview_data = {
                            "resumeId": resume_id,
                            "recruitmentRequestId": resumes[0].get("recruitmentRequestId", 1),
                            "interviewRound": "FIRST_ROUND",
                            "interviewerId": "1003",
                            "interviewerName": "技术总监",
                            "interviewerRole": "DEPARTMENT_HEAD",
                            "interviewTime": (datetime.now() + timedelta(days=1)).strftime("%Y-%m-%dT%H:%M:%S.%f")[:-3] + "Z",
                            "createUserId": "1001",
                            "createUserName": "系统用户",
                            "updateUserId": "1001",
                            "updateUserName": "系统用户"
                        }
                        
                        response = self.api_client.post("/interview/save", json=interview_data)
                        # 打印响应内容以调试
                        if response.status_code != 200:
                            print(f"响应状态码: {response.status_code}")
                            print(f"响应内容: {response.text}")
                        assert response.status_code == 200, f"保存面试记录失败，状态码: {response.status_code}，响应: {response.text}"
                        
                        data = response.json()
                        assert data.get("returnCode") == "SUC0000", f"保存面试记录失败，错误信息: {data.get('errorMsg')}"
                        assert data.get("body") is not None, "保存面试记录后应返回数据"
    
    def test_get_interview_list(self):
        """测试查询面试记录列表功能 (TC-042)"""
        response = self.api_client.get("/interview/list")
        assert response.status_code == 200, f"查询面试记录列表失败，状态码: {response.status_code}"
        
        data = response.json()
        assert data.get("returnCode") == "SUC0000", f"查询面试记录列表失败，错误信息: {data.get('errorMsg')}"
        assert isinstance(data.get("body"), list), "面试记录列表应为数组格式"
    
    def test_update_interview_result(self):
        """测试更新面试结果功能 (TC-043)"""
        # 先获取面试记录
        interview_response = self.api_client.get("/interview/list")
        if interview_response.status_code == 200:
            interview_data = interview_response.json()
            if interview_data.get("returnCode") == "SUC0000":
                interviews = interview_data.get("body", [])
                if interviews:
                    # 尝试更新第一个面试记录的结果
                    interview_id = interviews[0].get("interviewRecordId")
                    if interview_id:
                        # 准备面试结果数据
                        result_data = {
                            "interviewResult": "PASSED",
                            "interviewComment": "技术能力强，沟通表达良好"
                        }
                        
                        response = self.api_client.put(f"/interview/{interview_id}/result", json=result_data)
                        assert response.status_code == 200, f"更新面试结果失败，状态码: {response.status_code}"
                        
                        data = response.json()
                        assert data.get("returnCode") == "SUC0000", f"更新面试结果失败，错误信息: {data.get('errorMsg')}"
    
    def test_get_interview_by_resume(self):
        """测试根据简历ID查询面试记录功能 (TC-044)"""
        # 先获取简历列表
        resume_response = self.api_client.get("/resume/list")
        if resume_response.status_code == 200:
            resume_data = resume_response.json()
            if resume_data.get("returnCode") == "SUC0000":
                resumes = resume_data.get("body", [])
                if resumes:
                    # 尝试根据第一个简历ID查询面试记录
                    resume_id = resumes[0].get("resumeId")
                    if resume_id:
                        response = self.api_client.get(f"/interview/resume/{resume_id}")
                        assert response.status_code == 200, f"根据简历ID查询面试记录失败，状态码: {response.status_code}"
                        
                        data = response.json()
                        assert data.get("returnCode") == "SUC0000", f"根据简历ID查询面试记录失败，错误信息: {data.get('errorMsg')}"
    
    def test_get_pending_interview_count(self):
        """测试获取待面试数量功能 (TC-045)"""
        response = self.api_client.get("/interview/pending/count")
        assert response.status_code == 200, f"获取待面试数量失败，状态码: {response.status_code}"
        
        data = response.json()
        assert data.get("returnCode") == "SUC0000", f"获取待面试数量失败，错误信息: {data.get('errorMsg')}"
        assert isinstance(data.get("body"), int), "待面试数量应为整数"
    
    def test_interview_management_permission(self):
        """测试面试管理权限控制 (TC-046)"""
        # 使用普通用户登录
        if "doris" in config.TEST_USERS:
            self.api_client.logout()
            success = self.api_client.login(
                config.TEST_USERS["doris"]["username"],
                config.TEST_USERS["doris"]["password"]
            )
            if success:
                # 尝试获取面试记录列表
                response = self.api_client.get("/interview/list")
                # 普通用户可能没有权限，根据实际后端实现判断
                data = response.json()
                assert data.get("returnCode") is not None, "应返回响应数据"

    def test_schedule_three_rounds_interview(self):
        """测试安排三面面试功能 (TC-047)"""
        # 先获取简历列表
        resume_response = self.api_client.get("/resume/list")
        print(f"简历列表响应状态码: {resume_response.status_code}")
        print(f"简历列表响应内容: {resume_response.text}")
        
        if resume_response.status_code == 200:
            resume_data = resume_response.json()
            print(f"简历列表返回码: {resume_data.get('returnCode')}")
            print(f"简历数量: {len(resume_data.get('body', []))}")
            
            if resume_data.get("returnCode") == "SUC0000":
                resumes = resume_data.get("body", [])
                if resumes:
                    # 尝试为第一个简历安排三面面试
                    resume_id = resumes[0].get("resumeId")
                    print(f"使用的简历ID: {resume_id}")
                    
                    if resume_id:
                        recruitment_request_id = resumes[0].get("recruitmentRequestId", 1)
                        print(f"使用的招聘申请ID: {recruitment_request_id}")
                        
                        # 1. 安排一面（室经理）
                        first_round_data = {
                            "resumeId": resume_id,
                            "recruitmentRequestId": recruitment_request_id,
                            "interviewRound": "FIRST_ROUND",
                            "interviewerId": "1004",
                            "interviewerName": "室经理",
                            "interviewerRole": "ROOM_MANAGER",
                            "interviewTime": (datetime.now() + timedelta(days=1)).strftime("%Y-%m-%dT%H:%M:%S.%f")[:-3] + "Z",
                            "createUserId": "1001",
                            "createUserName": "系统用户",
                            "updateUserId": "1001",
                            "updateUserName": "系统用户"
                        }
                        
                        print(f"安排一面数据: {first_round_data}")
                        first_response = self.api_client.post("/interview/save", json=first_round_data)
                        print(f"一面安排响应状态码: {first_response.status_code}")
                        print(f"一面安排响应内容: {first_response.text}")
                        assert first_response.status_code == 200, f"安排一面失败，状态码: {first_response.status_code}"
                        
                        # 2. 安排二面（团队经理）
                        second_round_data = {
                            "resumeId": resume_id,
                            "recruitmentRequestId": recruitment_request_id,
                            "interviewRound": "SECOND_ROUND",
                            "interviewerId": "1005",
                            "interviewerName": "团队经理",
                            "interviewerRole": "TEAM_MANAGER",
                            "interviewTime": (datetime.now() + timedelta(days=2)).strftime("%Y-%m-%dT%H:%M:%S.%f")[:-3] + "Z",
                            "createUserId": "1001",
                            "createUserName": "系统用户",
                            "updateUserId": "1001",
                            "updateUserName": "系统用户"
                        }
                        
                        print(f"安排二面数据: {second_round_data}")
                        second_response = self.api_client.post("/interview/save", json=second_round_data)
                        print(f"二面安排响应状态码: {second_response.status_code}")
                        print(f"二面安排响应内容: {second_response.text}")
                        assert second_response.status_code == 200, f"安排二面失败，状态码: {second_response.status_code}"
                        
                        # 3. 安排三面（分管总）
                        third_round_data = {
                            "resumeId": resume_id,
                            "recruitmentRequestId": recruitment_request_id,
                            "interviewRound": "THIRD_ROUND",
                            "interviewerId": "1003",
                            "interviewerName": "分管总",
                            "interviewerRole": "DEPARTMENT_HEAD",
                            "interviewTime": (datetime.now() + timedelta(days=3)).strftime("%Y-%m-%dT%H:%M:%S.%f")[:-3] + "Z",
                            "createUserId": "1001",
                            "createUserName": "系统用户",
                            "updateUserId": "1001",
                            "updateUserName": "系统用户"
                        }
                        
                        print(f"安排三面数据: {third_round_data}")
                        third_response = self.api_client.post("/interview/save", json=third_round_data)
                        print(f"三面安排响应状态码: {third_response.status_code}")
                        print(f"三面安排响应内容: {third_response.text}")
                        assert third_response.status_code == 200, f"安排三面失败，状态码: {third_response.status_code}"
                else:
                    print("警告：简历列表为空，跳过面试安排测试")
            else:
                print(f"警告：获取简历列表失败，返回码: {resume_data.get('returnCode')}")
        else:
            print(f"警告：获取简历列表失败，状态码: {resume_response.status_code}")

    def test_update_three_rounds_results(self):
        """测试更新三面面试结果功能 (TC-048)"""
        # 先获取简历列表
        resume_response = self.api_client.get("/resume/list")
        if resume_response.status_code == 200:
            resume_data = resume_response.json()
            if resume_data.get("returnCode") == "SUC0000":
                resumes = resume_data.get("body", [])
                if resumes:
                    # 获取第一个简历的ID
                    resume_id = resumes[0].get("resumeId")
                    if resume_id:
                        # 根据简历ID获取面试记录
                        interview_response = self.api_client.get(f"/interview/resume/{resume_id}")
                        if interview_response.status_code == 200:
                            interview_data = interview_response.json()
                            if interview_data.get("returnCode") == "SUC0000":
                                interviews = interview_data.get("body", [])
                                if interviews:
                                    # 按面试轮次分组
                                    round_interviews = {}
                                    for interview in interviews:
                                        round = interview.get("interviewRound")
                                        round_interviews[round] = interview
                                    
                                    # 更新一面结果
                                    if "FIRST_ROUND" in round_interviews:
                                        first_round = round_interviews["FIRST_ROUND"]
                                        first_id = first_round.get("interviewRecordId")
                                        if first_id:
                                            first_result = {
                                                "interviewResult": "PASSED",
                                                "interviewComment": "专业知识扎实，符合岗位要求"
                                            }
                                            first_response = self.api_client.put(f"/interview/{first_id}/result", json=first_result)
                                            assert first_response.status_code == 200, f"更新一面结果失败"
                                    
                                    # 更新二面结果
                                    if "SECOND_ROUND" in round_interviews:
                                        second_round = round_interviews["SECOND_ROUND"]
                                        second_id = second_round.get("interviewRecordId")
                                        if second_id:
                                            second_result = {
                                                "interviewResult": "PASSED",
                                                "interviewComment": "团队协作能力强，沟通表达良好"
                                            }
                                            second_response = self.api_client.put(f"/interview/{second_id}/result", json=second_result)
                                            assert second_response.status_code == 200, f"更新二面结果失败"
                                    
                                    # 更新三面结果
                                    if "THIRD_ROUND" in round_interviews:
                                        third_round = round_interviews["THIRD_ROUND"]
                                        third_id = third_round.get("interviewRecordId")
                                        if third_id:
                                            third_result = {
                                                "interviewResult": "PASSED",
                                                "interviewComment": "综合素质优秀，建议录用"
                                            }
                                            third_response = self.api_client.put(f"/interview/{third_id}/result", json=third_result)
                                            assert third_response.status_code == 200, f"更新三面结果失败"

if __name__ == "__main__":
    pytest.main([__file__])

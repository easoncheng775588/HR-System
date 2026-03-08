# 端到端招聘流程测试

import pytest
import sys
import os
from datetime import datetime, timedelta

# 添加项目根目录到Python搜索路径
sys.path.insert(0, os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

from common.api_client import ApiClient
from common.config import config

class TestEndToEndRecruitmentProcess:
    """端到端招聘流程测试 - 从用人申请到录用的完整流程"""
    
    def setup_method(self):
        """每个测试方法执行前的设置"""
        self.api_client = ApiClient()
        # 登录获取认证
        self.api_client.login(
            config.TEST_USERS["admin"]["username"],
            config.TEST_USERS["admin"]["password"]
        )
        self.test_data = {}
    
    def teardown_method(self):
        """每个测试方法执行后的清理"""
        if hasattr(self, 'api_client'):
            self.api_client.logout()
    
    def test_complete_recruitment_process(self):
        """测试完整的招聘流程 (TC-E2E-001)
        
        流程包括：
        1. 创建用人申请
        2. 第一级审批（部门经理）
        3. 第二级审批（HR总监）
        4. 第三级审批（总经理）
        5. 发布岗位
        6. 提交简历
        7. 筛选简历
        8. 创建面试记录（一面）
        9. 更新一面面试结果为通过
        10. 创建面试记录（二面）
        11. 更新二面面试结果为通过
        12. 创建面试记录（三面）
        13. 更新三面面试结果为通过
        14. 创建录用记录
        15. 发送录用邮件
        16. 更新录用状态为已批准
        """
        
        # ========== 步骤1: 创建用人申请 ==========
        print("\n" + "="*50)
        print("步骤1: 创建用人申请")
        print("="*50)
        
        recruitment_request_data = {
            "requestTitle": "端到端测试岗位-软件工程师",
            "totalRecruitmentCount": 10,
            "vacancyCount": 1,
            "team": "技术部",
            "technicalPlatform": "JAVA",
            "supplementCount": 1,
            "urgentRequirement": "NO",
            "proposedLevel": "P3",
            "experienceYears": "3-5年",
            "skillRequirement": "熟练掌握Java、Spring框架",
            "positionResponsibility": "负责后端开发和系统架构设计",
            "createUserId": "1001",
            "createUserName": "系统用户",
            "updateUserId": "1001",
            "updateUserName": "系统用户"
        }
        
        response = self.api_client.post("/recruitment-request/submit", json=recruitment_request_data)
        assert response.status_code == 200, f"创建用人申请失败，状态码: {response.status_code}"
        
        data = response.json()
        assert data.get("returnCode") == "SUC0000", f"创建用人申请失败，错误信息: {data.get('errorMsg')}"
        
        request_id = data.get("body", {}).get("recruitmentRequestId")
        assert request_id is not None, "创建用人申请后应返回recruitmentRequestId"
        
        self.test_data['request_id'] = request_id
        print(f"✓ 用人申请创建成功，ID: {request_id}")
        
        # ========== 步骤2-4: 三级审批（使用three-level/approve API） ==========
        print("\n" + "="*50)
        print("步骤2-4: 三级审批")
        print("="*50)
        
        # 第一级审批
        first_approval_data = {
            "approvalLevel": "FIRST",
            "approvalUserId": "1002",
            "approvalUserName": "部门经理",
            "approvalResult": "APPROVED",
            "approvalComment": "同意招聘",
            "approvalTime": datetime.now().strftime("%Y-%m-%dT%H:%M:%S")
        }
        
        response = self.api_client.post(f"/recruitment-request/{request_id}/three-level/approve", json=first_approval_data)
        assert response.status_code == 200, f"第一级审批失败，状态码: {response.status_code}"
        
        data = response.json()
        assert data.get("returnCode") == "SUC0000", f"第一级审批失败，错误信息: {data.get('errorMsg')}"
        
        # 重新查询用人申请以获取更新后的状态
        response = self.api_client.get(f"/recruitment-request/{request_id}")
        assert response.status_code == 200, f"查询用人申请失败，状态码: {response.status_code}"
        
        data = response.json()
        assert data.get("returnCode") == "SUC0000", f"查询用人申请失败，错误信息: {data.get('errorMsg')}"
        
        # 验证状态变为1STAPPROVED
        updated_request = data.get("body")
        assert updated_request.get("approvalStatus") == "1STAPPROVED", f"审批状态应为1STAPPROVED，实际为: {updated_request.get('approvalStatus')}"
        
        print(f"✓ 第一级审批成功，状态: {updated_request.get('approvalStatus')}")
        
        # 第二级审批
        second_approval_data = {
            "approvalLevel": "SECOND",
            "approvalUserId": "1003",
            "approvalUserName": "HR总监",
            "approvalResult": "APPROVED",
            "approvalComment": "同意招聘",
            "approvalTime": datetime.now().strftime("%Y-%m-%dT%H:%M:%S")
        }
        
        response = self.api_client.post(f"/recruitment-request/{request_id}/three-level/approve", json=second_approval_data)
        assert response.status_code == 200, f"第二级审批失败，状态码: {response.status_code}"
        
        data = response.json()
        assert data.get("returnCode") == "SUC0000", f"第二级审批失败，错误信息: {data.get('errorMsg')}"
        
        # 重新查询用人申请以获取更新后的状态
        response = self.api_client.get(f"/recruitment-request/{request_id}")
        assert response.status_code == 200, f"查询用人申请失败，状态码: {response.status_code}"
        
        data = response.json()
        assert data.get("returnCode") == "SUC0000", f"查询用人申请失败，错误信息: {data.get('errorMsg')}"
        
        # 验证状态变为2NDAPPROVED
        updated_request = data.get("body")
        assert updated_request.get("approvalStatus") == "2NDAPPROVED", f"审批状态应为2NDAPPROVED，实际为: {updated_request.get('approvalStatus')}"
        
        print(f"✓ 第二级审批成功，状态: {updated_request.get('approvalStatus')}")
        
        # 第三级审批
        third_approval_data = {
            "approvalLevel": "THIRD",
            "approvalUserId": "1004",
            "approvalUserName": "总经理",
            "approvalResult": "APPROVED",
            "approvalComment": "同意招聘",
            "approvalTime": datetime.now().strftime("%Y-%m-%dT%H:%M:%S")
        }
        
        response = self.api_client.post(f"/recruitment-request/{request_id}/three-level/approve", json=third_approval_data)
        assert response.status_code == 200, f"第三级审批失败，状态码: {response.status_code}"
        
        data = response.json()
        assert data.get("returnCode") == "SUC0000", f"第三级审批失败，错误信息: {data.get('errorMsg')}"
        
        # 重新查询用人申请以获取更新后的状态
        response = self.api_client.get(f"/recruitment-request/{request_id}")
        assert response.status_code == 200, f"查询用人申请失败，状态码: {response.status_code}"
        
        data = response.json()
        assert data.get("returnCode") == "SUC0000", f"查询用人申请失败，错误信息: {data.get('errorMsg')}"
        
        # 验证状态变为3RDAPPROVED
        updated_request = data.get("body")
        assert updated_request.get("approvalStatus") == "3RDAPPROVED", f"审批状态应为3RDAPPROVED，实际为: {updated_request.get('approvalStatus')}"
        
        print(f"✓ 第三级审批成功，状态: {updated_request.get('approvalStatus')}")
        
        # ========== 步骤5: 发布岗位 ==========
        print("\n" + "="*50)
        print("步骤5: 发布岗位")
        print("="*50)
        
        publish_data = {
            "publishStatus": "PUBLISHED",
            "publishTime": datetime.now().strftime("%Y-%m-%dT%H:%M:%S")
        }
        
        response = self.api_client.put(f"/recruitment-request/{request_id}/publish-status", json=publish_data)
        assert response.status_code == 200, f"发布岗位失败，状态码: {response.status_code}"
        
        data = response.json()
        assert data.get("returnCode") == "SUC0000", f"发布岗位失败，错误信息: {data.get('errorMsg')}"
        
        # 验证发布状态
        updated_request = data.get("body")
        assert updated_request.get("positionPublishStatus") == "PUBLISHED", f"发布状态应为PUBLISHED，实际为: {updated_request.get('positionPublishStatus')}"
        
        print(f"✓ 岗位发布成功，状态: {updated_request.get('positionPublishStatus')}")
        
        # ========== 步骤6: 提交简历 ==========
        print("\n" + "="*50)
        print("步骤6: 提交简历")
        print("="*50)
        
        # 使用时间戳和随机数确保简历ID唯一
        import time
        import random
        timestamp = int(time.time())
        random_num = random.randint(1000, 9999)
        
        resume_data = {
            "recruitmentRequestId": request_id,
            "jobTitle": "端到端测试岗位-软件工程师",
            "applicantName": f"张三_{timestamp}_{random_num}",
            "contactPhone": f"13800138{random_num}",
            "email": f"zhangsan_{timestamp}_{random_num}@example.com",
            "education": "本科",
            "workExperience": "4年Java开发经验",
            "resumeFileName": f"zhangsan_resume_{timestamp}_{random_num}.pdf",
            "resumeFileUrl": "/uploads/test_resume.pdf",
            "status": "PENDING_SCREENING",
            "createUserId": "1001",
            "createUserName": "系统用户",
            "updateUserId": "1001",
            "updateUserName": "系统用户"
        }
        
        response = self.api_client.post("/resume/submit", json=resume_data)
        assert response.status_code == 200, f"提交简历失败，状态码: {response.status_code}"
        
        data = response.json()
        assert data.get("returnCode") == "SUC0000", f"提交简历失败，错误信息: {data.get('errorMsg')}"
        
        resume_id = data.get("body", {}).get("resumeId")
        assert resume_id is not None, "提交简历后应返回resumeId"
        
        self.test_data['resume_id'] = resume_id
        self.test_data['applicant_name'] = resume_data['applicantName']
        self.test_data['contact_phone'] = resume_data['contactPhone']
        self.test_data['email'] = resume_data['email']
        print(f"✓ 简历提交成功，ID: {resume_id}")
        
        # ========== 步骤7: 筛选简历 ==========
        print("\n" + "="*50)
        print("步骤7: 筛选简历")
        print("="*50)
        
        response = self.api_client.put(f"/resume/{resume_id}/status", json={"status": "SCREENED"})
        assert response.status_code == 200, f"筛选简历失败，状态码: {response.status_code}"
        
        data = response.json()
        assert data.get("returnCode") == "SUC0000", f"筛选简历失败，错误信息: {data.get('errorMsg')}"
        
        # 验证简历状态
        updated_resume = data.get("body")
        assert updated_resume.get("status") == "SCREENED", f"简历状态应为SCREENED，实际为: {updated_resume.get('status')}"
        
        print(f"✓ 简历筛选成功，状态: {updated_resume.get('status')}")
        
        # ========== 步骤8: 创建面试记录（一面） ==========
        print("\n" + "="*50)
        print("步骤8: 创建面试记录（一面）")
        print("="*50)
        
        # 检查简历是否已经有旧的面试记录，如果有则删除
        interview_response = self.api_client.get(f"/interview/resume/{resume_id}")
        if interview_response.status_code == 200:
            interview_data = interview_response.json()
            if interview_data.get("returnCode") == "SUC0000":
                existing_interviews = interview_data.get("body", [])
                if len(existing_interviews) > 0:
                    print(f"⚠ 简历ID {resume_id} 已有 {len(existing_interviews)} 条旧面试记录")
                    print("✓ 删除旧面试记录以创建新的测试数据")
                    # 删除所有旧的面试记录
                    for interview in existing_interviews:
                        interview_id = interview.get("interviewRecordId")
                        delete_response = self.api_client.delete(f"/interview/{interview_id}")
                        if delete_response.status_code == 200:
                            print(f"  ✓ 删除面试记录 ID: {interview_id}")
        
        # 创建新的面试记录
        first_interview_data = {
            "resumeId": resume_id,
            "recruitmentRequestId": request_id,
            "interviewRound": "FIRST_ROUND",
            "interviewerId": "1002",
            "interviewerName": "技术经理",
            "interviewerRole": "DEPARTMENT_HEAD",
            "interviewTime": (datetime.now() + timedelta(days=1)).strftime("%Y-%m-%dT%H:%M:%S"),
            "interviewResult": "PENDING",
            "interviewComment": "",
            "createUserId": "1001",
            "createUserName": "系统用户",
            "updateUserId": "1001",
            "updateUserName": "系统用户"
        }
        
        response = self.api_client.post("/interview/save", json=first_interview_data)
        assert response.status_code == 200, f"创建一面面试记录失败，状态码: {response.status_code}"
        
        data = response.json()
        assert data.get("returnCode") == "SUC0000", f"创建一面面试记录失败，错误信息: {data.get('errorMsg')}"
        
        first_interview_id = data.get("body", {}).get("interviewRecordId")
        assert first_interview_id is not None, "创建面试记录后应返回interviewRecordId"
        
        self.test_data['first_interview_id'] = first_interview_id
        print(f"✓ 一面面试记录创建成功，ID: {first_interview_id}")
        
        # ========== 步骤9: 更新一面面试结果为通过 ==========
        print("\n" + "="*50)
        print("步骤9: 更新一面面试结果为通过")
        print("="*50)
        
        if self.test_data.get('first_interview_id'):
            first_interview_result_data = {
                "interviewResult": "PASSED",
                "interviewComment": "技术能力符合要求，建议进入二面"
            }
            
            response = self.api_client.put(f"/interview/{self.test_data['first_interview_id']}/result", json=first_interview_result_data)
            assert response.status_code == 200, f"更新一面面试结果失败，状态码: {response.status_code}"
            
            data = response.json()
            assert data.get("returnCode") == "SUC0000", f"更新一面面试结果失败，错误信息: {data.get('errorMsg')}"
            
            # 验证面试结果
            updated_interview = data.get("body")
            assert updated_interview.get("interviewResult") == "PASSED", f"面试结果应为PASSED，实际为: {updated_interview.get('interviewResult')}"
            
            print(f"✓ 一面面试结果更新成功，结果: {updated_interview.get('interviewResult')}")
        
        # ========== 步骤10: 创建面试记录（二面） ==========
        print("\n" + "="*50)
        print("步骤10: 创建面试记录（二面）")
        print("="*50)
        
        second_interview_data = {
            "resumeId": resume_id,
            "recruitmentRequestId": request_id,
            "interviewRound": "SECOND_ROUND",
            "interviewerId": "1003",
            "interviewerName": "技术总监",
            "interviewerRole": "TECHNICAL_DIRECTOR",
            "interviewTime": (datetime.now() + timedelta(days=2)).strftime("%Y-%m-%dT%H:%M:%S"),
            "interviewResult": "PENDING",
            "interviewComment": "",
            "createUserId": "1001",
            "createUserName": "系统用户",
            "updateUserId": "1001",
            "updateUserName": "系统用户"
        }
        
        response = self.api_client.post("/interview/save", json=second_interview_data)
        assert response.status_code == 200, f"创建二面面试记录失败，状态码: {response.status_code}"
        
        data = response.json()
        assert data.get("returnCode") == "SUC0000", f"创建二面面试记录失败，错误信息: {data.get('errorMsg')}"
        
        second_interview_id = data.get("body", {}).get("interviewRecordId")
        assert second_interview_id is not None, "创建面试记录后应返回interviewRecordId"
        
        self.test_data['second_interview_id'] = second_interview_id
        print(f"✓ 二面面试记录创建成功，ID: {second_interview_id}")
        
        # ========== 步骤11: 更新二面面试结果为通过 ==========
        print("\n" + "="*50)
        print("步骤11: 更新二面面试结果为通过")
        print("="*50)
        
        if self.test_data.get('second_interview_id'):
            second_interview_result_data = {
                "interviewResult": "PASSED",
                "interviewComment": "技术深度和架构能力符合要求，建议进入三面"
            }
            
            response = self.api_client.put(f"/interview/{self.test_data['second_interview_id']}/result", json=second_interview_result_data)
            assert response.status_code == 200, f"更新二面面试结果失败，状态码: {response.status_code}"
            
            data = response.json()
            assert data.get("returnCode") == "SUC0000", f"更新二面面试结果失败，错误信息: {data.get('errorMsg')}"
            
            # 验证面试结果
            updated_interview = data.get("body")
            assert updated_interview.get("interviewResult") == "PASSED", f"面试结果应为PASSED，实际为: {updated_interview.get('interviewResult')}"
            
            print(f"✓ 二面面试结果更新成功，结果: {updated_interview.get('interviewResult')}")
        
        # ========== 步骤12: 创建面试记录（三面） ==========
        print("\n" + "="*50)
        print("步骤12: 创建面试记录（三面）")
        print("="*50)
        
        third_interview_data = {
            "resumeId": resume_id,
            "recruitmentRequestId": request_id,
            "interviewRound": "THIRD_ROUND",
            "interviewerId": "1004",
            "interviewerName": "总经理",
            "interviewerRole": "GENERAL_MANAGER",
            "interviewTime": (datetime.now() + timedelta(days=3)).strftime("%Y-%m-%dT%H:%M:%S"),
            "interviewResult": "PENDING",
            "interviewComment": "",
            "createUserId": "1001",
            "createUserName": "系统用户",
            "updateUserId": "1001",
            "updateUserName": "系统用户"
        }
        
        response = self.api_client.post("/interview/save", json=third_interview_data)
        assert response.status_code == 200, f"创建三面面试记录失败，状态码: {response.status_code}"
        
        data = response.json()
        assert data.get("returnCode") == "SUC0000", f"创建三面面试记录失败，错误信息: {data.get('errorMsg')}"
        
        third_interview_id = data.get("body", {}).get("interviewRecordId")
        assert third_interview_id is not None, "创建面试记录后应返回interviewRecordId"
        
        self.test_data['third_interview_id'] = third_interview_id
        print(f"✓ 三面面试记录创建成功，ID: {third_interview_id}")
        
        # ========== 步骤13: 更新三面面试结果为通过 ==========
        print("\n" + "="*50)
        print("步骤13: 更新三面面试结果为通过")
        print("="*50)
        
        if self.test_data.get('third_interview_id'):
            third_interview_result_data = {
                "interviewResult": "PASSED",
                "interviewComment": "综合素质优秀，建议录用"
            }
            
            response = self.api_client.put(f"/interview/{self.test_data['third_interview_id']}/result", json=third_interview_result_data)
            assert response.status_code == 200, f"更新三面面试结果失败，状态码: {response.status_code}"
            
            data = response.json()
            assert data.get("returnCode") == "SUC0000", f"更新三面面试结果失败，错误信息: {data.get('errorMsg')}"
            
            # 验证面试结果
            updated_interview = data.get("body")
            assert updated_interview.get("interviewResult") == "PASSED", f"面试结果应为PASSED，实际为: {updated_interview.get('interviewResult')}"
            
            print(f"✓ 三面面试结果更新成功，结果: {updated_interview.get('interviewResult')}")
        
        # ========== 步骤14: 创建录用记录 ==========
        print("\n" + "="*50)
        print("步骤14: 创建录用记录")
        print("="*50)
        
        offer_data = {
            "resumeId": resume_id,
            "recruitmentRequestId": request_id,
            "candidateName": self.test_data.get("applicant_name", "张三"),
            "contactPhone": self.test_data.get("contact_phone", "13800138000"),
            "email": self.test_data.get("email", "zhangsan@example.com"),
            "position": "端到端测试岗位-软件工程师",
            "offerSalary": "25000",
            "offerStartDate": (datetime.now() + timedelta(days=30)).strftime("%Y-%m-%d"),
            "offerStatus": "PENDING_ACCEPTANCE",
            "offerComment": "欢迎加入我们的团队",
            "createUserId": "1001",
            "createUserName": "系统用户",
            "updateUserId": "1001",
            "updateUserName": "系统用户"
        }
        
        response = self.api_client.post("/offer/save", json=offer_data)
        assert response.status_code == 200, f"创建录用记录失败，状态码: {response.status_code}"
        
        data = response.json()
        assert data.get("returnCode") == "SUC0000", f"创建录用记录失败，错误信息: {data.get('errorMsg')}"
        
        offer_id = data.get("body", {}).get("offerRecordId")
        assert offer_id is not None, "创建录用记录后应返回offerRecordId"
        
        self.test_data['offer_id'] = offer_id
        print(f"✓ 录用记录创建成功，ID: {offer_id}")
        
        # ========== 步骤15: 发送录用邮件 ==========
        print("\n" + "="*50)
        print("步骤15: 发送录用邮件")
        print("="*50)
        
        response = self.api_client.post(f"/offer/{offer_id}/send-email")
        assert response.status_code == 200, f"发送录用邮件失败，状态码: {response.status_code}"
        
        data = response.json()
        assert data.get("returnCode") == "SUC0000", f"发送录用邮件失败，错误信息: {data.get('errorMsg')}"
        
        print(f"✓ 录用邮件发送成功")
        
        # ========== 步骤16: 更新录用状态为已批准 ==========
        print("\n" + "="*50)
        print("步骤16: 更新录用状态为已批准")
        print("="*50)
        
        response = self.api_client.put(f"/offer/{offer_id}/status?status=APPROVED")
        assert response.status_code == 200, f"更新录用状态失败，状态码: {response.status_code}"
        
        data = response.json()
        assert data.get("returnCode") == "SUC0000", f"更新录用状态失败，错误信息: {data.get('errorMsg')}"
        
        # 验证录用状态
        updated_offer = data.get("body")
        assert updated_offer.get("status") == "APPROVED", f"录用状态应为APPROVED，实际为: {updated_offer.get('status')}"
        
        print(f"✓ 录用状态更新成功，状态: {updated_offer.get('status')}")
        
        # ========== 流程完成总结 ==========
        print("\n" + "="*50)
        print("端到端招聘流程测试完成")
        print("="*50)
        print(f"用人申请ID: {request_id}")
        print(f"简历ID: {resume_id}")
        print(f"一面面试ID: {first_interview_id}")
        print(f"二面面试ID: {second_interview_id}")
        print(f"三面面试ID: {third_interview_id}")
        print(f"录用记录ID: {offer_id}")
        print(f"录用状态: {updated_offer.get('status')}")
        print("="*50)
        print("✓ 所有步骤执行成功！")
        print("="*50)
    
    def test_recruitment_process_with_rejection(self):
        """测试包含拒绝的招聘流程 (TC-E2E-002)
        
        测试在面试环节被拒绝的情况：
        1. 创建用人申请
        2. 三级审批
        3. 发布岗位
        4. 提交简历
        5. 筛选简历
        6. 创建面试记录（一面）
        7. 更新一面面试结果为拒绝
        """
        
        # ========== 步骤1: 创建用人申请 ==========
        print("\n" + "="*50)
        print("步骤1: 创建用人申请")
        print("="*50)
        
        recruitment_request_data = {
            "requestTitle": "端到端测试岗位-被拒绝案例",
            "totalRecruitmentCount": 10,
            "vacancyCount": 1,
            "team": "技术部",
            "technicalPlatform": "JAVA",
            "supplementCount": 1,
            "urgentRequirement": "NO",
            "proposedLevel": "P3",
            "experienceYears": "3-5年",
            "skillRequirement": "熟练掌握Java、Spring框架",
            "positionResponsibility": "负责后端开发和系统架构设计",
            "createUserId": "1001",
            "createUserName": "系统用户",
            "updateUserId": "1001",
            "updateUserName": "系统用户"
        }
        
        response = self.api_client.post("/recruitment-request/submit", json=recruitment_request_data)
        assert response.status_code == 200, f"创建用人申请失败，状态码: {response.status_code}"
        
        data = response.json()
        assert data.get("returnCode") == "SUC0000", f"创建用人申请失败，错误信息: {data.get('errorMsg')}"
        
        request_id = data.get("body", {}).get("recruitmentRequestId")
        assert request_id is not None, "创建用人申请后应返回recruitmentRequestId"
        
        print(f"✓ 用人申请创建成功，ID: {request_id}")
        
        # ========== 步骤2-4: 三级审批（简化处理，直接调用三级审批API） ==========
        print("\n" + "="*50)
        print("步骤2-4: 三级审批")
        print("="*50)
        
        response = self.api_client.post(f"/recruitment-request/{request_id}/three-level/approve")
        assert response.status_code == 200, f"三级审批失败，状态码: {response.status_code}"
        
        data = response.json()
        assert data.get("returnCode") == "SUC0000", f"三级审批失败，错误信息: {data.get('errorMsg')}"
        
        print(f"✓ 三级审批成功")
        
        # ========== 步骤5: 发布岗位 ==========
        print("\n" + "="*50)
        print("步骤5: 发布岗位")
        print("="*50)
        
        publish_data = {
            "publishStatus": "PUBLISHED",
            "publishTime": datetime.now().strftime("%Y-%m-%dT%H:%M:%S")
        }
        
        response = self.api_client.put(f"/recruitment-request/{request_id}/publish-status", json=publish_data)
        assert response.status_code == 200, f"发布岗位失败，状态码: {response.status_code}"
        
        data = response.json()
        assert data.get("returnCode") == "SUC0000", f"发布岗位失败，错误信息: {data.get('errorMsg')}"
        
        print(f"✓ 岗位发布成功")
        
        # ========== 步骤6: 提交简历 ==========
        print("\n" + "="*50)
        print("步骤6: 提交简历")
        print("="*50)
        
        resume_data = {
            "recruitmentRequestId": request_id,
            "jobTitle": "端到端测试岗位-被拒绝案例",
            "applicantName": "李四",
            "contactPhone": "13900139000",
            "email": "lisi@example.com",
            "education": "本科",
            "workExperience": "2年Java开发经验",
            "resumeFileName": "lisi_resume.pdf",
            "resumeFileUrl": "/uploads/test_resume2.pdf",
            "status": "PENDING_SCREENING",
            "createUserId": "1001",
            "createUserName": "系统用户",
            "updateUserId": "1001",
            "updateUserName": "系统用户"
        }
        
        response = self.api_client.post("/resume/submit", json=resume_data)
        assert response.status_code == 200, f"提交简历失败，状态码: {response.status_code}"
        
        data = response.json()
        assert data.get("returnCode") == "SUC0000", f"提交简历失败，错误信息: {data.get('errorMsg')}"
        
        resume_id = data.get("body", {}).get("resumeId")
        assert resume_id is not None, "提交简历后应返回resumeId"
        
        print(f"✓ 简历提交成功，ID: {resume_id}")
        
        # ========== 步骤7: 筛选简历 ==========
        print("\n" + "="*50)
        print("步骤7: 筛选简历")
        print("="*50)
        
        response = self.api_client.put(f"/resume/{resume_id}/status", json={"status": "SCREENED"})
        assert response.status_code == 200, f"筛选简历失败，状态码: {response.status_code}"
        
        data = response.json()
        assert data.get("returnCode") == "SUC0000", f"筛选简历失败，错误信息: {data.get('errorMsg')}"
        
        print(f"✓ 简历筛选成功")
        
        # ========== 步骤8: 创建面试记录（一面） ==========
        print("\n" + "="*50)
        print("步骤8: 创建面试记录（一面）")
        print("="*50)
        
        interview_data = {
            "resumeId": resume_id,
            "recruitmentRequestId": request_id,
            "interviewRound": "FIRST_ROUND",
            "interviewerId": "1002",
            "interviewerName": "技术经理",
            "interviewerRole": "DEPARTMENT_HEAD",
            "interviewTime": (datetime.now() + timedelta(days=1)).strftime("%Y-%m-%dT%H:%M:%S"),
            "interviewResult": "PENDING",
            "interviewComment": "",
            "createUserId": "1001",
            "createUserName": "系统用户",
            "updateUserId": "1001",
            "updateUserName": "系统用户"
        }
        
        response = self.api_client.post("/interview/save", json=interview_data)
        assert response.status_code == 200, f"创建面试记录失败，状态码: {response.status_code}"
        
        data = response.json()
        assert data.get("returnCode") == "SUC0000", f"创建面试记录失败，错误信息: {data.get('errorMsg')}"
        
        interview_id = data.get("body", {}).get("interviewRecordId")
        assert interview_id is not None, "创建面试记录后应返回interviewRecordId"
        
        print(f"✓ 面试记录创建成功，ID: {interview_id}")
        
        # ========== 步骤9: 更新面试结果为拒绝 ==========
        print("\n" + "="*50)
        print("步骤9: 更新面试结果为拒绝")
        print("="*50)
        
        interview_result_data = {
            "interviewResult": "REJECTED",
            "interviewComment": "技术能力不符合要求，建议拒绝"
        }
        
        response = self.api_client.put(f"/interview/{interview_id}/result", json=interview_result_data)
        assert response.status_code == 200, f"更新面试结果失败，状态码: {response.status_code}"
        
        data = response.json()
        assert data.get("returnCode") == "SUC0000", f"更新面试结果失败，错误信息: {data.get('errorMsg')}"
        
        # 验证面试结果
        updated_interview = data.get("body")
        assert updated_interview.get("interviewResult") == "REJECTED", f"面试结果应为REJECTED，实际为: {updated_interview.get('interviewResult')}"
        
        print(f"✓ 面试结果更新成功，结果: {updated_interview.get('interviewResult')}")
        
        # ========== 流程完成总结 ==========
        print("\n" + "="*50)
        print("包含拒绝的招聘流程测试完成")
        print("="*50)
        print(f"用人申请ID: {request_id}")
        print(f"简历ID: {resume_id}")
        print(f"面试ID: {interview_id}")
        print(f"面试结果: REJECTED")
        print("="*50)
        print("✓ 所有步骤执行成功！")
        print("="*50)
    
    def test_interview_count_constraint(self):
        """测试面试次数约束 (TC-E2E-003)
        
        测试业务规则：一个候选人最多只能进行3次面试
        1. 创建用人申请
        2. 三级审批
        3. 发布岗位
        4. 提交简历
        5. 筛选简历
        6. 创建3次面试记录
        7. 尝试创建第4次面试记录（应该失败）
        """
        
        # ========== 步骤1: 创建用人申请 ==========
        print("\n" + "="*50)
        print("步骤1: 创建用人申请")
        print("="*50)
        
        recruitment_request_data = {
            "requestTitle": "端到端测试岗位-面试次数约束",
            "totalRecruitmentCount": 10,
            "vacancyCount": 1,
            "team": "技术部",
            "technicalPlatform": "JAVA",
            "supplementCount": 1,
            "urgentRequirement": "NO",
            "proposedLevel": "P3",
            "experienceYears": "3-5年",
            "skillRequirement": "熟练掌握Java、Spring框架",
            "positionResponsibility": "负责后端开发和系统架构设计",
            "createUserId": "1001",
            "createUserName": "系统用户",
            "updateUserId": "1001",
            "updateUserName": "系统用户"
        }
        
        response = self.api_client.post("/recruitment-request/submit", json=recruitment_request_data)
        assert response.status_code == 200, f"创建用人申请失败，状态码: {response.status_code}"
        
        data = response.json()
        assert data.get("returnCode") == "SUC0000", f"创建用人申请失败，错误信息: {data.get('errorMsg')}"
        
        request_id = data.get("body", {}).get("recruitmentRequestId")
        assert request_id is not None, "创建用人申请后应返回recruitmentRequestId"
        
        print(f"✓ 用人申请创建成功，ID: {request_id}")
        
        # ========== 步骤2-4: 三级审批和发布岗位（简化处理） ==========
        response = self.api_client.post(f"/recruitment-request/{request_id}/three-level-approve")
        assert response.status_code == 200
        
        publish_data = {
            "publishStatus": "PUBLISHED",
            "publishTime": datetime.now().strftime("%Y-%m-%dT%H:%M:%S")
        }
        response = self.api_client.put(f"/recruitment-request/{request_id}/publish-status", json=publish_data)
        assert response.status_code == 200
        
        print(f"✓ 三级审批和岗位发布成功")
        
        # ========== 步骤5: 提交简历 ==========
        resume_data = {
            "recruitmentRequestId": request_id,
            "jobTitle": "端到端测试岗位-面试次数约束",
            "applicantName": "王五",
            "contactPhone": "13700137000",
            "email": "wangwu@example.com",
            "education": "本科",
            "workExperience": "5年Java开发经验",
            "resumeFileName": "wangwu_resume.pdf",
            "resumeFileUrl": "/uploads/test_resume3.pdf",
            "status": "PENDING_SCREENING",
            "createUserId": "1001",
            "createUserName": "系统用户",
            "updateUserId": "1001",
            "updateUserName": "系统用户"
        }
        
        response = self.api_client.post("/resume/submit", json=resume_data)
        assert response.status_code == 200
        data = response.json()
        assert data.get("returnCode") == "SUC0000"
        
        resume_id = data.get("body", {}).get("resumeId")
        assert resume_id is not None
        
        print(f"✓ 简历提交成功，ID: {resume_id}")
        
        # ========== 步骤6: 筛选简历 ==========
        response = self.api_client.put(f"/resume/{resume_id}/status", json={"status": "SCREENED"})
        assert response.status_code == 200
        
        print(f"✓ 简历筛选成功")
        
        # ========== 步骤7: 创建3次面试记录 ==========
        interview_rounds = [
            ("FIRST_ROUND", "技术经理", "DEPARTMENT_HEAD", "1002"),
            ("SECOND_ROUND", "技术总监", "TECHNICAL_DIRECTOR", "1003"),
            ("THIRD_ROUND", "总经理", "GENERAL_MANAGER", "1004")
        ]
        
        interview_ids = []
        for i, (round_name, interviewer_name, interviewer_role, interviewer_id) in enumerate(interview_rounds, 1):
            print(f"\n创建第{i}次面试记录...")
            
            interview_data = {
                "resumeId": resume_id,
                "recruitmentRequestId": request_id,
                "interviewRound": round_name,
                "interviewerId": interviewer_id,
                "interviewerName": interviewer_name,
                "interviewerRole": interviewer_role,
                "interviewTime": (datetime.now() + timedelta(days=i)).strftime("%Y-%m-%dT%H:%M:%S"),
                "interviewResult": "PENDING",
                "interviewComment": "",
                "createUserId": "1001",
                "createUserName": "系统用户",
                "updateUserId": "1001",
                "updateUserName": "系统用户"
            }
            
            response = self.api_client.post("/interview/save", json=interview_data)
            assert response.status_code == 200, f"创建第{i}次面试记录失败，状态码: {response.status_code}"
            
            data = response.json()
            assert data.get("returnCode") == "SUC0000", f"创建第{i}次面试记录失败，错误信息: {data.get('errorMsg')}"
            
            interview_id = data.get("body", {}).get("interviewRecordId")
            assert interview_id is not None, f"创建第{i}次面试记录后应返回interviewRecordId"
            
            interview_ids.append(interview_id)
            print(f"✓ 第{i}次面试记录创建成功，ID: {interview_id}")
        
        # ========== 步骤8: 尝试创建第4次面试记录（应该失败） ==========
        print("\n" + "="*50)
        print("步骤8: 尝试创建第4次面试记录（应该失败）")
        print("="*50)
        
        fourth_interview_data = {
            "resumeId": resume_id,
            "recruitmentRequestId": request_id,
            "interviewRound": "FIRST_ROUND",
            "interviewerId": "1002",
            "interviewerName": "技术经理",
            "interviewerRole": "DEPARTMENT_HEAD",
            "interviewTime": (datetime.now() + timedelta(days=4)).strftime("%Y-%m-%dT%H:%M:%S"),
            "interviewResult": "PENDING",
            "interviewComment": "",
            "createUserId": "1001",
            "createUserName": "系统用户",
            "updateUserId": "1001",
            "updateUserName": "系统用户"
        }
        
        response = self.api_client.post("/interview/save", json=fourth_interview_data)
        
        # 验证返回错误
        assert response.status_code == 200, f"请求应成功返回，状态码: {response.status_code}"
        
        data = response.json()
        assert data.get("returnCode") != "SUC0000", f"创建第4次面试记录应该失败，但返回了成功"
        assert "3次面试" in data.get("errorMsg", ""), f"错误信息应包含'3次面试'，实际: {data.get('errorMsg')}"
        
        print(f"✓ 第4次面试记录创建被正确拒绝")
        print(f"✓ 错误信息: {data.get('errorMsg')}")
        
        # ========== 流程完成总结 ==========
        print("\n" + "="*50)
        print("面试次数约束测试完成")
        print("="*50)
        print(f"用人申请ID: {request_id}")
        print(f"简历ID: {resume_id}")
        print(f"已创建面试次数: {len(interview_ids)}")
        print(f"面试IDs: {interview_ids}")
        print("="*50)
        print("✓ 所有步骤执行成功！")
        print("✓ 面试次数约束正确生效！")
        print("="*50)

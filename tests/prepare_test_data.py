# 测试数据准备脚本

import sys
import os
import json
from datetime import datetime, timedelta

# 添加项目根目录到Python搜索路径
sys.path.insert(0, os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

from common.api_client import ApiClient
from common.config import config

class TestDataPreparer:
    def __init__(self):
        self.api_client = ApiClient()
        self.api_client.login(
            config.TEST_USERS["admin"]["username"],
            config.TEST_USERS["admin"]["password"]
        )
        self.test_data = {}
    
    def cleanup(self):
        """清理测试数据"""
        if hasattr(self, 'api_client'):
            self.api_client.logout()
    
    def create_test_users(self):
        """创建测试用户"""
        print("创建测试用户...")
        import time
        timestamp = int(time.time())
        
        users = [
            {
                "username": f"test_recruiter_{timestamp}",
                "password": "password123",
                "realName": "测试招聘专员",
                "email": f"test_recruiter_{timestamp}@example.com",
                "phone": "13800138001",
                "department": "人力资源部",
                "position": "招聘专员",
                "status": "ACTIVE"
            },
            {
                "username": f"test_interviewer1_{timestamp}",
                "password": "password123",
                "realName": "测试面试官1",
                "email": f"test_interviewer1_{timestamp}@example.com",
                "phone": "13800138002",
                "department": "技术部",
                "position": "技术经理",
                "status": "ACTIVE"
            },
            {
                "username": f"test_interviewer2_{timestamp}",
                "password": "password123",
                "realName": "测试面试官2",
                "email": f"test_interviewer2_{timestamp}@example.com",
                "phone": "13800138003",
                "department": "技术部",
                "position": "团队经理",
                "status": "ACTIVE"
            },
            {
                "username": f"test_interviewer3_{timestamp}",
                "password": "password123",
                "realName": "测试面试官3",
                "email": f"test_interviewer3_{timestamp}@example.com",
                "phone": "13800138004",
                "department": "技术部",
                "position": "分管总",
                "status": "ACTIVE"
            }
        ]
        
        user_ids = []
        for user in users:
            try:
                response = self.api_client.post("/users", json=user)
                if response.status_code == 200:
                    data = response.json()
                    if data.get("returnCode") == "SUC0000":
                        # UserController返回的body是字符串，需要通过查询所有用户获取userId
                        username = user['username']
                        get_response = self.api_client.get("/users")
                        if get_response.status_code == 200:
                            get_data = get_response.json()
                            if get_data.get("returnCode") == "SUC0000":
                                all_users = get_data.get("body", [])
                                for u in all_users:
                                    if u.get("username") == username:
                                        user_id = u.get("userId")
                                        user_ids.append(user_id)
                                        print(f"  创建用户成功: {user['username']} (ID: {user_id})")
                                        break
                                continue
                        print(f"  创建用户成功但无法获取ID: {user['username']}")
                    else:
                        print(f"  创建用户失败: {user['username']}, 错误: {data.get('errorMsg')}")
                else:
                    print(f"  创建用户失败: {user['username']}, 状态码: {response.status_code}")
            except Exception as e:
                print(f"  创建用户异常: {user['username']}, 错误: {str(e)}")
        
        self.test_data['user_ids'] = user_ids
        return user_ids
    
    def create_test_recruitment_requests(self, count=5):
        """创建测试用人申请"""
        print(f"创建{count}个测试用人申请...")
        request_ids = []
        
        for i in range(count):
            request_data = {
                "requestTitle": f"测试岗位{i+1}",
                "team": "零售",
                "totalRecruitmentCount": 10,
                "vacancyCount": 1,
                "supplementCount": 1,
                "technicalPlatform": "Java",
                "proposedLevel": "中级",
                "experienceYears": "3-5年",
                "skillRequirement": f"测试技能要求{i+1}",
                "positionResponsibility": f"测试岗位职责{i+1}",
                "urgentRequirement": "否",
                "createUserId": "1001",
                "createUserName": "系统用户",
                "updateUserId": "1001",
                "updateUserName": "系统用户"
            }
            
            try:
                # 保存草稿
                save_response = self.api_client.post("/recruitment-request/save-draft", json=request_data)
                if save_response.status_code == 200:
                    save_data = save_response.json()
                    if save_data.get("returnCode") == "SUC0000":
                        request_id = save_data.get("body", {}).get("recruitmentRequestId")
                        
                        # 提交申请
                        submit_response = self.api_client.post("/recruitment-request/submit", json=request_data)
                        if submit_response.status_code == 200:
                            submit_data = submit_response.json()
                            if submit_data.get("returnCode") == "SUC0000":
                                request_ids.append(request_id)
                                print(f"  创建用人申请成功: {request_data['requestTitle']} (ID: {request_id})")
                            else:
                                print(f"  提交用人申请失败: {request_data['requestTitle']}, 错误: {submit_data.get('errorMsg')}")
                        else:
                            print(f"  提交用人申请失败: {request_data['requestTitle']}, 状态码: {submit_response.status_code}")
                    else:
                        print(f"  保存用人申请失败: {request_data['requestTitle']}, 错误: {save_data.get('errorMsg')}")
                else:
                    print(f"  保存用人申请失败: {request_data['requestTitle']}, 状态码: {save_response.status_code}")
            except Exception as e:
                print(f"  创建用人申请异常: {request_data['requestTitle']}, 错误: {str(e)}")
        
        self.test_data['request_ids'] = request_ids
        return request_ids
    
    def create_test_resumes(self, count, published_request_ids):
        """创建测试简历"""
        print(f"创建{count}个测试简历...")
        resume_ids = []
        
        for i in range(count):
            request_id = published_request_ids[i % len(published_request_ids)]
            
            resume_data = {
                "jobTitle": "软件工程师",
                "applicantName": f"测试候选人{i+1}",
                "contactPhone": f"138001380{10+i:02d}",
                "email": f"test_candidate{i+1}@example.com",
                "education": "本科",
                "workExperience": f"{i+1}年",
                "resumeFileName": f"test_resume_{i+1}.pdf",
                "resumeFileUrl": f"/resumes/test_resume_{i+1}.pdf",
                "status": "PENDING",
                "interviewStatus": "PENDING",
                "recruitmentRequestId": request_id,
                "createUserId": "1001",
                "createUserName": "系统用户",
                "updateUserId": "1001",
                "updateUserName": "系统用户"
            }
            
            try:
                response = self.api_client.post("/resume/submit", json=resume_data)
                if response.status_code == 200:
                    data = response.json()
                    if data.get("returnCode") == "SUC0000":
                        resume_id = data.get("body", {}).get("resumeId")
                        resume_ids.append(resume_id)
                        print(f"  创建简历成功: {resume_data['applicantName']} (ID: {resume_id})")
                    else:
                        print(f"  创建简历失败: {resume_data['applicantName']}, 错误: {data.get('errorMsg')}")
                else:
                    print(f"  创建简历失败: {resume_data['applicantName']}, 状态码: {response.status_code}")
            except Exception as e:
                print(f"  创建简历异常: {resume_data['applicantName']}, 错误: {str(e)}")
        
        self.test_data['resume_ids'] = resume_ids
        return resume_ids
    
    def create_test_interviews(self, resume_ids, request_ids):
        """创建测试面试记录"""
        print("创建测试面试记录...")
        interview_ids = []
        
        # 为每个简历创建3轮面试
        for i, resume_id in enumerate(resume_ids[:5]):  # 只为前5个简历创建面试
            request_id = request_ids[i % len(request_ids)]
            
            # 一面（室经理）
            first_round_data = {
                "resumeId": resume_id,
                "recruitmentRequestId": request_id,
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
            
            try:
                first_response = self.api_client.post("/interview/save", json=first_round_data)
                if first_response.status_code == 200:
                    first_data = first_response.json()
                    if first_data.get("returnCode") == "SUC0000":
                        interview_id = first_data.get("body", {}).get("interviewRecordId")
                        interview_ids.append(interview_id)
                        print(f"  创建一面成功: 简历ID {resume_id} (面试ID: {interview_id})")
            except Exception as e:
                print(f"  创建一面异常: 简历ID {resume_id}, 错误: {str(e)}")
            
            # 二面（团队经理）
            second_round_data = {
                "resumeId": resume_id,
                "recruitmentRequestId": request_id,
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
            
            try:
                second_response = self.api_client.post("/interview/save", json=second_round_data)
                if second_response.status_code == 200:
                    second_data = second_response.json()
                    if second_data.get("returnCode") == "SUC0000":
                        interview_id = second_data.get("body", {}).get("interviewRecordId")
                        interview_ids.append(interview_id)
                        print(f"  创建二面成功: 简历ID {resume_id} (面试ID: {interview_id})")
            except Exception as e:
                print(f"  创建二面异常: 简历ID {resume_id}, 错误: {str(e)}")
            
            # 三面（分管总）
            third_round_data = {
                "resumeId": resume_id,
                "recruitmentRequestId": request_id,
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
            
            try:
                third_response = self.api_client.post("/interview/save", json=third_round_data)
                if third_response.status_code == 200:
                    third_data = third_response.json()
                    if third_data.get("returnCode") == "SUC0000":
                        interview_id = third_data.get("body", {}).get("interviewRecordId")
                        interview_ids.append(interview_id)
                        print(f"  创建三面成功: 简历ID {resume_id} (面试ID: {interview_id})")
            except Exception as e:
                print(f"  创建三面异常: 简历ID {resume_id}, 错误: {str(e)}")
        
        self.test_data['interview_ids'] = interview_ids
        return interview_ids
    
    def screen_resumes(self, resume_ids):
        """筛选简历"""
        print("筛选简历...")
        screened_ids = []
        
        for resume_id in resume_ids:
            try:
                response = self.api_client.put(f"/resume/{resume_id}/status", json={"status": "SCREENED"})
                if response.status_code == 200:
                    data = response.json()
                    if data.get("returnCode") == "SUC0000":
                        screened_ids.append(resume_id)
                        print(f"  筛选简历成功: ID {resume_id}")
                    else:
                        print(f"  筛选简历失败: ID {resume_id}, 错误: {data.get('errorMsg')}")
                else:
                    print(f"  筛选简历失败: ID {resume_id}, 状态码: {response.status_code}")
            except Exception as e:
                print(f"  筛选简历异常: ID {resume_id}, 错误: {str(e)}")
        
        self.test_data['screened_resume_ids'] = screened_ids
        return screened_ids
    
    def update_interview_results(self, resume_ids):
        """更新面试结果为通过"""
        print("更新面试结果为通过...")
        passed_ids = []
        
        for resume_id in resume_ids:
            try:
                # 获取该简历的所有面试记录
                response = self.api_client.get(f"/interview/resume/{resume_id}")
                if response.status_code == 200:
                    data = response.json()
                    if data.get("returnCode") == "SUC0000":
                        interviews = data.get("body", [])
                        # 更新所有面试记录为通过
                        all_passed = True
                        for interview in interviews:
                            interview_id = interview.get("interviewRecordId")
                            update_response = self.api_client.put(
                                f"/interview/{interview_id}/result",
                                json={"interviewResult": "PASSED"}
                            )
                            if update_response.status_code != 200:
                                all_passed = False
                                break
                        
                        if all_passed:
                            passed_ids.append(resume_id)
                            print(f"  更新面试结果成功: 简历ID {resume_id}")
                        else:
                            print(f"  更新面试结果失败: 简历ID {resume_id}")
                    else:
                        print(f"  获取面试记录失败: 简历ID {resume_id}")
                else:
                    print(f"  获取面试记录失败: 简历ID {resume_id}, 状态码: {response.status_code}")
            except Exception as e:
                print(f"  更新面试结果异常: 简历ID {resume_id}, 错误: {str(e)}")
        
        self.test_data['passed_resume_ids'] = passed_ids
        return passed_ids
    
    def create_test_offers(self, resume_ids, request_ids):
        """创建测试录用记录"""
        print("创建测试录用记录...")
        offer_ids = []
        
        for i, resume_id in enumerate(resume_ids[:3]):  # 只为前3个简历创建录用记录
            request_id = request_ids[i % len(request_ids)]
            
            offer_data = {
                "resumeId": resume_id,
                "recruitmentRequestId": request_id,
                "candidateName": f"测试候选人{i+1}",
                "contactPhone": f"138001380{10+i:02d}",
                "email": f"test_candidate{i+1}@example.com",
                "position": "软件工程师",
                "status": "PENDING",
                "createUserId": "1001",
                "createUserName": "系统用户",
                "updateUserId": "1001",
                "updateUserName": "系统用户"
            }
            
            try:
                response = self.api_client.post("/offer/save", json=offer_data)
                if response.status_code == 200:
                    data = response.json()
                    if data.get("returnCode") == "SUC0000":
                        offer_id = data.get("body", {}).get("offerRecordId")
                        offer_ids.append(offer_id)
                        print(f"  创建录用记录成功: {offer_data['candidateName']} (ID: {offer_id})")
                    else:
                        print(f"  创建录用记录失败: {offer_data['candidateName']}, 错误: {data.get('errorMsg')}")
                else:
                    print(f"  创建录用记录失败: {offer_data['candidateName']}, 状态码: {response.status_code}")
            except Exception as e:
                print(f"  创建录用记录异常: {offer_data['candidateName']}, 错误: {str(e)}")
        
        self.test_data['offer_ids'] = offer_ids
        return offer_ids
    
    def approve_recruitment_requests(self, request_ids):
        """审批用人申请"""
        print("审批用人申请...")
        approved_ids = []
        
        for request_id in request_ids:
            try:
                # 第一级审批
                approve1_response = self.api_client.post(
                    f"/recruitment-request/{request_id}/three-level/approve",
                    json={
                        "approvalUserId": "1001",
                        "approvalUserName": "系统用户",
                        "approvalComment": "同意"
                    }
                )
                
                if approve1_response.status_code == 200:
                    # 第二级审批
                    approve2_response = self.api_client.post(
                        f"/recruitment-request/{request_id}/three-level/approve",
                        json={
                            "approvalUserId": "1001",
                            "approvalUserName": "系统用户",
                            "approvalComment": "同意"
                        }
                    )
                    
                    if approve2_response.status_code == 200:
                        # 第三级审批
                        approve3_response = self.api_client.post(
                            f"/recruitment-request/{request_id}/three-level/approve",
                            json={
                                "approvalUserId": "1001",
                                "approvalUserName": "系统用户",
                                "approvalComment": "同意"
                            }
                        )
                        
                        if approve3_response.status_code == 200:
                            approved_ids.append(request_id)
                            print(f"  审批用人申请成功: ID {request_id}")
                        else:
                            print(f"  第三级审批失败: ID {request_id}")
                    else:
                        print(f"  第二级审批失败: ID {request_id}")
                else:
                    print(f"  第一级审批失败: ID {request_id}")
            except Exception as e:
                print(f"  审批用人申请异常: ID {request_id}, 错误: {str(e)}")
        
        self.test_data['approved_request_ids'] = approved_ids
        return approved_ids
    
    def publish_positions(self, request_ids):
        """发布岗位"""
        print("发布岗位...")
        published_ids = []
        
        for request_id in request_ids:
            try:
                response = self.api_client.put(f"/recruitment-request/{request_id}/publish-status", json={"publishStatus": "PUBLISHED"})
                if response.status_code == 200:
                    data = response.json()
                    if data.get("returnCode") == "SUC0000":
                        published_ids.append(request_id)
                        print(f"  发布岗位成功: ID {request_id}")
                    else:
                        print(f"  发布岗位失败: ID {request_id}, 错误: {data.get('errorMsg')}")
                else:
                    print(f"  发布岗位失败: ID {request_id}, 状态码: {response.status_code}")
            except Exception as e:
                print(f"  发布岗位异常: ID {request_id}, 错误: {str(e)}")
        
        self.test_data['published_request_ids'] = published_ids
        return published_ids
    
    def prepare_all_test_data(self):
        """准备所有测试数据 - 按照正确的招聘流程顺序"""
        print("=" * 50)
        print("开始准备测试数据")
        print("=" * 50)
        
        # 1. 创建测试用户
        self.create_test_users()
        
        # 2. 创建测试用人申请
        request_ids = self.create_test_recruitment_requests(5)
        
        # 3. 审批用人申请（三级审批）
        approved_ids = self.approve_recruitment_requests(request_ids)
        
        # 4. 发布岗位（岗位发布成功后才能提交简历）
        published_ids = self.publish_positions(approved_ids)
        
        # 5. 创建简历（关联已发布的岗位）
        resume_ids = self.create_test_resumes(10, published_ids)
        
        # 6. 筛选简历（简历提交后进入简历筛选，筛选通过才能进入面试）
        screened_resume_ids = self.screen_resumes(resume_ids)
        
        # 7. 创建面试记录（为筛选通过的简历创建三面）
        interview_ids = self.create_test_interviews(screened_resume_ids, published_ids)
        
        # 8. 更新面试结果为通过（面试三面全部通过的才能进入录用管理）
        passed_resume_ids = self.update_interview_results(screened_resume_ids)
        
        # 9. 创建测试录用记录（为三面全部通过的简历创建录用记录）
        offer_ids = self.create_test_offers(passed_resume_ids, published_ids)
        
        print("=" * 50)
        print("测试数据准备完成")
        print("=" * 50)
        print(f"用户数量: {len(self.test_data.get('user_ids', []))}")
        print(f"用人申请数量: {len(self.test_data.get('request_ids', []))}")
        print(f"简历数量: {len(self.test_data.get('resume_ids', []))}")
        print(f"面试记录数量: {len(self.test_data.get('interview_ids', []))}")
        print(f"已审批用人申请数量: {len(self.test_data.get('approved_request_ids', []))}")
        print(f"已发布岗位数量: {len(self.test_data.get('published_request_ids', []))}")
        print(f"录用记录数量: {len(self.test_data.get('offer_ids', []))}")
        print("=" * 50)
        
        # 保存测试数据到文件
        with open('/Users/silver/Vibe Coding/HR System/tests/test_data.json', 'w', encoding='utf-8') as f:
            json.dump(self.test_data, f, ensure_ascii=False, indent=2)
        
        print("测试数据已保存到: test_data.json")
        
        return self.test_data

if __name__ == "__main__":
    preparer = TestDataPreparer()
    try:
        test_data = preparer.prepare_all_test_data()
    finally:
        preparer.cleanup()

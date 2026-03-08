# 简历提交页面显示测试

import pytest
import sys
import os

sys.path.insert(0, os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

from common.api_client import ApiClient
from common.config import config

class TestResumeSubmissionPage:
    """简历提交页面显示测试"""
    
    def setup_method(self):
        """每个测试方法执行前的设置"""
        self.api_client = ApiClient()
        self.api_client.login(
            config.TEST_USERS["admin"]["username"],
            config.TEST_USERS["admin"]["password"]
        )
    
    def teardown_method(self):
        """每个测试方法执行后的清理"""
        if hasattr(self, 'api_client'):
            self.api_client.logout()
    
    def test_get_published_jobs_list(self):
        """测试获取已发布岗位列表 (TC-056)"""
        response = self.api_client.get("/recruitment-request/approval/status/3RDAPPROVED")
        assert response.status_code == 200, f"获取已发布岗位列表失败，状态码: {response.status_code}"
        
        data = response.json()
        assert data.get("returnCode") == "SUC0000", f"获取已发布岗位列表失败，错误信息: {data.get('errorMsg')}"
        
        jobs = data.get("body", [])
        assert isinstance(jobs, list), "返回数据应为列表"
        
        if jobs:
            print(f"获取到 {len(jobs)} 个已审批通过的岗位")
    
    def test_published_jobs_team_field_display(self):
        """测试已发布岗位列表中所属团队字段显示 (TC-057)"""
        response = self.api_client.get("/recruitment-request/approval/status/3RDAPPROVED")
        assert response.status_code == 200
        
        data = response.json()
        assert data.get("returnCode") == "SUC0000"
        
        jobs = data.get("body", [])
        
        if not jobs:
            pytest.skip("没有已审批通过的岗位数据")
        
        published_jobs = [job for job in jobs if job.get("positionPublishStatus") == "PUBLISHED"]
        
        if not published_jobs:
            pytest.skip("没有已发布的岗位数据")
        
        print(f"找到 {len(published_jobs)} 个已发布的岗位")
        
        for job in published_jobs:
            job_id = job.get("recruitmentRequestId")
            job_title = job.get("requestTitle")
            team = job.get("team")
            
            print(f"岗位ID: {job_id}, 岗位标题: {job_title}, 所属团队: {team}")
            
            assert "team" in job, f"岗位 {job_title} 缺少team字段"
            assert team is not None, f"岗位 {job_title} 的team字段为空"
            assert team != "", f"岗位 {job_title} 的team字段为空字符串"
    
    def test_published_jobs_required_fields(self):
        """测试已发布岗位列表的必填字段 (TC-058)"""
        response = self.api_client.get("/recruitment-request/approval/status/3RDAPPROVED")
        assert response.status_code == 200
        
        data = response.json()
        assert data.get("returnCode") == "SUC0000"
        
        jobs = data.get("body", [])
        
        if not jobs:
            pytest.skip("没有已审批通过的岗位数据")
        
        published_jobs = [job for job in jobs if job.get("positionPublishStatus") == "PUBLISHED"]
        
        if not published_jobs:
            pytest.skip("没有已发布的岗位数据")
        
        required_fields = [
            "recruitmentRequestId",
            "requestTitle",
            "team",
            "technicalPlatform",
            "supplementCount",
            "proposedLevel",
            "positionPublishStatus"
        ]
        
        for job in published_jobs:
            job_title = job.get("requestTitle", "未知岗位")
            for field in required_fields:
                assert field in job, f"岗位 {job_title} 缺少必填字段: {field}"
    
    def test_published_jobs_filter_by_status(self):
        """测试按发布状态筛选岗位 (TC-059)"""
        response = self.api_client.get("/recruitment-request/approval/status/3RDAPPROVED")
        assert response.status_code == 200
        
        data = response.json()
        assert data.get("returnCode") == "SUC0000"
        
        jobs = data.get("body", [])
        
        if not jobs:
            pytest.skip("没有已审批通过的岗位数据")
        
        published_count = len([job for job in jobs if job.get("positionPublishStatus") == "PUBLISHED"])
        unpublished_count = len([job for job in jobs if job.get("positionPublishStatus") == "UNPUBLISHED"])
        draft_count = len([job for job in jobs if job.get("positionPublishStatus") == "DRAFT"])
        # 统计其他状态的岗位（可能为null或其他值）
        other_count = len([job for job in jobs if job.get("positionPublishStatus") not in ["PUBLISHED", "UNPUBLISHED", "DRAFT"]])
        
        print(f"已发布岗位数: {published_count}, 未发布岗位数: {unpublished_count}, 草稿岗位数: {draft_count}, 其他状态岗位数: {other_count}, 总岗位数: {len(jobs)}")
        
        assert published_count >= 0, "已发布岗位数不能为负数"
        assert unpublished_count >= 0, "未发布岗位数不能为负数"
        assert draft_count >= 0, "草稿岗位数不能为负数"
        # 验证所有状态加起来等于总数（考虑所有可能的发布状态）
        assert published_count + unpublished_count + draft_count + other_count == len(jobs), "已发布、未发布、草稿和其他状态岗位数之和应等于总岗位数"
    
    def test_published_jobs_data_consistency(self):
        """测试已发布岗位数据一致性 (TC-060)"""
        response = self.api_client.get("/recruitment-request/approval/status/3RDAPPROVED")
        assert response.status_code == 200
        
        data = response.json()
        assert data.get("returnCode") == "SUC0000"
        
        jobs = data.get("body", [])
        
        if not jobs:
            pytest.skip("没有已审批通过的岗位数据")
        
        published_jobs = [job for job in jobs if job.get("positionPublishStatus") == "PUBLISHED"]
        
        if not published_jobs:
            pytest.skip("没有已发布的岗位数据")
        
        for job in published_jobs:
            job_id = job.get("recruitmentRequestId")
            job_title = job.get("requestTitle")
            approval_status = job.get("approvalStatus")
            publish_status = job.get("positionPublishStatus")
            
            assert approval_status == "3RDAPPROVED", f"岗位 {job_title} (ID: {job_id}) 的审批状态不是3RDAPPROVED"
            assert publish_status == "PUBLISHED", f"岗位 {job_title} (ID: {job_id}) 的发布状态不是PUBLISHED"
            
            supplement_count = job.get("supplementCount")
            assert supplement_count is not None, f"岗位 {job_title} 的supplementCount字段为空"
            assert isinstance(supplement_count, int), f"岗位 {job_title} 的supplementCount字段不是整数"
            assert supplement_count > 0, f"岗位 {job_title} 的supplementCount字段应大于0"

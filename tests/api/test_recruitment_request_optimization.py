import sys
from pathlib import Path

import pytest

sys.path.insert(0, str(Path(__file__).resolve().parents[1]))

from common.api_client import ApiClient


class TestRecruitmentRequestOptimization:
    def setup_method(self):
        self.api_client = ApiClient()
        # Fall back to default admin account used across project tests
        self.api_client.login("admin", "123321")

    def teardown_method(self):
        self.api_client.logout()

    def test_save_draft_allows_relaxed_payload(self):
        payload = {
            "requestTitle": "草稿-优化验证",
            "category": "其他",
        }
        response = self.api_client.post("/recruitment-request/save-draft", json=payload)
        assert response.status_code == 200

        body = response.json()
        assert body.get("returnCode") == "SUC0000"
        assert body.get("body") is not None
        assert body["body"].get("approvalStatus") == "DRAFT"

    def test_submit_supports_new_required_fields(self):
        payload = {
            "requestTitle": "提交流程-优化验证",
            "team": "零售平台开发室",
            "totalRecruitmentCount": 10,
            "vacancyCount": 2,
            "technicalPlatform": "开放",
            "category": "系统研发岗",
            "supplementCount": 1,
            "urgentRequirement": "YES",
            "proposedLevel": "PT",
            "experienceYears": "1-3",
            "skillRequirement": "具备良好沟通与协作能力",
            "positionResponsibility": "负责系统研发与交付",
            "interviewerId": "1001",
            "interviewerName": "系统用户",
        }
        response = self.api_client.post("/recruitment-request/submit", json=payload)
        assert response.status_code == 200

        body = response.json()
        assert body.get("returnCode") == "SUC0000"
        assert body.get("body") is not None
        assert body["body"].get("approvalStatus") in ["PENDING", "1STAPPROVED", "2NDAPPROVED", "3RDAPPROVED"]

    def test_user_search_endpoint_available(self):
        response = self.api_client.get("/users/search", params={"keyword": "a"})
        assert response.status_code == 200

        body = response.json()
        assert body.get("returnCode") == "SUC0000"
        assert isinstance(body.get("body"), list)

    def test_workflow_initiated_contains_applicant_dept(self):
        response = self.api_client.get("/workflow-center/initiated", params={"userId": "1001"})
        assert response.status_code == 200

        body = response.json()
        assert body.get("returnCode") == "SUC0000"
        items = body.get("body") or []
        if items:
            assert "applicantDept" in items[0]


if __name__ == "__main__":
    raise SystemExit(pytest.main([__file__]))

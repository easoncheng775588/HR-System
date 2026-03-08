#!/usr/bin/env python3
# -*- coding: utf-8 -*-

import os
import sys
import time
import unittest
from typing import Dict, Any

import requests

sys.path.insert(0, os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
from common.config import config  # noqa: E402


class TestWorkflowCenterApi(unittest.TestCase):
    def setUp(self):
        self.session = requests.Session()
        self.session.verify = False
        self.base_url = f"{config.API_BASE_URL}/api"

        user = config.TEST_USERS.get("admin", {})
        resp = self.session.post(
            f"{self.base_url}/auth/login",
            json={
                "username": user.get("username", "admin"),
                "password": user.get("password", "123321"),
            },
            timeout=20,
        )
        self.assertEqual(resp.status_code, 200, "login http status is not 200")
        data = resp.json()
        self.assertEqual(data.get("returnCode"), "SUC0000", f"login failed: {data}")
        token = data.get("body", {}).get("token")
        if token:
            self.session.headers.update({"Authorization": f"Bearer {token}"})

    def _create_submitted_request(self, title_suffix: str) -> int:
        payload: Dict[str, Any] = {
            "requestTitle": f"WF自动化-{title_suffix}-{int(time.time())}",
            "totalRecruitmentCount": 10,
            "vacancyCount": 2,
            "team": "技术管理团队",
            "technicalPlatform": "backend",
            "supplementCount": 2,
            "urgentRequirement": "no",
            "proposedLevel": "MID_LEVEL",
            "experienceYears": "3-5",
            "skillRequirement": "Java, Spring Boot",
            "positionResponsibility": "流程中心自动化测试",
            "createUserId": "1001",
            "createUserName": "系统用户",
            "updateUserId": "1001",
            "updateUserName": "系统用户",
        }
        resp = self.session.post(
            f"{self.base_url}/recruitment-request/submit",
            json=payload,
            timeout=20,
        )
        self.assertEqual(resp.status_code, 200, "submit http status is not 200")
        data = resp.json()
        self.assertEqual(data.get("returnCode"), "SUC0000", f"submit failed: {data}")
        request_id = data.get("body", {}).get("recruitmentRequestId")
        self.assertIsNotNone(request_id, "submit body has no recruitmentRequestId")
        return int(request_id)

    def test_workflow_center_todo_and_detail(self):
        request_id = self._create_submitted_request("todo")

        todo_resp = self.session.get(
            f"{self.base_url}/workflow-center/todo",
            params={"userId": "1001", "userRole": "管理员"},
            timeout=20,
        )
        self.assertEqual(todo_resp.status_code, 200, "todo http status is not 200")
        todo_data = todo_resp.json()
        self.assertEqual(todo_data.get("returnCode"), "SUC0000", f"todo failed: {todo_data}")
        todo_list = todo_data.get("body", [])
        self.assertIsInstance(todo_list, list, "todo body is not list")
        self.assertTrue(any(int(i.get("requestId", -1)) == request_id for i in todo_list), "created request not found in todo list")

        detail_resp = self.session.get(
            f"{self.base_url}/workflow-center/detail/{request_id}",
            params={"viewerId": "1001", "viewerName": "系统用户", "viewerRole": "管理员"},
            timeout=20,
        )
        self.assertEqual(detail_resp.status_code, 200, "detail http status is not 200")
        detail_data = detail_resp.json()
        self.assertEqual(detail_data.get("returnCode"), "SUC0000", f"detail failed: {detail_data}")
        body = detail_data.get("body", {})
        self.assertIn("request", body, "detail body missing request")
        self.assertIn("approvalHistory", body, "detail body missing approvalHistory")
        self.assertIn("processLogs", body, "detail body missing processLogs")

    def test_workflow_center_initiated_and_processed(self):
        request_id = self._create_submitted_request("initiated")

        initiated_resp = self.session.get(
            f"{self.base_url}/workflow-center/initiated",
            params={"userId": "1001"},
            timeout=20,
        )
        self.assertEqual(initiated_resp.status_code, 200, "initiated http status is not 200")
        initiated_data = initiated_resp.json()
        self.assertEqual(initiated_data.get("returnCode"), "SUC0000", f"initiated failed: {initiated_data}")
        initiated_list = initiated_data.get("body", [])
        self.assertTrue(any(int(i.get("requestId", -1)) == request_id for i in initiated_list), "created request not found in initiated list")

        approve_resp = self.session.post(
            f"{self.base_url}/workflow-center/{request_id}/approve",
            json={
                "action": "APPROVE",
                "approvalUserId": "1001",
                "approvalUserName": "系统用户",
                "approvalUserRole": "管理员",
                "approvalComment": "自动化通过",
            },
            timeout=20,
        )
        self.assertEqual(approve_resp.status_code, 200, "approve http status is not 200")
        approve_data = approve_resp.json()
        self.assertEqual(approve_data.get("returnCode"), "SUC0000", f"approve failed: {approve_data}")

        processed_resp = self.session.get(
            f"{self.base_url}/workflow-center/processed",
            params={"userId": "1001"},
            timeout=20,
        )
        self.assertEqual(processed_resp.status_code, 200, "processed http status is not 200")
        processed_data = processed_resp.json()
        self.assertEqual(processed_data.get("returnCode"), "SUC0000", f"processed failed: {processed_data}")
        processed_list = processed_data.get("body", [])
        self.assertTrue(any(int(i.get("requestId", -1)) == request_id for i in processed_list), "approved request not found in processed list")


if __name__ == "__main__":
    unittest.main()

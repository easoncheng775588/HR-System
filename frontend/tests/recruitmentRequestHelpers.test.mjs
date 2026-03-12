import test from 'node:test'
import assert from 'node:assert/strict'

import {
  TEXTAREA_MAX_LENGTH,
  buildRecruitmentRequestPayload,
  buildRecruitmentRequestViewerParams,
  formatWorkflowNodeLabel,
  getDepartmentDisplayText,
  getRequestTypeLabel,
  inferSubmitterRoleType,
  normalizeRecruitmentRequestFormValues,
} from '../src/components/recruitmentRequestHelpers.js'

test('request type labels match frozen contract', () => {
  assert.equal(getRequestTypeLabel('LEAVE'), '离场')
  assert.equal(getRequestTypeLabel('RELEASE_NO_GAP'), '释放不缺')
  assert.equal(getRequestTypeLabel('NEW_DEMAND'), '新增需求')
  assert.equal(getRequestTypeLabel('UNKNOWN_TYPE'), 'UNKNOWN_TYPE')
})

test('department display prefers applicationDepartment and falls back to team/group info', () => {
  assert.equal(
    getDepartmentDisplayText({ applicationDepartment: '零售业务开发团队 / 零售平台开发室' }),
    '零售业务开发团队 / 零售平台开发室',
  )
  assert.equal(
    getDepartmentDisplayText({ teamName: '零售业务开发团队', groupName: '零售平台开发室' }),
    '零售业务开发团队 / 零售平台开发室',
  )
  assert.equal(getDepartmentDisplayText({ teamName: '技术管理团队' }), '技术管理团队')
})

test('form values normalize readonly display fields for create and edit flows', () => {
  const normalized = normalizeRecruitmentRequestFormValues(
    { requestTitle: '测试申请', vacancyCount: 2 },
    { teamName: '零售业务开发团队', groupName: '零售平台开发室' },
  )

  assert.equal(normalized.applicationDepartment, '零售业务开发团队 / 零售平台开发室')
  assert.equal(normalized.totalRecruitmentCount, 0)
  assert.equal(normalized.vacancyCount, 2)
  assert.equal(normalized.remark, undefined)
})

test('submit payload excludes readonly fields and trims optional remark', () => {
  const payload = buildRecruitmentRequestPayload({
    requestTitle: '  零售平台开发室补员申请  ',
    requestType: 'LEAVE',
    applicationDepartment: '零售业务开发团队 / 零售平台开发室',
    totalRecruitmentCount: 8,
    vacancyCount: 1,
    remark: '  离场补位  ',
    interviewerId: '1008',
  })

  assert.deepEqual(payload, {
    requestTitle: '零售平台开发室补员申请',
    requestType: 'LEAVE',
    remark: '离场补位',
    interviewerId: '1008',
  })
})

test('viewer params always include viewerId required by list contract', () => {
  assert.deepEqual(buildRecruitmentRequestViewerParams({ userId: '1002', position: '室经理' }), {
    viewerId: '1002',
    viewerRole: '室经理',
  })
  assert.deepEqual(buildRecruitmentRequestViewerParams({}), {
    viewerId: '1001',
    viewerRole: '',
  })
})

test('third approval node label follows submitter role type', () => {
  assert.equal(inferSubmitterRoleType({ applicationDepartment: '零售业务开发团队 / 零售平台开发室' }), 'ROOM_MANAGER')
  assert.equal(inferSubmitterRoleType({ applicantDept: '技术管理团队' }), 'DIRECT_TEAM_MANAGER')
  assert.equal(formatWorkflowNodeLabel('三级审批', 'ROOM_MANAGER'), '团队经理审批')
  assert.equal(formatWorkflowNodeLabel('三级审批', 'DIRECT_TEAM_MANAGER'), '分管总审批')
  assert.equal(formatWorkflowNodeLabel('三级审批通过', 'DIRECT_TEAM_MANAGER'), '分管总审批通过')
  assert.equal(formatWorkflowNodeLabel('编制管理岗审批', 'ROOM_MANAGER'), '编制管理岗审批')
})

test('textarea limit stays frozen at 500 characters', () => {
  assert.equal(TEXTAREA_MAX_LENGTH, 500)
})

import test from 'node:test'
import assert from 'node:assert/strict'

import {
  getGroupOptions,
  getTeamOptions,
  normalizeDepartmentPayload,
  validateDepartmentSelection,
} from '../src/components/userManagementDepartment.js'

const departmentOptions = [
  {
    teamName: '零售业务开发团队',
    teamType: 'NORMAL',
    requiresGroup: true,
    groupOptions: [{ groupName: '零售平台开发室' }],
  },
  {
    teamName: '技术管理团队',
    teamType: 'DIRECT',
    requiresGroup: false,
    groupOptions: [],
  },
  {
    teamName: '直属人员',
    teamType: 'DIRECT_CATEGORY',
    requiresGroup: true,
    groupOptions: [{ groupName: '部门总经理' }, { groupName: '分管总' }],
  },
]

test('returns stable team options for select rendering', () => {
  assert.deepEqual(getTeamOptions(departmentOptions), [
    { label: '零售业务开发团队', value: '零售业务开发团队' },
    { label: '技术管理团队', value: '技术管理团队' },
    { label: '直属人员', value: '直属人员' },
  ])
})

test('returns group options only for the selected team', () => {
  assert.deepEqual(getGroupOptions(departmentOptions, '零售业务开发团队'), [
    { label: '零售平台开发室', value: '零售平台开发室' },
  ])
  assert.deepEqual(getGroupOptions(departmentOptions, '技术管理团队'), [])
})

test('validates missing group for a team that requires group selection', () => {
  assert.equal(
    validateDepartmentSelection({
      teamName: '零售业务开发团队',
      groupName: undefined,
      departmentOptions,
    }),
    '所选团队要求必须填写室组名称',
  )
})

test('validates disallowed group for a direct team', () => {
  assert.equal(
    validateDepartmentSelection({
      teamName: '技术管理团队',
      groupName: '零售平台开发室',
      departmentOptions,
    }),
    '所选团队不允许填写室组名称',
  )
})

test('normalizes payload by clearing groupName for direct team', () => {
  assert.deepEqual(
    normalizeDepartmentPayload(
      {
        userId: '1002',
        teamName: '技术管理团队',
        groupName: '不应保留',
      },
      departmentOptions,
    ),
    {
      userId: '1002',
      teamName: '技术管理团队',
      groupName: undefined,
    },
  )
})

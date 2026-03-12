export const TEXTAREA_MAX_LENGTH = 500

export const REQUEST_TYPE_OPTIONS = [
  { label: '离场', value: 'LEAVE' },
  { label: '释放不缺', value: 'RELEASE_NO_GAP' },
  { label: '新增需求', value: 'NEW_DEMAND' },
]

const SUBMIT_FIELDS = [
  'requestTitle',
  'requestType',
  'technicalPlatform',
  'category',
  'supplementCount',
  'urgentRequirement',
  'proposedLevel',
  'experienceYears',
  'skillRequirement',
  'positionResponsibility',
  'remark',
  'interviewerId',
  'interviewerName',
]

const cleanText = (value) => (typeof value === 'string' ? value.trim() : value)

export const getRequestTypeLabel = (value) =>
  REQUEST_TYPE_OPTIONS.find((item) => item.value === value)?.label || value || '-'

export const getDepartmentDisplayText = (source = {}) => {
  if (source.applicationDepartment) return source.applicationDepartment
  if (source.departmentDisplay) return source.departmentDisplay
  if (source.teamName && source.groupName) return `${source.teamName} / ${source.groupName}`
  if (source.groupName) return source.groupName
  if (source.team) return source.team
  if (source.teamName) return source.teamName
  if (source.department) return source.department
  return ''
}

export const getOrgUnitName = (source = {}) =>
  source.orgUnitName || source.groupName || source.teamName || source.department || ''

export const normalizeRecruitmentRequestFormValues = (request = {}, currentUser = {}) => ({
  ...request,
  applicationDepartment:
    getDepartmentDisplayText(request) || getDepartmentDisplayText(currentUser) || undefined,
  totalRecruitmentCount: Number(request.totalRecruitmentCount || 0),
  vacancyCount: Number(request.vacancyCount || 0),
  remark: request.remark || undefined,
})

export const buildRecruitmentRequestPayload = (values = {}) =>
  SUBMIT_FIELDS.reduce((payload, fieldName) => {
    const value = cleanText(values[fieldName])
    if (value === undefined || value === null || value === '') {
      return payload
    }

    payload[fieldName] = value
    return payload
  }, {})

export const buildRecruitmentRequestViewerParams = (user = {}) => ({
  viewerId: String(user.userId || '1001'),
  viewerRole: String(user.position || user.positionName || user.role || ''),
})

export const inferSubmitterRoleType = (source = {}) => {
  if (source.submitterRoleType) return source.submitterRoleType

  const departmentText =
    source.applicationDepartment || source.applicantDept || source.team || source.departmentDisplay || ''
  if (departmentText.includes('/')) return 'ROOM_MANAGER'
  if (departmentText) return 'DIRECT_TEAM_MANAGER'

  return 'ROOM_MANAGER'
}

export const getThirdApprovalNodeLabel = (submitterRoleType) =>
  submitterRoleType === 'DIRECT_TEAM_MANAGER' ? '分管总审批' : '团队经理审批'

export const formatWorkflowNodeLabel = (nodeName, submitterRoleType) => {
  if (!nodeName) return '-'

  const thirdNodeLabel = getThirdApprovalNodeLabel(submitterRoleType)
  if (nodeName === '三级审批') return thirdNodeLabel
  if (nodeName.includes('三级审批')) return nodeName.replace(/三级审批/g, thirdNodeLabel)
  if (nodeName.includes('第三级审批')) return nodeName.replace(/第三级审批/g, thirdNodeLabel)

  return nodeName
}

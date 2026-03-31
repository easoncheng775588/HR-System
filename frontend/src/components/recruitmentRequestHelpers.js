export const RECRUITMENT_CATEGORY_OPTIONS = ['系统研发岗', '产品助理', '测试', '项目助理', '其他']

export const RECRUITMENT_PLATFORM_OPTIONS = [
  '开放',
  '主机',
  '测试',
  'T24',
  '手机',
  '数据仓库',
  '行政',
  '其他（请在“备注”处说明）',
]

export const TEXTAREA_MAX_LENGTH = 500

export const REQUEST_TYPE_OPTIONS = [
  { label: '离场、释放补缺', value: 'LEAVE' },
  { label: '新增需求', value: 'NEW_DEMAND' },
]

const SUBMIT_FIELDS = [
  'requestTitle',
  'orgUnitName',
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

export const extractOrgUnitNameFromDepartment = (departmentText = '') => {
  if (!departmentText) return ''
  if (!departmentText.includes('/')) return departmentText.trim()
  const segments = departmentText.split('/')
  return segments[segments.length - 1].trim()
}

export const getRequestTypeLabel = (value) => {
  if (value === 'RELEASE_NO_GAP') {
    return '离场、释放补缺'
  }
  return REQUEST_TYPE_OPTIONS.find((item) => item.value === value)?.label || value || '-'
}

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
  source.orgUnitName ||
  extractOrgUnitNameFromDepartment(source.applicationDepartment || '') ||
  source.groupName ||
  source.teamName ||
  source.department ||
  ''

export const buildDepartmentDisplayByOrgUnit = (orgUnitName = '', orgUnits = []) => {
  if (!orgUnitName) return ''
  const currentUnit = orgUnits.find((item) => item.unitName === orgUnitName)
  if (!currentUnit) return orgUnitName
  if (currentUnit.unitType === 'GROUP' && currentUnit.parentUnitName) {
    return `${currentUnit.parentUnitName} / ${currentUnit.unitName}`
  }
  return currentUnit.unitName
}

export const buildResponsibleDepartmentOptions = (staffings = [], orgUnits = []) =>
  staffings
    .filter((item) => item?.orgUnitName)
    .map((item) => ({
      value: item.orgUnitName,
      label: buildDepartmentDisplayByOrgUnit(item.orgUnitName, orgUnits),
      orgUnitName: item.orgUnitName,
      totalRecruitmentCount: Number(item.totalHeadcount || 0),
      vacancyCount: Number(item.vacancyHeadcount || 0),
    }))

export const normalizeRecruitmentRequestFormValues = (request = {}, currentUser = {}) => ({
  ...request,
  orgUnitName: getOrgUnitName(request) || getOrgUnitName(currentUser) || undefined,
  applicationDepartment:
    getDepartmentDisplayText(request) || getDepartmentDisplayText(currentUser) || undefined,
  urgentRequirement: request.urgentRequirement || 'YES',
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

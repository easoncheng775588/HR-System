export const RESUME_STATUS = {
  PENDING_REVIEW: '待审核',
  PENDING_SCREEN: '待简历初筛',
  PASSED_SCREEN: '已通过简历初筛',
  NEED_MATERIAL_EDIT: '需修改简历材料',
  REJECTED_SCREEN: '未通过简历初筛',
}

export const RESUME_TAB = {
  ALL: 'all',
  PENDING_REVIEW: 'pendingReview',
  PASSED: 'passed',
  NEED_MATERIAL_EDIT: 'needMaterialEdit',
  REJECTED: 'rejected',
}

export const SCREEN_ACTION = {
  PASS: 'PASS',
  MATERIAL_EDIT: 'MATERIAL_EDIT',
  REJECT: 'REJECT',
}

export const INTERVIEWER_CHOICE = {
  CONFIRM: 'CONFIRM',
  ABANDON: 'ABANDON',
}

const pickRoleText = (user = {}) =>
  `${user?.position || ''} ${user?.positionName || ''} ${user?.role || ''}`.trim()

export const isSuperAdmin = (user = {}) =>
  String(user?.userId || '') === '1001' || pickRoleText(user).includes('管理员')

export const isSupplierHrRole = (user = {}) => pickRoleText(user).includes('供应商HR')

export const isOutsourcingManagerRole = (user = {}) =>
  pickRoleText(user).includes('外包招聘管理岗') || pickRoleText(user).includes('外包招聘管理')

export const isInterviewerRole = (user = {}) => pickRoleText(user).includes('面试官')

export const isRoomManagerRole = (user = {}) => pickRoleText(user).includes('室经理')

export const isTeamManagerRole = (user = {}) => pickRoleText(user).includes('团队经理')

export const canCreateResume = (user = {}) => isSupplierHrRole(user)

export const normalizeResumeStatus = (status) => {
  const raw = String(status || '').trim()
  if (!raw) return raw
  if (raw === RESUME_STATUS.PENDING_SCREEN || raw === 'PENDING_SCREENING') {
    return RESUME_STATUS.PENDING_REVIEW
  }
  return raw
}

export const normalizeResumeRecord = (record = {}) => ({
  ...record,
  status: normalizeResumeStatus(record.status),
})

export const filterResumesByTab = (rows = [], tabKey = RESUME_TAB.ALL) => {
  if (tabKey === RESUME_TAB.ALL) return rows
  return rows.filter((record) => {
    const normalizedStatus = normalizeResumeStatus(record?.status)
    if (tabKey === RESUME_TAB.PENDING_REVIEW) return normalizedStatus === RESUME_STATUS.PENDING_REVIEW
    if (tabKey === RESUME_TAB.PASSED) return normalizedStatus === RESUME_STATUS.PASSED_SCREEN
    if (tabKey === RESUME_TAB.NEED_MATERIAL_EDIT) return normalizedStatus === RESUME_STATUS.NEED_MATERIAL_EDIT
    if (tabKey === RESUME_TAB.REJECTED) return normalizedStatus === RESUME_STATUS.REJECTED_SCREEN
    return true
  })
}

export const canShowResumeTabs = (user = {}) => isSupplierHrRole(user) || canDoScreening(user)

export const isResumeLocked = (record = {}) =>
  Boolean(record?.confirmedInterviewerId) || String(record?.currentInterviewerChoiceStatus || '') === '已确认'

export const canEditOrDeleteResume = (user = {}, record = {}) => {
  if (!isSupplierHrRole(user)) return false
  const isOwner = String(record?.createUserId || '') === String(user?.userId || '')
  if (!isOwner) return false
  const status = normalizeResumeStatus(record?.status)
  return status === RESUME_STATUS.PENDING_REVIEW || status === RESUME_STATUS.NEED_MATERIAL_EDIT
}

export const canDoScreening = (user = {}) => isOutsourcingManagerRole(user) || isSuperAdmin(user)

export const canShowScreenActions = (user = {}, record = {}) =>
  canDoScreening(user) && normalizeResumeStatus(record?.status) === RESUME_STATUS.PENDING_REVIEW

export const canDispatchResume = (user = {}, record = {}) =>
  canDoScreening(user)
  && normalizeResumeStatus(record?.status) === RESUME_STATUS.PASSED_SCREEN
  && !isResumeLocked(record)

export const canDoInterviewerChoice = (user = {}) => isInterviewerRole(user) || isSuperAdmin(user)

export const buildResumeListQuery = (user = {}) => ({
  viewerId: String(user?.userId || ''),
  viewerRole: String(user?.position || user?.positionName || user?.role || ''),
})

export const buildOperatorPayload = (user = {}) => ({
  operatorUserId: String(user?.userId || ''),
  operatorUserName: String(user?.realName || user?.username || ''),
  operatorRole: String(user?.position || user?.positionName || user?.role || ''),
})

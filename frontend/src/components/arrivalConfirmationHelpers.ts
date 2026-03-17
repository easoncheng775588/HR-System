export const matchOption = (input: string, option?: { label?: unknown }) => {
  const keyword = String(input || '').trim().toLowerCase()
  const label = String(option?.label || '').toLowerCase()
  return label.includes(keyword)
}

export const isOutsourcingCreator = (user?: Record<string, unknown> | null) => {
  const userId = String(user?.userId || '')
  const roleText = `${String(user?.position || '')} ${String(user?.role || '')}`
  return userId === '1001' || roleText.includes('外包招聘管理')
}

export const buildCandidateLabel = (candidate?: Record<string, unknown>) => {
  const name = String(candidate?.candidateName || '-')
  const dept = String(candidate?.targetOrgUnitName || '-')
  const supplier = String(candidate?.supplierName || '-')
  return `${name} / ${dept} / ${supplier}`
}

export const formatApprovalStatus = (status?: string) => {
  if (status === 'APPROVED') return '已通过'
  if (status === 'REJECTED') return '已拒绝'
  return '处理中'
}

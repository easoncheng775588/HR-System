import { canShowInterviewEvaluationLaunch } from './interviewPermissionHelpers'

const roleTextOf = (user: Record<string, unknown> = {}) =>
  `${user?.position || ''} ${user?.positionName || ''} ${user?.role || ''}`.trim()

export const isSuperAdmin = (user: Record<string, unknown> = {}) =>
  String(user?.userId || '') === '1001' || roleTextOf(user).includes('管理员')

export const isOutsourcingManager = (user: Record<string, unknown> = {}) =>
  roleTextOf(user).includes('外包招聘管理岗') || roleTextOf(user).includes('外包招聘管理')

export const isSupplierHr = (user: Record<string, unknown> = {}) => roleTextOf(user).includes('供应商HR')

export const isInterviewer = (user: Record<string, unknown> = {}) => roleTextOf(user).includes('面试官')

export const canOpenResumeConfirmDialog = (
  _user: Record<string, unknown> = {},
  record: Record<string, unknown> = {},
) => Boolean(record?.canInterviewerConfirm)

export const canAbandonInterviewChoice = (
  _user: Record<string, unknown> = {},
  record: Record<string, unknown> = {},
) => Boolean(record?.canInterviewerAbandon)

export const canConfirmInterviewTime = (
  _user: Record<string, unknown> = {},
  record: Record<string, unknown> = {},
) =>
  Boolean(record?.canConfirmInterviewTime)
  && String(record?.dispatchStatus || '').toUpperCase() === 'CONFIRMED'

export const canLaunchInterviewEvaluation = (
  _user: Record<string, unknown> = {},
  record: Record<string, unknown> = {},
) => canShowInterviewEvaluationLaunch(record)

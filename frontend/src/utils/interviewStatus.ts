import { formatInterviewTimeRangeText } from './interviewTimeFormat'

export const INTERVIEW_METHOD = {
  ONLINE: 'ONLINE',
  OFFLINE: 'OFFLINE',
} as const

export const INTERVIEW_METHOD_OPTIONS = [
  { label: '线上', value: INTERVIEW_METHOD.ONLINE },
  { label: '线下', value: INTERVIEW_METHOD.OFFLINE },
]

export const HIRE_SUGGESTION_OPTIONS = [
  { label: '优秀', value: '优秀' },
  { label: '良好', value: '良好' },
  { label: '及格', value: '及格' },
  { label: '淘汰', value: '淘汰' },
]

export const PROCESS_CODE = {
  RECRUITMENT_REQUEST: 'RECRUITMENT_REQUEST',
  INTERVIEW_EVALUATION: 'INTERVIEW_EVALUATION',
} as const

export const getInterviewMethodLabel = (method?: string) => {
  if (method === INTERVIEW_METHOD.ONLINE) return '线上'
  if (method === INTERVIEW_METHOD.OFFLINE) return '线下'
  return method || '-'
}

export const formatDateTimeText = (value?: string) => {
  if (!value) return '-'
  const date = new Date(value)
  return Number.isNaN(date.getTime()) ? String(value) : date.toLocaleString('zh-CN')
}

export const formatInterviewTimeRange = (start?: string, end?: string) => {
  return formatInterviewTimeRangeText(start, end)
}

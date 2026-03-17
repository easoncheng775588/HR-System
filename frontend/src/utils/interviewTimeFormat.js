import dayjs from 'dayjs'

export const formatLocalMinuteText = (value) => {
  if (!value) return '-'
  const date = dayjs(value)
  return date.isValid() ? date.format('YYYY-MM-DD HH:mm') : String(value)
}

export const formatInterviewTimeRangeText = (start, end) => {
  if (!start && !end) return '-'
  return `${formatLocalMinuteText(start)} ～ ${formatLocalMinuteText(end)}`
}


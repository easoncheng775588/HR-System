export const formatInterviewDatePayload = (value) => {
  if (!value) return ''
  if (typeof value.format === 'function') {
    return value.format('YYYY-MM-DD')
  }
  return String(value)
}

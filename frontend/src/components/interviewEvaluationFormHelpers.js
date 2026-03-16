import dayjs from 'dayjs'

export const buildInterviewEvaluationInitialValues = (record = {}) => ({
  entryLevelSuggestion: record?.entryLevelSuggestion || '',
  score: record?.score || '',
  hireSuggestion: record?.hireSuggestion || '',
  interviewDate: toDayjsValue(record?.interviewDate),
})

export const formatInterviewEvaluationDatePayload = (value) => {
  if (!value || typeof value.format !== 'function') {
    return ''
  }
  return value.format('YYYY-MM-DD')
}

const toDayjsValue = (value) => {
  if (!value) {
    return undefined
  }
  const parsed = dayjs(value)
  return parsed.isValid() ? parsed : undefined
}

export const canShowInterviewEvaluationLaunch = (record = {}) =>
  Boolean(record?.canLaunchEvaluation) && !String(record?.evaluationStatus || '').trim()

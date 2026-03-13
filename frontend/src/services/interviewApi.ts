import api from '../utils/api'

export const submitInterviewerChoice = (resumeId: number | string, payload: Record<string, unknown>) =>
  api.post(`/api/resume/${resumeId}/interviewer-choice`, payload)

export const getPendingInterviewArrangement = (params: Record<string, unknown>) =>
  api.get('/api/interview-arrangement/pending', { params })

export const confirmInterviewTime = (resumeId: number | string, payload: Record<string, unknown>) =>
  api.post(`/api/interview-arrangement/${resumeId}/confirm-time`, payload)

export const submitInterviewEvaluation = (payload: Record<string, unknown>) =>
  api.post('/api/interview-evaluation/submit', payload)

export const getInterviewEvaluationDetail = (evaluationId: number | string) =>
  api.get(`/api/interview-evaluation/${evaluationId}`)

export const approveWorkflowProcess = (
  processCode: string,
  businessId: number | string,
  payload: Record<string, unknown>,
) => api.post(`/api/workflow-center/${processCode}/${businessId}/approve`, payload)

export const getWorkflowEvaluationDetail = (detail = {}) => {
  if (detail?.evaluation && Object.keys(detail.evaluation).length > 0) return detail.evaluation
  if (detail?.interviewEvaluation && Object.keys(detail.interviewEvaluation).length > 0) return detail.interviewEvaluation
  return {}
}

export const getWorkflowArrivalDetail = (detail = {}) => {
  if (detail?.arrivalConfirmation && Object.keys(detail.arrivalConfirmation).length > 0) return detail.arrivalConfirmation
  return {}
}

export const getWorkflowApprovalHistory = (detail = {}) => {
  if (Array.isArray(detail?.approvalHistory) && detail.approvalHistory.length > 0) return detail.approvalHistory
  if (Array.isArray(detail?.interviewEvaluationApprovalHistory) && detail.interviewEvaluationApprovalHistory.length > 0) {
    return detail.interviewEvaluationApprovalHistory
  }
  if (Array.isArray(detail?.arrivalConfirmationApprovalHistory) && detail.arrivalConfirmationApprovalHistory.length > 0) {
    return detail.arrivalConfirmationApprovalHistory
  }
  return []
}

export const getWorkflowEvaluationDetail = (detail = {}) => {
  return detail?.evaluation || detail?.interviewEvaluation || {}
}

export const getWorkflowApprovalHistory = (detail = {}) => {
  return detail?.approvalHistory || detail?.interviewEvaluationApprovalHistory || []
}

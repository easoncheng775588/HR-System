import test from 'node:test'
import assert from 'node:assert/strict'

import {
  getWorkflowApprovalHistory,
  getWorkflowEvaluationDetail,
} from '../src/components/workflowDetailHelpers.js'

test('getWorkflowEvaluationDetail supports interview evaluation payload key', () => {
  const detail = getWorkflowEvaluationDetail({
    interviewEvaluation: {
      candidateName: '候选人A',
      interviewerName: '面试官A',
    },
  })

  assert.equal(detail.candidateName, '候选人A')
  assert.equal(detail.interviewerName, '面试官A')
})

test('getWorkflowApprovalHistory supports interview evaluation history payload key', () => {
  const history = getWorkflowApprovalHistory({
    interviewEvaluationApprovalHistory: [{ approverName: '外包岗A' }],
  })

  assert.equal(history.length, 1)
  assert.equal(history[0].approverName, '外包岗A')
})

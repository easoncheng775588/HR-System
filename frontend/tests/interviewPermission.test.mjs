import test from 'node:test'
import assert from 'node:assert/strict'

import { canShowInterviewEvaluationLaunch } from '../src/utils/interviewPermissionHelpers.js'

test('canLaunchInterviewEvaluation hides button after evaluation has been initiated', () => {
  assert.equal(
    canShowInterviewEvaluationLaunch({
      canLaunchEvaluation: true,
      evaluationStatus: 'PENDING_OUTSOURCING',
    }),
    false,
  )
})

test('canLaunchInterviewEvaluation allows button when no evaluation exists', () => {
  assert.equal(
    canShowInterviewEvaluationLaunch({
      canLaunchEvaluation: true,
      evaluationStatus: '',
    }),
    true,
  )
})

import test from 'node:test'
import assert from 'node:assert/strict'

import {
  buildInterviewEvaluationInitialValues,
  formatInterviewEvaluationDatePayload,
} from '../src/components/interviewEvaluationFormHelpers.js'

test('buildInterviewEvaluationInitialValues does not prefill entry level or interview date from candidate data', () => {
  const values = buildInterviewEvaluationInitialValues({
    appliedLevel: 'P6',
    confirmedInterviewTime: '2026-03-20 10:30:00',
  })

  assert.equal(values.entryLevelSuggestion, '')
  assert.equal(values.interviewDate, undefined)
})

test('buildInterviewEvaluationInitialValues keeps persisted evaluation values for re-edit', () => {
  const values = buildInterviewEvaluationInitialValues({
    entryLevelSuggestion: 'P7',
    interviewDate: '2026-03-21 09:00:00',
  })

  assert.equal(values.entryLevelSuggestion, 'P7')
  assert.ok(values.interviewDate)
  assert.equal(values.interviewDate.format('YYYY-MM-DD'), '2026-03-21')
})

test('formatInterviewEvaluationDatePayload submits date picker value as date-only string', () => {
  const payload = formatInterviewEvaluationDatePayload({
    format(pattern) {
      assert.equal(pattern, 'YYYY-MM-DD')
      return '2026-03-20'
    },
  })

  assert.equal(payload, '2026-03-20')
})

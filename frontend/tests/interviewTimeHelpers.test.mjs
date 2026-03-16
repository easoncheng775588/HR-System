import test from 'node:test'
import assert from 'node:assert/strict'

import { formatInterviewDatePayload } from '../src/components/interviewTimeHelpers.js'

test('formatInterviewDatePayload keeps date-only payload for date picker values', () => {
  const payload = formatInterviewDatePayload({
    format(pattern) {
      assert.equal(pattern, 'YYYY-MM-DD')
      return '2026-03-20'
    },
  })

  assert.equal(payload, '2026-03-20')
})

import test from 'node:test'
import assert from 'node:assert/strict'

import { formatInterviewDatePayload } from '../src/components/interviewTimeHelpers.js'

test('formatInterviewDatePayload keeps datetime payload for datetime picker values', () => {
  const payload = formatInterviewDatePayload({
    format(pattern) {
      assert.equal(pattern, 'YYYY-MM-DD HH:mm:ss')
      return '2026-03-20 14:30:00'
    },
  })

  assert.equal(payload, '2026-03-20 14:30:00')
})

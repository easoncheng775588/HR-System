import test from 'node:test'
import assert from 'node:assert/strict'

import { formatInterviewTimeRangeText } from '../src/utils/interviewTimeFormat.js'

test('formatInterviewTimeRangeText renders utc timestamps in local minute precision', () => {
  const text = formatInterviewTimeRangeText(
    '2026-03-17T00:00:00.000+00:00',
    '2026-03-31T10:00:00.000+00:00',
  )

  assert.equal(text, '2026-03-17 08:00 ～ 2026-03-31 18:00')
})

import test from 'node:test'
import assert from 'node:assert/strict'

import { RECRUITMENT_LEVEL_OPTIONS, buildEntryLevelOptions } from '../src/components/interviewEvaluationHelpers.js'

test('entry level options fall back to the same recruitment request levels', () => {
  const options = buildEntryLevelOptions([])

  assert.deepEqual(
    options,
    RECRUITMENT_LEVEL_OPTIONS.map((value) => ({ value, label: value })),
  )
})

test('entry level options prefer LEVEL params when available', () => {
  const options = buildEntryLevelOptions([
    { value: 'LEVEL_001', label: '初级' },
    { value: 'LEVEL_002', label: '中级' },
  ])

  assert.deepEqual(options, [
    { value: 'LEVEL_001', label: '初级' },
    { value: 'LEVEL_002', label: '中级' },
  ])
})

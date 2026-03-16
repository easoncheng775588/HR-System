import test from 'node:test'
import assert from 'node:assert/strict'

import {
  DEFAULT_ATTACHMENT_EXTS,
  INTERVIEW_EVALUATION_EXTS,
  buildAcceptAttribute,
  getAllowedAttachmentExts,
} from '../src/components/attachmentUploadRules.js'

test('interview evaluation attachments only allow excel formats by default', () => {
  assert.deepEqual(getAllowedAttachmentExts('interview_evaluation'), INTERVIEW_EVALUATION_EXTS)
})

test('resume attachments keep the shared default formats', () => {
  assert.deepEqual(getAllowedAttachmentExts('resume'), DEFAULT_ATTACHMENT_EXTS)
})

test('accept attribute is built from extension list', () => {
  assert.equal(buildAcceptAttribute(['xls', 'xlsx']), '.xls,.xlsx')
})

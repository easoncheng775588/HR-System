export const DEFAULT_ATTACHMENT_EXTS = ['zip', 'xls', 'xlsx', 'doc', 'docx', 'pdf', 'ppt', 'pptx']
export const INTERVIEW_EVALUATION_EXTS = ['xls', 'xlsx']

export const getAllowedAttachmentExts = (businessType, customExts) => {
  if (Array.isArray(customExts) && customExts.length > 0) {
    return customExts
  }
  if (businessType === 'interview_evaluation') {
    return INTERVIEW_EVALUATION_EXTS
  }
  return DEFAULT_ATTACHMENT_EXTS
}

export const buildAcceptAttribute = (exts) => (exts || []).map((ext) => `.${ext}`).join(',')

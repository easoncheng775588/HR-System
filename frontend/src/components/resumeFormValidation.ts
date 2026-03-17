import type { AttachmentItem } from './AttachmentUploader'

const CREATE_REQUIRED_MESSAGE_MAP: Record<string, string> = {
  relatedRequestIds: '请选择关联需求',
  candidateName: '请输入候选人',
  gender: '请选择性别',
  birthDate: '请输入出生年月日',
  firstDegree: '请选择第一学历',
  firstDegreeGraduateYear: '请输入第一学历毕业年份',
  firstDegreeSchool: '请输入第一学历毕业院校',
  firstDegreeMajor: '请输入第一学历毕业专业',
  firstDegreeFullTime: '请选择第一学历是否全日制',
  highestDegree: '请选择最高学历',
  highestDegreeMajor: '请输入最高学历毕业专业',
  highestDegreeGraduateYear: '请输入最高学历毕业年份',
  highestDegreeSchool: '请输入最高学历毕业院校',
  highestDegreeFullTime: '请选择最高学历是否全日制',
  englishLevel: '请选择英语水平',
  candidatePlatform: '请选择候选人技术平台',
  appliedCategory: '请选择申请岗位',
  appliedLevel: '请选择申请职级',
  itWorkYears: '请输入IT工作年限',
  itInternshipYears: '请输入IT实习年限',
  latestCompany: '请输入最近服务的公司名称',
  interviewAvailableTime: '请选择可参加面试时间段',
  inShenzhen: '请选择候选人是否在深圳',
  onboardDate: '请选择可到岗时间',
  supplierInitialInterview: '请选择供应商是否已初面',
  writtenTestScore: '请输入笔试成绩',
  supplierInterviewComment: '请输入供应商初面意见',
  remark: '请输入备注',
}

export const getResumeCreateRequiredRule = (fieldName: string) => {
  const message = CREATE_REQUIRED_MESSAGE_MAP[fieldName]
  return message ? [{ required: true, message }] : []
}

export const validateCreateResumeAttachments = (files: AttachmentItem[]) => {
  const doneFiles = (files || []).filter((file) => (file.status || 'done') === 'done')
  return doneFiles.length > 0
}


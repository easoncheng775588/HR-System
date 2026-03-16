import React, { useEffect, useState } from 'react'
import { Button, DatePicker, Descriptions, Drawer, Form, Input, Select, Space, message } from 'antd'
import { useParam } from '../contexts/ParamContext'
import AttachmentUploader, { AttachmentItem } from './AttachmentUploader'
import { getInterviewEvaluationDetail, submitInterviewEvaluation } from '../services/interviewApi'
import { HIRE_SUGGESTION_OPTIONS, formatDateTimeText, getInterviewMethodLabel } from '../utils/interviewStatus'
import { buildEntryLevelOptions } from './interviewEvaluationHelpers'
import { buildInterviewEvaluationInitialValues, formatInterviewEvaluationDatePayload } from './interviewEvaluationFormHelpers'

interface InterviewEvaluationDrawerProps {
  open: boolean
  record?: Record<string, unknown> | null
  operator?: Record<string, unknown>
  onClose: () => void
  onSubmitted?: () => void
}

const InterviewEvaluationDrawer: React.FC<InterviewEvaluationDrawerProps> = ({
  open,
  record,
  operator = {},
  onClose,
  onSubmitted,
}) => {
  const [form] = Form.useForm()
  const { getLevelOptions } = useParam()
  const [submitting, setSubmitting] = useState(false)
  const [loadingDetail, setLoadingDetail] = useState(false)
  const [files, setFiles] = useState<AttachmentItem[]>([])
  const levelOptions = buildEntryLevelOptions(getLevelOptions())

  useEffect(() => {
    if (!open) {
      form.resetFields()
      setFiles([])
      return
    }
    form.setFieldsValue(buildInterviewEvaluationInitialValues(record || {}))
    const candidateAttachmentUrl = String(record?.attachmentUrl || '').trim()
    if (candidateAttachmentUrl) {
      setFiles([
        {
          uid: 'evaluation-init',
          fileName: String(record?.attachmentName || '面试评价附件'),
          fileUrl: candidateAttachmentUrl,
          fileKey: '',
          status: 'done',
        },
      ])
    }
  }, [open, record, form])

  useEffect(() => {
    if (!open || !record?.evaluationId) return
    const loadDetail = async () => {
      setLoadingDetail(true)
      try {
        const res = await getInterviewEvaluationDetail(String(record.evaluationId))
        if (res.data?.returnCode === 'SUC0000' && res.data?.body) {
          const evaluation = res.data.body?.evaluation || res.data.body
          form.setFieldsValue(buildInterviewEvaluationInitialValues(evaluation || {}))
        }
      } catch (_error) {
        message.error('获取面试评价详情失败')
      } finally {
        setLoadingDetail(false)
      }
    }
    loadDetail()
  }, [open, record, form])

  const submit = async () => {
    try {
      const values = await form.validateFields()
      const payload = {
        resumeId: Number(record?.resumeId || 0),
        operatorUserId: String(operator?.userId || ''),
        operatorUserName: String(operator?.realName || operator?.username || ''),
        operatorRole: String(operator?.position || operator?.positionName || operator?.role || ''),
        entryLevelSuggestion: values.entryLevelSuggestion,
        score: values.score,
        interviewMethod: String(record?.interviewMethod || ''),
        interviewDate: formatInterviewEvaluationDatePayload(values.interviewDate),
        hireSuggestion: values.hireSuggestion,
        attachmentName: files[0]?.fileName || '',
        attachmentUrl: files[0]?.fileUrl || '',
      }
      setSubmitting(true)
      const res = await submitInterviewEvaluation(payload)
      if (res.data?.returnCode === 'SUC0000') {
        message.success('提交成功')
        onSubmitted?.()
        onClose()
      } else {
        message.error(res.data?.errorMsg || '提交失败')
      }
    } catch (error) {
      if (!error?.errorFields) {
        message.error(error?.message || '提交失败')
      }
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <Drawer
      title="发起面试评价"
      placement="right"
      width={720}
      open={open}
      onClose={onClose}
      extra={
        <Space>
          <Button onClick={onClose}>取消</Button>
          <Button type="primary" loading={submitting || loadingDetail} onClick={submit}>
            提交
          </Button>
        </Space>
      }
    >
      <Descriptions column={1} size="small">
        <Descriptions.Item label="候选人">{String(record?.candidateName || '-')}</Descriptions.Item>
        <Descriptions.Item label="候选人性别">
          {record?.candidateGender || (record?.gender === 'MALE' ? '男' : record?.gender === 'FEMALE' ? '女' : '-')}
        </Descriptions.Item>
        <Descriptions.Item label="工作年限">{String(record?.workYears || record?.itWorkYears || '-')}</Descriptions.Item>
        <Descriptions.Item label="应聘级别">{String(record?.appliedLevel || '-')}</Descriptions.Item>
        <Descriptions.Item label="来源（供应商）">{String(record?.supplierName || '-')}</Descriptions.Item>
        <Descriptions.Item label="面试官">{String(record?.confirmedInterviewerName || '-')}</Descriptions.Item>
        <Descriptions.Item label="技术平台">{String(record?.candidatePlatform || record?.platform || '-')}</Descriptions.Item>
        <Descriptions.Item label="面试方式">{getInterviewMethodLabel(String(record?.interviewMethod || ''))}</Descriptions.Item>
        <Descriptions.Item label="已确认面试时间">{formatDateTimeText(String(record?.confirmedInterviewTime || ''))}</Descriptions.Item>
      </Descriptions>

      <Form form={form} layout="vertical" style={{ marginTop: 16 }}>
        <Form.Item name="entryLevelSuggestion" label="入场职级建议" rules={[{ required: true, message: '请选择入场职级建议' }]}>
          <Select placeholder="请选择入场职级建议" options={levelOptions} />
        </Form.Item>
        <Form.Item name="score" label="面试评价得分" rules={[{ required: true, message: '请输入面试评价得分' }]}>
          <Input />
        </Form.Item>
        <Form.Item name="interviewDate" label="面试日期" rules={[{ required: true, message: '请选择面试日期' }]}>
          <DatePicker style={{ width: '100%' }} />
        </Form.Item>
        <Form.Item name="hireSuggestion" label="录用建议" rules={[{ required: true, message: '请选择录用建议' }]}>
          <Select options={HIRE_SUGGESTION_OPTIONS} />
        </Form.Item>
        <Form.Item label="附件（面试评价表）">
          <AttachmentUploader businessType="interview_evaluation" value={files} onChange={setFiles} multiple={false} maxCount={1} />
        </Form.Item>
      </Form>
    </Drawer>
  )
}

export default InterviewEvaluationDrawer

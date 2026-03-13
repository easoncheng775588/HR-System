import React, { useEffect } from 'react'
import { DatePicker, Form, Input, Modal, Select, message } from 'antd'
import dayjs from 'dayjs'
import { INTERVIEW_METHOD, INTERVIEW_METHOD_OPTIONS } from '../utils/interviewStatus'

interface ResumeConfirmDialogProps {
  open: boolean
  submitting?: boolean
  onCancel: () => void
  onSubmit: (payload: Record<string, unknown>) => Promise<void> | void
}

const ResumeConfirmDialog: React.FC<ResumeConfirmDialogProps> = ({
  open,
  submitting = false,
  onCancel,
  onSubmit,
}) => {
  const [form] = Form.useForm()
  const method = Form.useWatch('interviewMethod', form)

  useEffect(() => {
    if (!open) form.resetFields()
  }, [open, form])

  const submit = async () => {
    const values = await form.validateFields()
    const range = values.availableRange || []
    const start = range[0]
    const end = range[1]
    if (!start || !end) {
      message.error('面试官可面试时间段不能为空')
      return
    }
    if (start.valueOf() > end.valueOf()) {
      message.error('面试开始时间不能晚于结束时间')
      return
    }
    await onSubmit({
      interviewMethod: values.interviewMethod,
      meetingNo: values.meetingNo || '',
      availableStartTime: dayjs(start).format('YYYY-MM-DD HH:mm:ss'),
      availableEndTime: dayjs(end).format('YYYY-MM-DD HH:mm:ss'),
    })
  }

  return (
    <Modal title="简历确认" open={open} onCancel={onCancel} onOk={submit} confirmLoading={submitting}>
      <Form form={form} layout="vertical">
        <Form.Item name="interviewMethod" label="面试方式" rules={[{ required: true, message: '面试方式不能为空' }]}>
          <Select options={INTERVIEW_METHOD_OPTIONS} placeholder="请选择面试方式" />
        </Form.Item>
        <Form.Item
          name="meetingNo"
          label="会议号"
          rules={[
            {
              validator: async (_, value) => {
                if (method === INTERVIEW_METHOD.ONLINE && !String(value || '').trim()) {
                  throw new Error('线上面试会议号不能为空')
                }
              },
            },
          ]}
        >
          <Input placeholder={method === INTERVIEW_METHOD.ONLINE ? '请输入会议号' : '线下面试无需填写'} disabled={method !== INTERVIEW_METHOD.ONLINE} />
        </Form.Item>
        <Form.Item
          name="availableRange"
          label="面试官可面试时间段"
          rules={[{ required: true, message: '面试官可面试时间段不能为空' }]}
        >
          <DatePicker.RangePicker showTime style={{ width: '100%' }} />
        </Form.Item>
      </Form>
    </Modal>
  )
}

export default ResumeConfirmDialog

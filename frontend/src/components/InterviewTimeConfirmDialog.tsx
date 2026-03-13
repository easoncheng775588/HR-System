import React, { useEffect } from 'react'
import { DatePicker, Form, Modal } from 'antd'
import dayjs from 'dayjs'

interface InterviewTimeConfirmDialogProps {
  open: boolean
  submitting?: boolean
  initialInterviewTime?: string
  onCancel: () => void
  onSubmit: (payload: { interviewTime: string }) => Promise<void> | void
}

const InterviewTimeConfirmDialog: React.FC<InterviewTimeConfirmDialogProps> = ({
  open,
  submitting = false,
  initialInterviewTime,
  onCancel,
  onSubmit,
}) => {
  const [form] = Form.useForm()

  useEffect(() => {
    if (!open) {
      form.resetFields()
      return
    }
    form.setFieldsValue({
      interviewTime: initialInterviewTime ? dayjs(initialInterviewTime) : undefined,
    })
  }, [open, initialInterviewTime, form])

  const submit = async () => {
    const values = await form.validateFields()
    await onSubmit({
      interviewTime: dayjs(values.interviewTime).format('YYYY-MM-DD HH:mm:ss'),
    })
  }

  return (
    <Modal title="确认面试时间" open={open} onCancel={onCancel} onOk={submit} confirmLoading={submitting}>
      <Form form={form} layout="vertical">
        <Form.Item name="interviewTime" label="面试时间" rules={[{ required: true, message: '面试时间不能为空' }]}>
          <DatePicker showTime style={{ width: '100%' }} />
        </Form.Item>
      </Form>
    </Modal>
  )
}

export default InterviewTimeConfirmDialog

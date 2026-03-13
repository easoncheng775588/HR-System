import React from 'react'
import { Button, Upload, message } from 'antd'
import { UploadOutlined } from '@ant-design/icons'

export interface AttachmentItem {
  uid: string
  fileName: string
  fileUrl: string
  fileKey?: string
  status?: string
}

interface AttachmentUploaderProps {
  value?: AttachmentItem[]
  onChange?: (files: AttachmentItem[]) => void
  businessType: 'resume' | 'interview_evaluation'
  acceptExts?: string[]
  multiple?: boolean
  maxCount?: number
}

const DEFAULT_EXTS = ['zip', 'xls', 'xlsx', 'doc', 'docx', 'pdf', 'ppt', 'pptx']

const normalizeFileUrl = (rawUrl: string) => {
  const text = String(rawUrl || '').trim()
  if (!text) return ''
  if (text.startsWith('/api/uploads/')) return text
  if (text.startsWith('/uploads/')) return `/api${text}`
  if (text.startsWith('http://') || text.startsWith('https://')) return text
  if (text.startsWith('uploads/')) return `/api/${text}`
  return `/api/uploads/${text}`
}

const AttachmentUploader: React.FC<AttachmentUploaderProps> = ({
  value = [],
  onChange,
  businessType,
  acceptExts = DEFAULT_EXTS,
  multiple = true,
  maxCount = 10,
}) => {
  const upload = async ({ file, onSuccess, onError }) => {
    const name = String(file?.name || '')
    const ext = name.includes('.') ? name.split('.').pop()?.toLowerCase() : ''
    if (!ext || !acceptExts.includes(ext)) {
      message.error(`附件格式仅支持：${acceptExts.join('/')}`)
      onError?.(new Error('invalid_file_type'))
      return
    }

    const formData = new FormData()
    const rawFile = file?.originFileObj || file
    formData.append('file', rawFile)
    formData.append('resume', rawFile)
    formData.append('businessType', businessType)

    try {
      const token = localStorage.getItem('token')
      const response = await fetch('http://localhost:8080/api/upload', {
        method: 'POST',
        headers: token ? { Authorization: `Bearer ${token}` } : undefined,
        body: formData,
      })
      const result = await response.json()
      if (result?.returnCode !== 'SUC0000') {
        throw new Error(result?.errorMsg || 'upload_failed')
      }
      const body = result.body || {}
      const fileUrl = normalizeFileUrl(body.url || body.fileUrl || body.path || '')
      const fileName = String(body.name || name)
      onSuccess?.({ fileName, fileUrl, fileKey: body.fileKey || '' }, file)
      message.success(`附件上传成功：${fileName}`)
    } catch (error) {
      message.error(`上传失败: ${error?.message || '未知错误'}`)
      onError?.(error)
    }
  }

  const fileList = value.map((item) => ({
    uid: item.uid,
    name: item.fileName,
    status: item.status || 'done',
    url: item.fileUrl,
    response: {
      fileName: item.fileName,
      fileUrl: item.fileUrl,
      fileKey: item.fileKey || '',
    },
  }))

  return (
    <Upload
      multiple={multiple}
      maxCount={maxCount}
      customRequest={upload}
      fileList={fileList}
      onChange={({ fileList: changed }) => {
        const next = changed.map((file) => {
          const resp = file.response || {}
          return {
            uid: String(file.uid),
            fileName: String(file.name || resp.fileName || ''),
            fileUrl: normalizeFileUrl(String(file.url || resp.fileUrl || resp.url || '')),
            fileKey: String(resp.fileKey || ''),
            status: String(file.status || 'done'),
          }
        })
        onChange?.(next)
      }}
    >
      <Button icon={<UploadOutlined />}>上传附件</Button>
    </Upload>
  )
}

export default AttachmentUploader

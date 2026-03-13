import React, { useCallback, useEffect, useMemo, useState } from 'react'
import { Button, Card, Space, Table, message } from 'antd'
import { ClockCircleOutlined, FormOutlined } from '@ant-design/icons'
import { useAuth } from '../contexts/AuthContext'
import {
  confirmInterviewTime,
  getPendingInterviewArrangement,
} from '../services/interviewApi'
import {
  canConfirmInterviewTime,
  canLaunchInterviewEvaluation,
} from '../utils/interviewPermission'
import {
  formatDateTimeText,
  formatInterviewTimeRange,
  getInterviewMethodLabel,
} from '../utils/interviewStatus'
import InterviewTimeConfirmDialog from './InterviewTimeConfirmDialog'
import InterviewEvaluationDrawer from './InterviewEvaluationDrawer'

const InterviewScheduling = () => {
  const { user } = useAuth()
  const [loading, setLoading] = useState(false)
  const [rows, setRows] = useState([])
  const [isMobile, setIsMobile] = useState(false)
  const [confirmVisible, setConfirmVisible] = useState(false)
  const [confirmSubmitting, setConfirmSubmitting] = useState(false)
  const [currentRecord, setCurrentRecord] = useState(null)
  const [evaluationVisible, setEvaluationVisible] = useState(false)
  const [evaluationRecord, setEvaluationRecord] = useState(null)

  const fetchPending = useCallback(async () => {
    setLoading(true)
    try {
      const response = await getPendingInterviewArrangement({
        viewerId: String(user?.userId || ''),
        viewerRole: String(user?.position || user?.positionName || user?.role || ''),
      })
      if (response.data?.returnCode === 'SUC0000') {
        setRows(response.data.body || [])
      } else {
        message.error(response.data?.errorMsg || '获取待安排面试简历失败')
      }
    } catch (_error) {
      message.error('获取待安排面试简历失败，请稍后重试')
    } finally {
      setLoading(false)
    }
  }, [user])

  useEffect(() => {
    const handleResize = () => setIsMobile(window.innerWidth < 768)
    handleResize()
    window.addEventListener('resize', handleResize)
    fetchPending()
    return () => window.removeEventListener('resize', handleResize)
  }, [fetchPending])

  const openConfirmDialog = (record) => {
    setCurrentRecord(record)
    setConfirmVisible(true)
  }

  const submitConfirmTime = async (payload: { interviewTime: string }) => {
    if (!currentRecord?.resumeId) return
    try {
      setConfirmSubmitting(true)
      const response = await confirmInterviewTime(currentRecord.resumeId, {
        interviewTime: payload.interviewTime,
        operatorUserId: String(user?.userId || ''),
        operatorUserName: String(user?.realName || user?.username || ''),
        operatorRole: String(user?.position || user?.positionName || user?.role || ''),
      })
      if (response.data?.returnCode === 'SUC0000') {
        message.success('操作成功')
        setConfirmVisible(false)
        setCurrentRecord(null)
        await fetchPending()
      } else {
        message.error(response.data?.errorMsg || '操作失败')
      }
    } catch (_error) {
      message.error('操作失败，请稍后重试')
    } finally {
      setConfirmSubmitting(false)
    }
  }

  const dispatchStatusLabel = (value: unknown) => {
    const normalized = String(value || '').toUpperCase()
    if (normalized === 'PENDING') return '待面试官确认'
    if (normalized === 'CONFIRMED') return '已确认'
    if (normalized === 'ABANDONED') return '已放弃'
    return '-'
  }

  const arrangementColumns = useMemo(
    () => [
      { title: '候选人', dataIndex: 'candidateName', key: 'candidateName', width: 140 },
      {
        title: '关联需求',
        dataIndex: 'relatedRequestNames',
        key: 'relatedRequestNames',
        width: 220,
        ellipsis: true,
        render: (value) => value || '-',
      },
      { title: '供应商', dataIndex: 'supplierName', key: 'supplierName', width: 160, render: (v) => v || '-' },
      {
        title: '分发状态',
        dataIndex: 'dispatchStatus',
        key: 'dispatchStatus',
        width: 140,
        render: (value) => dispatchStatusLabel(value),
      },
      {
        title: '面试官',
        dataIndex: 'confirmedInterviewerName',
        key: 'confirmedInterviewerName',
        width: 140,
        render: (value) => value || '-',
      },
      {
        title: '面试方式',
        dataIndex: 'interviewMethod',
        key: 'interviewMethod',
        width: 120,
        render: (value) => getInterviewMethodLabel(value),
      },
      { title: '会议号', dataIndex: 'meetingNo', key: 'meetingNo', width: 180, render: (v) => v || '-' },
      {
        title: '可面试时间段',
        key: 'availableRange',
        width: 280,
        render: (_, record) => formatInterviewTimeRange(record.availableStartTime, record.availableEndTime),
      },
      {
        title: '已确认面试时间',
        dataIndex: 'confirmedInterviewTime',
        key: 'confirmedInterviewTime',
        width: 180,
        render: (value) => formatDateTimeText(value),
      },
      {
        title: '操作',
        key: 'action',
        fixed: 'right' as const,
        width: 180,
        render: (_, record) => (
          <Space size="small">
            {canConfirmInterviewTime(user || {}, record || {}) && (
              <Button type="link" icon={<ClockCircleOutlined />} onClick={() => openConfirmDialog(record)}>
                确认面试时间
              </Button>
            )}
          </Space>
        ),
      },
    ],
    [user],
  )

  const evaluationColumns = useMemo(
    () => [
      { title: '候选人', dataIndex: 'candidateName', key: 'candidateName', width: 140 },
      { title: '供应商', dataIndex: 'supplierName', key: 'supplierName', width: 160, render: (v) => v || '-' },
      {
        title: '已确认面试官',
        dataIndex: 'confirmedInterviewerName',
        key: 'confirmedInterviewerName',
        width: 160,
        render: (value) => value || '-',
      },
      {
        title: '已确认面试时间',
        dataIndex: 'confirmedInterviewTime',
        key: 'confirmedInterviewTime',
        width: 180,
        render: (value) => formatDateTimeText(value),
      },
      {
        title: '面试评价状态',
        dataIndex: 'evaluationStatus',
        key: 'evaluationStatus',
        width: 150,
        render: (v) => v || '-',
      },
      {
        title: '操作',
        key: 'action',
        fixed: 'right' as const,
        width: 220,
        render: (_, record) => (
          <Space size="small">
            {canLaunchInterviewEvaluation(user || {}, record || {}) && (
              <Button
                type="link"
                icon={<FormOutlined />}
                onClick={() => {
                  setEvaluationRecord(record)
                  setEvaluationVisible(true)
                }}
              >
                发起面试评价
              </Button>
            )}
          </Space>
        ),
      },
    ],
    [user],
  )

  const evaluationRows = useMemo(
    () => (rows || []).filter((item) => String(item?.dispatchStatus || '').toUpperCase() === 'CONFIRMED'),
    [rows],
  )

  return (
    <div className="app-page">
      <Card title="待安排面试简历">
        <div className="app-table-wrap">
          <Table
            rowKey="resumeId"
            loading={loading}
            dataSource={rows}
            columns={arrangementColumns}
            scroll={{ x: isMobile ? 1300 : 1700 }}
            pagination={{
              defaultPageSize: 10,
              showSizeChanger: true,
              pageSizeOptions: [10, 20, 30, 50, 100],
              showTotal: (total) => `共 ${total} 条记录`,
            }}
          />
        </div>
      </Card>

      <Card title="面试评价模块" style={{ marginTop: 16 }}>
        <div className="app-table-wrap">
          <Table
            rowKey="resumeId"
            loading={loading}
            dataSource={evaluationRows}
            columns={evaluationColumns}
            scroll={{ x: isMobile ? 1000 : 1300 }}
            pagination={{
              defaultPageSize: 10,
              showSizeChanger: true,
              pageSizeOptions: [10, 20, 30, 50, 100],
              showTotal: (total) => `共 ${total} 条记录`,
            }}
          />
        </div>
      </Card>

      <InterviewTimeConfirmDialog
        open={confirmVisible}
        submitting={confirmSubmitting}
        initialInterviewTime={String(currentRecord?.confirmedInterviewTime || '')}
        onCancel={() => {
          setConfirmVisible(false)
          setCurrentRecord(null)
        }}
        onSubmit={submitConfirmTime}
      />

      <InterviewEvaluationDrawer
        open={evaluationVisible}
        record={evaluationRecord}
        operator={user || {}}
        onClose={() => {
          setEvaluationVisible(false)
          setEvaluationRecord(null)
        }}
        onSubmitted={fetchPending}
      />
    </div>
  )
}

export default InterviewScheduling

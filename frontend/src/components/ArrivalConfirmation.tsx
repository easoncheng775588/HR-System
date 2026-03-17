import React, { useCallback, useEffect, useMemo, useState } from 'react'
import { Button, Card, Form, Popconfirm, Table, Tag, message } from 'antd'
import dayjs from 'dayjs'
import api from '../utils/api'
import { useAuth } from '../contexts/AuthContext'
import { useParam } from '../contexts/ParamContext'
import ArrivalConfirmationFormModal from './ArrivalConfirmationFormModal'
import { formatApprovalStatus, isOutsourcingCreator } from './arrivalConfirmationHelpers'

const ArrivalConfirmation = () => {
  const { user } = useAuth()
  const { getLevelText, getLevelOptions } = useParam()
  const [loading, setLoading] = useState(false)
  const [rows, setRows] = useState<any[]>([])
  const [optionsLoading, setOptionsLoading] = useState(false)
  const [options, setOptions] = useState<any>({
    candidates: [],
    suppliers: [],
    supplierHrs: [],
    roomManagers: [],
    orgUnits: [],
  })
  const [modalOpen, setModalOpen] = useState(false)
  const [submitting, setSubmitting] = useState(false)
  const [form] = Form.useForm()

  const canCreate = useMemo(() => isOutsourcingCreator(user), [user])
  const levelOptions = useMemo(() => getLevelOptions(), [getLevelOptions])

  const fetchList = useCallback(async () => {
    setLoading(true)
    try {
      const res = await api.get('/api/arrival-confirmation/list', {
        params: {
          viewerId: String(user?.userId || ''),
          viewerRole: user?.position || user?.role || '',
        },
      })
      if (res.data?.returnCode === 'SUC0000') {
        setRows(res.data.body || [])
      } else {
        message.error(res.data?.errorMsg || '获取到岗确认列表失败')
      }
    } catch (_error) {
      message.error('获取到岗确认列表失败')
    } finally {
      setLoading(false)
    }
  }, [user])

  const fetchOptions = useCallback(async () => {
    if (!canCreate) return
    setOptionsLoading(true)
    try {
      const res = await api.get('/api/arrival-confirmation/options', {
        params: {
          operatorUserId: String(user?.userId || ''),
          operatorUserRole: user?.position || user?.role || '',
        },
      })
      if (res.data?.returnCode === 'SUC0000') {
        setOptions(res.data.body || {})
      } else {
        message.error(res.data?.errorMsg || '获取到岗确认表单选项失败')
      }
    } catch (_error) {
      message.error('获取到岗确认表单选项失败')
    } finally {
      setOptionsLoading(false)
    }
  }, [canCreate, user])

  useEffect(() => {
    fetchList()
    fetchOptions()
  }, [fetchList, fetchOptions])

  const openCreate = useCallback(() => {
    form.resetFields()
    setModalOpen(true)
  }, [form])

  const closeCreate = useCallback(() => {
    setModalOpen(false)
    form.resetFields()
  }, [form])

  const handleSubmit = useCallback(async () => {
    const values = await form.validateFields()
    setSubmitting(true)
    try {
      const res = await api.post('/api/arrival-confirmation/submit', {
        entryRecordId: values.entryRecordId,
        supplierId: values.supplierId,
        supplierHrUserId: values.supplierHrUserId,
        targetOrgUnitName: values.targetOrgUnitName,
        roomManagerUserId: values.roomManagerUserId,
        entryDate: values.entryDate ? values.entryDate.format('YYYY-MM-DD') : '',
        positionLevel: values.positionLevel,
        operatorUserId: String(user?.userId || ''),
        operatorUserName: user?.realName || '系统用户',
        operatorUserRole: user?.position || user?.role || '',
      })
      if (res.data?.returnCode === 'SUC0000') {
        message.success('发起成功')
        closeCreate()
        await fetchList()
      } else {
        message.error(res.data?.errorMsg || '发起到岗确认失败')
      }
    } catch (_error) {
      message.error('发起到岗确认失败')
    } finally {
      setSubmitting(false)
    }
  }, [closeCreate, fetchList, form, user])

  const handleDelete = useCallback(async (arrivalConfirmationId: number) => {
    try {
      const res = await api.delete(`/api/arrival-confirmation/${arrivalConfirmationId}`, {
        params: {
          operatorUserId: String(user?.userId || ''),
          operatorUserRole: user?.position || user?.role || '',
        },
      })
      if (res.data?.returnCode === 'SUC0000') {
        message.success('删除成功')
        await fetchList()
      } else {
        message.error(res.data?.errorMsg || '删除失败')
      }
    } catch (_error) {
      message.error('删除失败')
    }
  }, [fetchList, user])

  const columns = useMemo(() => ([
    { title: '到岗人员', dataIndex: 'candidateName', key: 'candidateName', width: 140 },
    { title: '所属外包供应商', dataIndex: 'supplierName', key: 'supplierName', width: 180 },
    { title: '供应商HR', dataIndex: 'supplierHrUserName', key: 'supplierHrUserName', width: 140 },
    { title: '用人团队/部室', dataIndex: 'targetOrgUnitName', key: 'targetOrgUnitName', width: 220 },
    { title: '所属室经理', dataIndex: 'roomManagerUserName', key: 'roomManagerUserName', width: 140 },
    {
      title: '人员进场日期',
      dataIndex: 'entryDate',
      key: 'entryDate',
      width: 140,
      render: (value: string) => (value ? dayjs(value).format('YYYY-MM-DD') : '-'),
    },
    {
      title: '人员级别',
      dataIndex: 'positionLevel',
      key: 'positionLevel',
      width: 120,
      render: (value: string) => getLevelText(value),
    },
    { title: '创建人', dataIndex: 'createUserName', key: 'createUserName', width: 120 },
    {
      title: '创建时间',
      dataIndex: 'createTime',
      key: 'createTime',
      width: 180,
      render: (value: string) => (value ? dayjs(value).format('YYYY-MM-DD HH:mm:ss') : '-'),
    },
    { title: '当前审批环节', dataIndex: 'currentApprovalNode', key: 'currentApprovalNode', width: 180 },
    {
      title: '状态',
      dataIndex: 'approvalStatus',
      key: 'approvalStatus',
      width: 120,
      render: (value: string) => {
        const text = formatApprovalStatus(value)
        if (text === '已通过') return <Tag color="success">{text}</Tag>
        if (text === '已拒绝') return <Tag color="error">{text}</Tag>
        return <Tag color="processing">{text}</Tag>
      },
    },
    {
      title: '操作',
      key: 'action',
      width: 120,
      fixed: 'right' as const,
      render: (_: unknown, record: any) => {
        if (!record.canDelete) {
          return '-'
        }
        return (
          <Popconfirm title="确认删除这条到岗确认吗？" onConfirm={() => handleDelete(record.arrivalConfirmationId)}>
            <Button type="link" danger>
              删除
            </Button>
          </Popconfirm>
        )
      },
    },
  ]), [getLevelText, handleDelete])

  return (
    <div className="app-page">
      <Card
        title="到岗确认"
        extra={canCreate ? (
          <Button type="primary" onClick={openCreate} loading={optionsLoading}>
            发起到岗确认
          </Button>
        ) : null}
      >
        <div className="app-table-wrap">
          <Table
            rowKey="arrivalConfirmationId"
            loading={loading}
            dataSource={rows}
            columns={columns}
            scroll={{ x: 1800 }}
            pagination={{
              defaultPageSize: 10,
              showSizeChanger: true,
              pageSizeOptions: [10, 20, 30, 50, 100],
              showTotal: (total) => `共 ${total} 条记录`,
            }}
          />
        </div>
      </Card>

      <ArrivalConfirmationFormModal
        open={modalOpen}
        submitting={submitting}
        form={form}
        candidates={options.candidates || []}
        suppliers={options.suppliers || []}
        supplierHrs={options.supplierHrs || []}
        roomManagers={options.roomManagers || []}
        orgUnits={options.orgUnits || []}
        levelOptions={levelOptions}
        onCancel={closeCreate}
        onSubmit={handleSubmit}
      />
    </div>
  )
}

export default ArrivalConfirmation

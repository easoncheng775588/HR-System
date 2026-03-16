import React, { useCallback, useEffect, useMemo, useState } from 'react'
import { Button, Card, Form, Modal, Select, Space, Table, Tag, message } from 'antd'
import { CheckCircleOutlined, SendOutlined } from '@ant-design/icons'
import api from '../utils/api'
import { useAuth } from '../contexts/AuthContext'

const DemandManagement = () => {
  const { user } = useAuth()
  const [loading, setLoading] = useState(false)
  const [rows, setRows] = useState([])
  const [supplierOptions, setSupplierOptions] = useState([])
  const [dispatchVisible, setDispatchVisible] = useState(false)
  const [dispatchDemandId, setDispatchDemandId] = useState(null)
  const [dispatching, setDispatching] = useState(false)
  const [form] = Form.useForm()

  const roleText = `${user?.position || ''} ${user?.role || ''}`
  const isSuperAdmin = String(user?.userId || '') === '1001'
  const isOutsourcingManager = roleText.includes('外包招聘管理')
  const isSupplierHr = roleText.includes('供应商HR')

  const fetchList = useCallback(async () => {
    setLoading(true)
    try {
      const res = await api.get('/api/demand-management/list', {
        params: {
          viewerId: String(user?.userId || ''),
          viewerRole: user?.position || user?.role || '',
        },
      })
      if (res.data?.returnCode === 'SUC0000') {
        setRows(res.data.body || [])
      } else {
        message.error(res.data?.errorMsg || '获取需求列表失败')
      }
    } catch (_e) {
      message.error('获取需求列表失败')
    } finally {
      setLoading(false)
    }
  }, [user])

  const fetchSuppliers = useCallback(async () => {
    if (!isOutsourcingManager && !isSuperAdmin) return
    try {
      const res = await api.get('/api/demand-management/suppliers', {
        params: {
          viewerId: String(user?.userId || ''),
          viewerRole: user?.position || user?.role || '',
        },
      })
      if (res.data?.returnCode === 'SUC0000') {
        setSupplierOptions(res.data.body || [])
      }
    } catch (_e) {
      setSupplierOptions([])
    }
  }, [isOutsourcingManager, isSuperAdmin, user])

  useEffect(() => {
    fetchList()
    fetchSuppliers()
  }, [fetchList, fetchSuppliers])

  const openDispatch = (demandId) => {
    setDispatchDemandId(demandId)
    form.resetFields()
    setDispatchVisible(true)
  }

  const handleDispatch = async () => {
    const values = await form.validateFields()
    setDispatching(true)
    try {
      const res = await api.post(`/api/demand-management/${dispatchDemandId}/dispatch`, {
        supplierIds: values.supplierIds,
        operatorUserId: String(user?.userId || ''),
        operatorUserName: user?.realName || '系统用户',
      })
      if (res.data?.returnCode === 'SUC0000') {
        message.success('分发成功')
        setDispatchVisible(false)
        await fetchList()
      } else {
        message.error(res.data?.errorMsg || '分发失败')
      }
    } catch (_e) {
      message.error('分发失败')
    } finally {
      setDispatching(false)
    }
  }

  const handleConfirmReceive = async (demandId) => {
    try {
      const res = await api.post(`/api/demand-management/${demandId}/confirm`, {
        operatorUserId: String(user?.userId || ''),
        operatorUserName: user?.realName || '系统用户',
      })
      if (res.data?.returnCode === 'SUC0000') {
        message.success('操作成功')
        await fetchList()
      } else {
        message.error(res.data?.errorMsg || '确认接收失败')
      }
    } catch (_e) {
      message.error('确认接收失败')
    }
  }

  const acceptanceTag = (status) => {
    if (status === '全部确认接收') return <Tag color="success">{status}</Tag>
    if (status === '部分确认接收') return <Tag color="processing">{status}</Tag>
    return <Tag>--</Tag>
  }

  const managerColumns = useMemo(() => ([
    { title: '岗位/用人室组', dataIndex: 'positionOrgName', key: 'positionOrgName', width: 220 },
    { title: '空缺岗位', dataIndex: 'vacancyCount', key: 'vacancyCount', width: 100, align: 'center' },
    { title: '技术平台', dataIndex: 'technicalPlatform', key: 'technicalPlatform', width: 120 },
    { title: '招聘级别', dataIndex: 'recruitLevel', key: 'recruitLevel', width: 120 },
    { title: '招聘数量', dataIndex: 'recruitCount', key: 'recruitCount', width: 100, align: 'center' },
    { title: '岗位职责', dataIndex: 'positionResponsibility', key: 'positionResponsibility', width: 220, ellipsis: true },
    { title: '招聘要求', dataIndex: 'recruitRequirement', key: 'recruitRequirement', width: 220, ellipsis: true },
    { title: '需求接受状态', dataIndex: 'acceptanceStatus', key: 'acceptanceStatus', width: 140, render: acceptanceTag },
    { title: '分发供应商', dataIndex: 'dispatchSuppliers', key: 'dispatchSuppliers', width: 180, ellipsis: true },
    { title: '已接收供应商', dataIndex: 'acceptedSuppliers', key: 'acceptedSuppliers', width: 180, ellipsis: true },
    { title: '需求状态', dataIndex: 'demandStatus', key: 'demandStatus', width: 120 },
    { title: '创建时间', dataIndex: 'createTime', key: 'createTime', width: 180, render: (v) => (v ? new Date(v).toLocaleString('zh-CN') : '-') },
    { title: '修改时间', dataIndex: 'updateTime', key: 'updateTime', width: 180, render: (v) => (v ? new Date(v).toLocaleString('zh-CN') : '-') },
    { title: '创建用户', dataIndex: 'createUserName', key: 'createUserName', width: 120 },
    { title: '修改用户', dataIndex: 'updateUserName', key: 'updateUserName', width: 120 },
    {
      title: '操作',
      key: 'action',
      width: 120,
      fixed: 'right',
      render: (_, row) => (
        <Button type="link" icon={<SendOutlined />} onClick={() => openDispatch(row.demandId)}>
          分发
        </Button>
      ),
    },
  ]), [])

  const supplierColumns = useMemo(() => ([
    { title: '岗位/用人室组', dataIndex: 'positionOrgName', key: 'positionOrgName', width: 220 },
    { title: '空缺岗位', dataIndex: 'vacancyCount', key: 'vacancyCount', width: 100, align: 'center' },
    { title: '技术平台', dataIndex: 'technicalPlatform', key: 'technicalPlatform', width: 120 },
    { title: '招聘级别', dataIndex: 'recruitLevel', key: 'recruitLevel', width: 120 },
    { title: '招聘数量', dataIndex: 'recruitCount', key: 'recruitCount', width: 100, align: 'center' },
    { title: '岗位职责', dataIndex: 'positionResponsibility', key: 'positionResponsibility', width: 260, ellipsis: true },
    { title: '招聘要求', dataIndex: 'recruitRequirement', key: 'recruitRequirement', width: 260, ellipsis: true },
    {
      title: '操作',
      key: 'action',
      width: 140,
      fixed: 'right',
      render: (_, row) => {
        if (!row.canConfirm) {
          return null
        }
        return (
          <Button
            type="link"
            icon={<CheckCircleOutlined />}
            onClick={() => handleConfirmReceive(row.demandId)}
          >
            确认接收
          </Button>
        )
      },
    },
  ]), [])

  const columns = isSupplierHr && !isOutsourcingManager && !isSuperAdmin ? supplierColumns : managerColumns

  return (
    <div className="app-page">
      <Card title="需求管理">
        <div className="app-table-wrap">
          <Table
            rowKey="demandId"
            loading={loading}
            dataSource={rows}
            columns={columns}
            scroll={{ x: 2200 }}
            pagination={{
              defaultPageSize: 10,
              showSizeChanger: true,
              pageSizeOptions: [10, 20, 30, 50, 100],
              showTotal: (total) => `共 ${total} 条记录`,
            }}
          />
        </div>
      </Card>

      <Modal
        title="分发需求"
        open={dispatchVisible}
        onCancel={() => setDispatchVisible(false)}
        onOk={handleDispatch}
        confirmLoading={dispatching}
      >
        <Form form={form} layout="vertical">
          <Form.Item
            name="supplierIds"
            label="分发供应商"
            rules={[{ required: true, message: '请选择供应商' }]}
          >
            <Select
              mode="multiple"
              placeholder="请选择供应商"
              options={supplierOptions.map((item) => ({
                label: `${item.supplierName}（HR:${item.hrCount || 0}）`,
                value: item.supplierId,
              }))}
            />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  )
}

export default DemandManagement

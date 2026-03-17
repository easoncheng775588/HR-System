import React, { useCallback, useEffect, useMemo, useState } from 'react'
import { Button, Card, DatePicker, Form, Input, Modal, Select, Space, Table, Tag, message } from 'antd'
import { EditOutlined } from '@ant-design/icons'
import dayjs from 'dayjs'
import api from '../utils/api'
import { useAuth } from '../contexts/AuthContext'

const ENTRY_STATUS_OPTIONS = [
  { label: '接受入场', value: '接受入场' },
  { label: '放弃入场', value: '放弃入场' },
]

const EntryManagement = () => {
  const { user } = useAuth()
  const [loading, setLoading] = useState(false)
  const [saving, setSaving] = useState(false)
  const [rows, setRows] = useState([])
  const [editingRecord, setEditingRecord] = useState(null)
  const [form] = Form.useForm()

  const fetchList = useCallback(async () => {
    setLoading(true)
    try {
      const res = await api.get('/api/entry-management/list', {
        params: {
          viewerId: String(user?.userId || ''),
          viewerRole: user?.position || user?.role || '',
        },
      })
      if (res.data?.returnCode === 'SUC0000') {
        setRows(res.data.body || [])
      } else {
        message.error(res.data?.errorMsg || '获取入场列表失败')
      }
    } catch (_error) {
      message.error('获取入场列表失败')
    } finally {
      setLoading(false)
    }
  }, [user])

  useEffect(() => {
    fetchList()
  }, [fetchList])

  const openEdit = useCallback((record) => {
    setEditingRecord(record)
    form.setFieldsValue({
      entryStatus: record.entryStatus || undefined,
      plannedEntryDate: record.plannedEntryDate ? dayjs(record.plannedEntryDate) : undefined,
      actualEntryDate: record.actualEntryDate ? dayjs(record.actualEntryDate) : undefined,
      arrivalStatus: record.arrivalStatus || '',
    })
  }, [form])

  const closeEdit = useCallback(() => {
    setEditingRecord(null)
    form.resetFields()
  }, [form])

  const handleSave = useCallback(async () => {
    if (!editingRecord) {
      return
    }
    const values = await form.validateFields()
    setSaving(true)
    try {
      const res = await api.put(`/api/entry-management/${editingRecord.entryRecordId}`, {
        entryStatus: values.entryStatus || '',
        plannedEntryDate: values.plannedEntryDate ? values.plannedEntryDate.format('YYYY-MM-DD') : '',
        actualEntryDate: values.actualEntryDate ? values.actualEntryDate.format('YYYY-MM-DD') : '',
        arrivalStatus: values.arrivalStatus || '',
        operatorUserId: String(user?.userId || ''),
        operatorUserName: user?.realName || '系统用户',
        operatorRole: user?.position || user?.role || '',
      })
      if (res.data?.returnCode === 'SUC0000') {
        message.success('更新成功')
        closeEdit()
        await fetchList()
      } else {
        message.error(res.data?.errorMsg || '更新入场信息失败')
      }
    } catch (_error) {
      message.error('更新入场信息失败')
    } finally {
      setSaving(false)
    }
  }, [closeEdit, editingRecord, fetchList, form, user])

  const columns = useMemo(() => ([
    { title: '候选人', dataIndex: 'candidateName', key: 'candidateName', width: 140 },
    {
      title: '面试时间',
      dataIndex: 'interviewTime',
      key: 'interviewTime',
      width: 180,
      render: (value) => (value ? dayjs(value).format('YYYY-MM-DD HH:mm:ss') : '-'),
    },
    { title: '录用室组', dataIndex: 'hiredDepartment', key: 'hiredDepartment', width: 200 },
    { title: '职位级别', dataIndex: 'positionLevel', key: 'positionLevel', width: 120 },
    { title: '技术平台', dataIndex: 'technicalPlatform', key: 'technicalPlatform', width: 140 },
    {
      title: '入场状态',
      dataIndex: 'entryStatus',
      key: 'entryStatus',
      width: 140,
      render: (value) => {
        if (value === '接受入场') return <Tag color="success">{value}</Tag>
        if (value === '放弃入场') return <Tag color="error">{value}</Tag>
        return '-'
      },
    },
    {
      title: '拟到岗时间',
      dataIndex: 'plannedEntryDate',
      key: 'plannedEntryDate',
      width: 140,
      render: (value) => (value ? dayjs(value).format('YYYY-MM-DD') : '-'),
    },
    {
      title: '实际到岗时间',
      dataIndex: 'actualEntryDate',
      key: 'actualEntryDate',
      width: 140,
      render: (value) => (value ? dayjs(value).format('YYYY-MM-DD') : '-'),
    },
    { title: '到岗情况', dataIndex: 'arrivalStatus', key: 'arrivalStatus', width: 200, ellipsis: true, render: (value) => value || '-' },
    {
      title: '操作',
      key: 'action',
      width: 120,
      fixed: 'right',
      render: (_, record) => (
        <Button type="link" icon={<EditOutlined />} onClick={() => openEdit(record)}>
          编辑
        </Button>
      ),
    },
  ]), [openEdit])

  return (
    <div className="app-page">
      <Card title="入场管理">
        <div className="app-table-wrap">
          <Table
            rowKey="entryRecordId"
            loading={loading}
            dataSource={rows}
            columns={columns}
            scroll={{ x: 1500 }}
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
        title="编辑入场信息"
        open={Boolean(editingRecord)}
        onCancel={closeEdit}
        onOk={handleSave}
        confirmLoading={saving}
      >
        <Form form={form} layout="vertical">
          <Form.Item name="entryStatus" label="入场状态">
            <Select allowClear placeholder="请选择入场状态" options={ENTRY_STATUS_OPTIONS} />
          </Form.Item>
          <Form.Item name="plannedEntryDate" label="拟到岗时间">
            <DatePicker style={{ width: '100%' }} />
          </Form.Item>
          <Form.Item name="actualEntryDate" label="实际到岗时间">
            <DatePicker style={{ width: '100%' }} />
          </Form.Item>
          <Form.Item name="arrivalStatus" label="到岗情况">
            <Input.TextArea rows={3} maxLength={200} placeholder="请输入到岗情况" />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  )
}

export default EntryManagement

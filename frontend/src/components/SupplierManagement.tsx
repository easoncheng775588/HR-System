import React, { useEffect, useState } from 'react';
import { Button, Form, Input, message, Modal, Popconfirm, Select, Space, Table, Tag } from 'antd';
import { DeleteOutlined, EditOutlined, PlusOutlined } from '@ant-design/icons';
import api from '../utils/api';

interface UserItem {
  userId: string;
  username?: string;
  realName?: string;
}

interface SupplierItem {
  supplierId: number;
  supplierName: string;
  status: 'ACTIVE' | 'PAUSED' | 'INVALID' | string;
  userIds?: string[];
  hrNames?: string;
  createUserName?: string;
  createTime?: string;
  updateUserName?: string;
  updateTime?: string;
}

interface SubmitError {
  errorFields?: unknown;
  message?: string;
}

const getErrorMessage = (error: unknown): string => {
  if (typeof error === 'object' && error && 'message' in error) {
    return String((error as { message?: string }).message || '未知错误');
  }
  return '未知错误';
};

const SupplierManagement = () => {
  const [loading, setLoading] = useState(false);
  const [submitLoading, setSubmitLoading] = useState(false);
  const [modalVisible, setModalVisible] = useState(false);
  const [suppliers, setSuppliers] = useState<SupplierItem[]>([]);
  const [users, setUsers] = useState<UserItem[]>([]);
  const [editingSupplier, setEditingSupplier] = useState<SupplierItem | null>(null);
  const [form] = Form.useForm();

  useEffect(() => {
    fetchSuppliers();
    fetchUsers();
  }, []);

  const fetchSuppliers = async () => {
    setLoading(true);
    try {
      const response = await api.get('/api/suppliers');
      if (response.data?.returnCode === 'SUC0000') {
        setSuppliers(response.data.body || []);
      } else {
        message.error(response.data?.errorMsg || '获取供应商列表失败');
      }
    } catch (error: unknown) {
      message.error(`获取供应商列表失败: ${getErrorMessage(error)}`);
    } finally {
      setLoading(false);
    }
  };

  const fetchUsers = async () => {
    try {
      const response = await api.get('/api/users');
      if (response.data?.returnCode === 'SUC0000') {
        setUsers(response.data.body || []);
      } else {
        message.error(response.data?.errorMsg || '获取用户列表失败');
      }
    } catch (error: unknown) {
      message.error(`获取用户列表失败: ${getErrorMessage(error)}`);
    }
  };

  const openCreateModal = () => {
    setEditingSupplier(null);
    form.resetFields();
    form.setFieldsValue({
      status: 'ACTIVE',
      userIds: [],
    });
    setModalVisible(true);
  };

  const openEditModal = async (record: SupplierItem) => {
    try {
      const response = await api.get(`/api/suppliers/${record.supplierId}`);
      if (response.data?.returnCode !== 'SUC0000') {
        message.error(response.data?.errorMsg || '获取供应商详情失败');
        return;
      }

      const detail = response.data.body as SupplierItem;
      setEditingSupplier(record);
      form.setFieldsValue({
        supplierName: detail.supplierName,
        status: detail.status,
        userIds: detail.userIds || [],
      });
      setModalVisible(true);
    } catch (error: unknown) {
      message.error(`获取供应商详情失败: ${getErrorMessage(error)}`);
    }
  };

  const handleDelete = async (supplierId: number) => {
    try {
      const response = await api.delete(`/api/suppliers/${supplierId}`);
      if (response.data?.returnCode === 'SUC0000') {
        message.success('删除成功');
        fetchSuppliers();
      } else {
        message.error(response.data?.errorMsg || '删除失败');
      }
    } catch (error: unknown) {
      message.error(`删除失败: ${getErrorMessage(error)}`);
    }
  };

  const handleSubmit = async () => {
    try {
      const values = await form.validateFields();
      const payload = {
        supplierName: values.supplierName,
        status: values.status,
        userIds: values.userIds || [],
      };

      setSubmitLoading(true);
      const response = editingSupplier
        ? await api.put(`/api/suppliers/${editingSupplier.supplierId}`, payload)
        : await api.post('/api/suppliers', payload);

      if (response.data?.returnCode === 'SUC0000') {
        message.success(editingSupplier ? '供应商更新成功' : '供应商创建成功');
        setModalVisible(false);
        form.resetFields();
        fetchSuppliers();
      } else {
        message.error(response.data?.errorMsg || '操作失败');
      }
    } catch (error: unknown) {
      const submitError = error as SubmitError;
      if (submitError?.errorFields) return;
      message.error(`操作失败: ${getErrorMessage(error)}`);
    } finally {
      setSubmitLoading(false);
    }
  };

  const userOptions = users.map((user: UserItem) => ({
    value: user.userId,
    label: `${user.realName || user.username} (${user.userId})`,
  }));

  const statusText = (status: string) => {
    if (status === 'ACTIVE') return <Tag color="success">有效</Tag>;
    if (status === 'PAUSED') return <Tag color="warning">暂停</Tag>;
    if (status === 'INVALID') return <Tag color="error">失效</Tag>;
    return <Tag>{status || '-'}</Tag>;
  };

  const columns = [
    {
      title: '供应商名称',
      dataIndex: 'supplierName',
      key: 'supplierName',
      width: 220,
    },
    {
      title: '供应商HR人员',
      dataIndex: 'hrNames',
      key: 'hrNames',
      width: 320,
      render: (value: string) => value || '-',
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      width: 100,
      render: (value: string) => statusText(value),
    },
    {
      title: '创建人',
      dataIndex: 'createUserName',
      key: 'createUserName',
      width: 120,
      responsive: ['xl' as const],
      render: (value: string) => value || '-',
    },
    {
      title: '创建时间',
      dataIndex: 'createTime',
      key: 'createTime',
      width: 180,
      responsive: ['xxl' as const],
      render: (value: string) => (value ? new Date(value).toLocaleString('zh-CN') : '-'),
    },
    {
      title: '修改人',
      dataIndex: 'updateUserName',
      key: 'updateUserName',
      width: 120,
      responsive: ['xl' as const],
      render: (value: string) => value || '-',
    },
    {
      title: '修改时间',
      dataIndex: 'updateTime',
      key: 'updateTime',
      width: 180,
      responsive: ['xxl' as const],
      render: (value: string) => (value ? new Date(value).toLocaleString('zh-CN') : '-'),
    },
    {
      title: '操作',
      key: 'action',
      width: 150,
      fixed: 'right' as const,
      render: (_value: unknown, record: SupplierItem) => (
        <Space size="small">
          <Button type="link" icon={<EditOutlined />} onClick={() => openEditModal(record)}>
            编辑
          </Button>
          <Popconfirm
            title="确认删除该供应商吗？"
            onConfirm={() => handleDelete(record.supplierId)}
            okText="确认"
            cancelText="取消"
          >
            <Button type="link" danger icon={<DeleteOutlined />}>
              删除
            </Button>
          </Popconfirm>
        </Space>
      ),
    },
  ];

  return (
    <div style={{ width: '100%' }}>
      <div style={{ marginBottom: 16 }}>
        <Button type="primary" icon={<PlusOutlined />} onClick={openCreateModal}>
          新增供应商
        </Button>
      </div>

      <div className="app-table-wrap">
        <Table
          rowKey="supplierId"
          loading={loading}
          dataSource={suppliers}
          columns={columns}
          scroll={{ x: 1300 }}
          pagination={{
            defaultPageSize: 10,
            showSizeChanger: true,
            pageSizeOptions: [10, 20, 30, 50, 100],
            showTotal: (total) => `共 ${total} 条`,
          }}
        />
      </div>

      <Modal
        title={editingSupplier ? '编辑供应商' : '新增供应商'}
        open={modalVisible}
        onOk={handleSubmit}
        confirmLoading={submitLoading}
        onCancel={() => {
          setModalVisible(false);
          form.resetFields();
        }}
        okText="确认"
        cancelText="取消"
        width={640}
      >
        <Form form={form} layout="vertical">
          <Form.Item
            label="供应商名称"
            name="supplierName"
            rules={[
              { required: true, message: '请输入供应商名称' },
              { max: 100, message: '供应商名称最多100个字符' },
            ]}
          >
            <Input placeholder="请输入供应商名称" />
          </Form.Item>

          <Form.Item label="供应商HR人员" name="userIds">
            <Select
              mode="multiple"
              allowClear
              placeholder="请选择供应商HR人员"
              options={userOptions}
              showSearch
              optionFilterProp="label"
            />
          </Form.Item>

          <Form.Item label="状态" name="status" initialValue="ACTIVE" rules={[{ required: true, message: '请选择状态' }]}>
            <Select
              options={[
                { value: 'ACTIVE', label: '有效' },
                { value: 'PAUSED', label: '暂停' },
                { value: 'INVALID', label: '失效' },
              ]}
            />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
};

export default SupplierManagement;

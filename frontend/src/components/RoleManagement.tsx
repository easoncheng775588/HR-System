import React, { useEffect, useState } from 'react';
import { Button, Form, Input, message, Modal, Popconfirm, Select, Space, Table, Tag, Tooltip } from 'antd';
import { DeleteOutlined, EditOutlined, PlusOutlined } from '@ant-design/icons';
import api from '../utils/api';

interface UserItem {
  userId: string;
  username?: string;
  realName?: string;
}

interface RoleItem {
  roleId: number;
  roleName: string;
  description?: string;
  status?: string;
  userIds?: string[];
  memberNames?: string;
  memberCount?: number;
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

const RoleManagement = () => {
  const [loading, setLoading] = useState(false);
  const [submitLoading, setSubmitLoading] = useState(false);
  const [modalVisible, setModalVisible] = useState(false);
  const [roles, setRoles] = useState<RoleItem[]>([]);
  const [users, setUsers] = useState<UserItem[]>([]);
  const [editingRole, setEditingRole] = useState<RoleItem | null>(null);
  const [form] = Form.useForm();

  useEffect(() => {
    fetchRoles();
    fetchUsers();
  }, []);

  const fetchRoles = async () => {
    setLoading(true);
    try {
      const response = await api.get('/api/roles');
      if (response.data?.returnCode === 'SUC0000') {
        setRoles(response.data.body || []);
      } else {
        message.error(response.data?.errorMsg || '获取角色列表失败');
      }
    } catch (error: unknown) {
      message.error(`获取角色列表失败: ${getErrorMessage(error)}`);
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
    setEditingRole(null);
    form.resetFields();
    form.setFieldsValue({
      status: 'ACTIVE',
      userIds: [],
    });
    setModalVisible(true);
  };

  const openEditModal = async (record: RoleItem) => {
    try {
      const response = await api.get(`/api/roles/${record.roleId}`);
      if (response.data?.returnCode !== 'SUC0000') {
        message.error(response.data?.errorMsg || '获取角色详情失败');
        return;
      }

      const roleDetail = response.data.body as RoleItem;
      setEditingRole(record);
      form.setFieldsValue({
        roleName: roleDetail.roleName,
        description: roleDetail.description,
        status: roleDetail.status || 'ACTIVE',
        userIds: roleDetail.userIds || [],
      });
      setModalVisible(true);
    } catch (error: unknown) {
      message.error(`获取角色详情失败: ${getErrorMessage(error)}`);
    }
  };

  const handleDelete = async (roleId: number) => {
    try {
      const response = await api.delete(`/api/roles/${roleId}`);
      if (response.data?.returnCode === 'SUC0000') {
        message.success('删除成功');
        fetchRoles();
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
        roleName: values.roleName,
        description: values.description,
        status: values.status,
        userIds: values.userIds || [],
      };

      setSubmitLoading(true);
      const response = editingRole
        ? await api.put(`/api/roles/${editingRole.roleId}`, payload)
        : await api.post('/api/roles', payload);

      if (response.data?.returnCode === 'SUC0000') {
        message.success(editingRole ? '角色更新成功' : '角色创建成功');
        setModalVisible(false);
        form.resetFields();
        fetchRoles();
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

  const columns = [
    {
      title: '角色名称',
      dataIndex: 'roleName',
      key: 'roleName',
      width: 160,
      fixed: 'left' as const,
    },
    {
      title: '人员',
      dataIndex: 'memberNames',
      key: 'memberNames',
      width: 260,
      render: (value: string) => {
        if (!value) return '-';
        return (
          <Space size={[4, 4]} wrap>
            {value.split(',').map((name) => (
              <Tag key={name.trim()}>{name.trim()}</Tag>
            ))}
          </Space>
        );
      },
    },
    {
      title: '人数',
      dataIndex: 'memberCount',
      key: 'memberCount',
      width: 90,
      render: (value: number) => value || 0,
    },
    {
      title: '备注',
      dataIndex: 'description',
      key: 'description',
      width: 360,
      ellipsis: { showTitle: false },
      render: (text: string) => (
        <Tooltip title={text || '-'} placement="topLeft">
          <span>{text || '-'}</span>
        </Tooltip>
      ),
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
      width: 170,
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
      width: 170,
      responsive: ['xxl' as const],
      render: (value: string) => (value ? new Date(value).toLocaleString('zh-CN') : '-'),
    },
    {
      title: '操作',
      key: 'action',
      width: 150,
      fixed: 'right' as const,
      render: (_value: unknown, record: RoleItem) => (
        <Space size="small">
          <Button type="link" icon={<EditOutlined />} onClick={() => openEditModal(record)}>
            编辑
          </Button>
          <Popconfirm
            title="确认删除该角色吗？"
            onConfirm={() => handleDelete(record.roleId)}
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
          新增角色
        </Button>
      </div>

      <div className="app-table-wrap">
        <Table
          rowKey="roleId"
          loading={loading}
          dataSource={roles}
          columns={columns}
          scroll={{ x: 1280 }}
          pagination={{
            defaultPageSize: 10,
            showSizeChanger: true,
            pageSizeOptions: [10, 20, 30, 50, 100],
            showTotal: (total) => `共 ${total} 条`,
          }}
        />
      </div>

      <Modal
        title={editingRole ? '编辑角色' : '新增角色'}
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
            label="角色名称"
            name="roleName"
            rules={[
              { required: true, message: '请输入角色名称' },
              { max: 50, message: '角色名称最多50个字符' },
            ]}
          >
            <Input placeholder="请输入角色名称" />
          </Form.Item>

          <Form.Item label="人员" name="userIds">
            <Select
              mode="multiple"
              allowClear
              placeholder="请选择角色成员"
              options={userOptions}
              showSearch
              optionFilterProp="label"
            />
          </Form.Item>

          <Form.Item label="备注" name="description" rules={[{ max: 255, message: '备注最多255个字符' }]}>
            <Input.TextArea rows={4} placeholder="请输入备注" />
          </Form.Item>

          <Form.Item label="状态" name="status" initialValue="ACTIVE">
            <Select
              options={[
                { value: 'ACTIVE', label: '启用' },
                { value: 'DISABLED', label: '禁用' },
              ]}
            />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
};

export default RoleManagement;

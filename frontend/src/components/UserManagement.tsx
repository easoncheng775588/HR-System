import React, { useEffect, useMemo, useState } from 'react';
import { Table, Button, Modal, Form, Input, Select, message, Space, Popconfirm } from 'antd';
import { PlusOutlined, EditOutlined, DeleteOutlined } from '@ant-design/icons';
import api from '../utils/api';
import {
  getGroupOptions,
  getTeamOptions,
  normalizeDepartmentPayload,
  shouldRequireGroup,
  validateDepartmentSelection,
} from './userManagementDepartment';

const { Option } = Select;

const resolveDepartmentFieldError = (errorMessage) =>
  errorMessage === '团队名称不能为空' || errorMessage === '团队名称不存在' ? 'teamName' : 'groupName';

const UserManagement = () => {
  const [data, setData] = useState([]);
  const [loading, setLoading] = useState(false);
  const [modalVisible, setModalVisible] = useState(false);
  const [editingUser, setEditingUser] = useState(null);
  const [detailLoading, setDetailLoading] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [isMobile, setIsMobile] = useState(window.innerWidth < 768);
  const [positions, setPositions] = useState([]);
  const [positionsLoading, setPositionsLoading] = useState(false);
  const [departmentOptions, setDepartmentOptions] = useState([]);
  const [departmentLoading, setDepartmentLoading] = useState(false);
  const [form] = Form.useForm();

  const selectedTeamName = Form.useWatch('teamName', form);
  const groupRequired = shouldRequireGroup(departmentOptions, selectedTeamName);

  const teamOptions = useMemo(() => getTeamOptions(departmentOptions), [departmentOptions]);
  const groupOptions = useMemo(
    () => getGroupOptions(departmentOptions, selectedTeamName),
    [departmentOptions, selectedTeamName],
  );

  useEffect(() => {
    const handleResize = () => setIsMobile(window.innerWidth < 768);
    window.addEventListener('resize', handleResize);
    fetchUsers();
    fetchPositions();
    fetchDepartmentOptions();
    return () => window.removeEventListener('resize', handleResize);
  }, []);

  const fetchUsers = async () => {
    setLoading(true);
    try {
      const response = await api.get('/api/users');
      if (response.data?.returnCode === 'SUC0000') {
        setData(response.data.body || []);
      } else {
        message.error(response.data ? response.data.errorMsg : '获取用户列表失败');
      }
    } catch (error) {
      message.error(`获取用户列表失败：${error.message}`);
    } finally {
      setLoading(false);
    }
  };

  const fetchPositions = async () => {
    setPositionsLoading(true);
    try {
      const response = await api.get('/api/sys/params/active/type/POSITION');
      if (response.data?.returnCode === 'SUC0000') {
        setPositions(response.data.body || []);
      } else {
        message.error(response.data ? response.data.errorMsg : '获取岗位列表失败');
        setPositions([]);
      }
    } catch (error) {
      message.error(`获取岗位列表失败：${error.message}`);
      setPositions([]);
    } finally {
      setPositionsLoading(false);
    }
  };

  const fetchDepartmentOptions = async () => {
    setDepartmentLoading(true);
    try {
      const response = await api.get('/api/users/department-options');
      if (response.data?.returnCode === 'SUC0000') {
        setDepartmentOptions(response.data.body?.teamOptions || []);
      } else {
        message.error(response.data ? response.data.errorMsg : '获取部门选项失败');
        setDepartmentOptions([]);
      }
    } catch (error) {
      message.error(`获取部门选项失败：${error.message}`);
      setDepartmentOptions([]);
    } finally {
      setDepartmentLoading(false);
    }
  };

  const handleAdd = async () => {
    setEditingUser(null);
    form.resetFields();
    form.setFieldsValue({ status: 'ACTIVE', groupName: undefined, teamName: undefined });
    await fetchDepartmentOptions();
    setModalVisible(true);
  };

  const handleEdit = async (record) => {
    setEditingUser(record);
    setDetailLoading(true);
    setModalVisible(true);
    await fetchDepartmentOptions();
    try {
      const response = await api.get(`/api/users/${record.userId}`);
      if (response.data?.returnCode === 'SUC0000' && response.data.body) {
        const userDetail = response.data.body;
        form.setFieldsValue({
          ...userDetail,
          password: undefined,
          groupName: userDetail.groupName || undefined,
          teamName: userDetail.teamName || undefined,
        });
      } else {
        message.error(response.data ? response.data.errorMsg : '获取用户详情失败');
        setModalVisible(false);
      }
    } catch (error) {
      message.error(`获取用户详情失败：${error.message}`);
      setModalVisible(false);
    } finally {
      setDetailLoading(false);
    }
  };

  const handleDelete = async (userId) => {
    try {
      const response = await api.delete(`/api/users/${userId}`);
      if (response.data?.returnCode === 'SUC0000') {
        message.success('删除成功');
        fetchUsers();
      } else {
        message.error(response.data ? response.data.errorMsg : '删除失败');
      }
    } catch (error) {
      message.error(`删除失败：${error.message}`);
    }
  };

  const handleModalOk = async () => {
    try {
      const values = await form.validateFields();
      const departmentError = validateDepartmentSelection({
        teamName: values.teamName,
        groupName: values.groupName,
        departmentOptions,
      });

      if (departmentError) {
        form.setFields([
          {
            name: resolveDepartmentFieldError(departmentError),
            errors: [departmentError],
          },
        ]);
        return;
      }

      const normalizedPayload = normalizeDepartmentPayload(values, departmentOptions);
      if (editingUser && !normalizedPayload.password) {
        delete normalizedPayload.password;
      }

      setSubmitting(true);
      const url = editingUser ? `/api/users/${editingUser.userId}` : '/api/users';
      const method = editingUser ? 'put' : 'post';
      const response = await api[method](url, normalizedPayload);

      if (response.data?.returnCode === 'SUC0000') {
        message.success(editingUser ? '更新成功' : '创建成功');
        setModalVisible(false);
        form.resetFields();
        fetchUsers();
      } else {
        message.error(response.data ? response.data.errorMsg : '操作失败');
      }
    } catch (error) {
      if (error?.errorFields) return;
      if (error.response) {
        message.error(`操作失败：${error.response.data?.errorMsg || error.message}`);
      } else {
        message.error(`操作失败：${error.message}`);
      }
    } finally {
      setSubmitting(false);
    }
  };

  const handleModalCancel = () => {
    setModalVisible(false);
    setEditingUser(null);
    form.resetFields();
  };

  const handleTeamChange = () => {
    form.setFieldsValue({ groupName: undefined });
    form.setFields([
      { name: 'teamName', errors: [] },
      { name: 'groupName', errors: [] },
    ]);
  };

  const columns = [
    {
      title: '用户ID',
      dataIndex: 'userId',
      key: 'userId',
      width: isMobile ? 80 : 100,
    },
    {
      title: '用户名',
      dataIndex: 'username',
      key: 'username',
      width: isMobile ? 100 : 120,
    },
    {
      title: '真实姓名',
      dataIndex: 'realName',
      key: 'realName',
      width: isMobile ? 100 : 120,
    },
    {
      title: '部门',
      dataIndex: 'departmentDisplay',
      key: 'departmentDisplay',
      width: isMobile ? 180 : 240,
      ellipsis: true,
      render: (text) => text || '-',
    },
    {
      title: '邮箱',
      dataIndex: 'email',
      key: 'email',
      width: isMobile ? 150 : 180,
      ellipsis: true,
      responsive: ['md', 'lg', 'xl', 'xxl'],
    },
    {
      title: '电话',
      dataIndex: 'phone',
      key: 'phone',
      width: isMobile ? 100 : 130,
      responsive: ['md', 'lg', 'xl', 'xxl'],
    },
    {
      title: '岗位',
      dataIndex: 'position',
      key: 'position',
      width: isMobile ? 100 : 120,
      responsive: ['lg', 'xl', 'xxl'],
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      width: isMobile ? 80 : 100,
      render: (status) => (
        <span style={{ color: status === 'ACTIVE' ? '#52c41a' : '#ff4d4f', fontSize: isMobile ? 12 : 14 }}>
          {status === 'ACTIVE' ? '启用' : '禁用'}
        </span>
      ),
    },
    {
      title: '创建时间',
      dataIndex: 'createTime',
      key: 'createTime',
      width: isMobile ? 140 : 180,
      render: (text) => (text ? new Date(text).toLocaleString('zh-CN') : '-'),
      responsive: ['md', 'lg', 'xl', 'xxl'],
    },
    {
      title: '操作',
      key: 'action',
      width: isMobile ? 100 : 150,
      fixed: 'right',
      render: (_, record) => (
        <Space size="small">
          <Button
            type="link"
            icon={<EditOutlined />}
            onClick={() => handleEdit(record)}
            size={isMobile ? 'small' : 'middle'}
          >
            编辑
          </Button>
          <Popconfirm
            title="确定要删除该用户吗？"
            onConfirm={() => handleDelete(record.userId)}
            okText="确定"
            cancelText="取消"
          >
            <Button
              type="link"
              danger
              icon={<DeleteOutlined />}
              size={isMobile ? 'small' : 'middle'}
            >
              删除
            </Button>
          </Popconfirm>
        </Space>
      ),
    },
  ];

  return (
    <div style={{ maxWidth: '100%', overflow: 'hidden' }}>
      <div style={{ marginBottom: isMobile ? 12 : 16 }}>
        <Button
          type="primary"
          icon={<PlusOutlined />}
          onClick={handleAdd}
          style={{ background: '#1890ff', borderColor: '#1890ff' }}
          size={isMobile ? 'small' : 'middle'}
        >
          新增用户
        </Button>
      </div>

      <div style={{ overflow: 'auto' }}>
        <Table
          dataSource={data}
          loading={loading}
          rowKey="userId"
          columns={columns}
          scroll={{ x: isMobile ? 980 : 1600 }}
          pagination={{
            defaultPageSize: 10,
            showSizeChanger: true,
            pageSizeOptions: [10, 20, 30, 50, 100],
            showTotal: (total) => `共 ${total} 条记录`,
            simple: isMobile,
          }}
          size={isMobile ? 'small' : 'middle'}
        />
      </div>

      <Modal
        title={editingUser ? '编辑用户' : '新增用户'}
        open={modalVisible}
        onOk={handleModalOk}
        onCancel={handleModalCancel}
        width={isMobile ? '95%' : 600}
        okText="确定"
        cancelText="取消"
        confirmLoading={submitting}
        okButtonProps={{ style: { background: '#1890ff', borderColor: '#1890ff' } }}
        destroyOnHidden
      >
        <Form
          form={form}
          layout="vertical"
          initialValues={{
            status: 'ACTIVE',
          }}
        >
          <Form.Item
            label="用户ID"
            name="userId"
            rules={[
              { required: true, message: '请输入用户ID' },
              { max: 20, message: '用户ID最多20个字符' },
            ]}
          >
            <Input placeholder="请输入用户ID" disabled={!!editingUser} size={isMobile ? 'small' : 'middle'} />
          </Form.Item>

          <Form.Item
            label="用户名"
            name="username"
            rules={[
              { required: true, message: '请输入用户名' },
              { max: 50, message: '用户名最多50个字符' },
            ]}
          >
            <Input placeholder="请输入用户名" size={isMobile ? 'small' : 'middle'} />
          </Form.Item>

          <Form.Item
            label="密码"
            name="password"
            rules={editingUser ? [] : [
              { required: true, message: '请输入密码' },
              { min: 6, message: '密码至少6个字符' },
            ]}
          >
            <Input.Password placeholder={editingUser ? '留空则不修改密码' : '请输入密码'} size={isMobile ? 'small' : 'middle'} />
          </Form.Item>

          <Form.Item
            label="真实姓名"
            name="realName"
            rules={[
              { required: true, message: '请输入真实姓名' },
              { max: 50, message: '真实姓名最多50个字符' },
            ]}
          >
            <Input placeholder="请输入真实姓名" size={isMobile ? 'small' : 'middle'} />
          </Form.Item>

          <Form.Item
            label="邮箱"
            name="email"
            rules={[
              { type: 'email', message: '请输入有效的邮箱地址' },
              { max: 100, message: '邮箱最多100个字符' },
            ]}
          >
            <Input placeholder="请输入邮箱" size={isMobile ? 'small' : 'middle'} />
          </Form.Item>

          <Form.Item
            label="电话"
            name="phone"
            rules={[{ max: 20, message: '电话最多20个字符' }]}
          >
            <Input placeholder="请输入电话" size={isMobile ? 'small' : 'middle'} />
          </Form.Item>

          <Form.Item
            label="团队名称"
            name="teamName"
            rules={[
              { required: true, message: '团队名称不能为空' },
              {
                validator: (_, value) => {
                  if (!value) return Promise.resolve();
                  const errorMessage = validateDepartmentSelection({
                    teamName: value,
                    groupName: form.getFieldValue('groupName'),
                    departmentOptions,
                  });
                  return errorMessage === null || errorMessage === '所选团队要求必须填写室组名称'
                    ? Promise.resolve()
                    : Promise.reject(new Error(errorMessage));
                },
              },
            ]}
          >
            <Select
              placeholder="请选择团队名称"
              size={isMobile ? 'small' : 'middle'}
              loading={departmentLoading}
              options={teamOptions}
              onChange={handleTeamChange}
            />
          </Form.Item>

          <Form.Item
            label="室组名称"
            name="groupName"
            rules={[
              {
                validator: (_, value) => {
                  const errorMessage = validateDepartmentSelection({
                    teamName: form.getFieldValue('teamName'),
                    groupName: value,
                    departmentOptions,
                  });
                  return errorMessage && resolveDepartmentFieldError(errorMessage) === 'groupName'
                    ? Promise.reject(new Error(errorMessage))
                    : Promise.resolve();
                },
              },
            ]}
          >
            <Select
              placeholder={!selectedTeamName ? '请先选择团队名称' : groupRequired ? '请选择室组名称' : '所选团队无需室组'}
              size={isMobile ? 'small' : 'middle'}
              loading={departmentLoading || detailLoading}
              options={groupOptions}
              disabled={!selectedTeamName || !groupRequired}
              allowClear
            />
          </Form.Item>

          <Form.Item
            label="岗位"
            name="position"
            rules={[{ max: 100, message: '岗位最多100个字符' }]}
          >
            <Select
              placeholder="请选择岗位"
              size={isMobile ? 'small' : 'middle'}
              allowClear
              loading={positionsLoading}
            >
              {positions.map((position) => (
                <Option key={position.paramValue} value={position.paramValue}>
                  {position.paramName}
                </Option>
              ))}
            </Select>
          </Form.Item>

          <Form.Item
            label="状态"
            name="status"
            rules={[{ required: true, message: '请选择状态' }]}
          >
            <Select placeholder="请选择状态" size={isMobile ? 'small' : 'middle'}>
              <Option value="ACTIVE">启用</Option>
              <Option value="DISABLED">禁用</Option>
            </Select>
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
};

export default UserManagement;

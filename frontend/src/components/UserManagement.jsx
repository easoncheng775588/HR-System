import React, { useState, useEffect } from 'react';
import { Table, Button, Modal, Form, Input, Select, message, Space, Popconfirm } from 'antd';
import { PlusOutlined, EditOutlined, DeleteOutlined } from '@ant-design/icons';
import api from '../utils/api';

const { Option } = Select;

const UserManagement = () => {
  const [data, setData] = useState([]);
  const [loading, setLoading] = useState(false);
  const [modalVisible, setModalVisible] = useState(false);
  const [editingUser, setEditingUser] = useState(null);
  const [isMobile, setIsMobile] = useState(false);
  const [form] = Form.useForm();

  useEffect(() => {
    fetchUsers();
    handleResize();
    window.addEventListener('resize', handleResize);
    return () => window.removeEventListener('resize', handleResize);
  }, []);

  const handleResize = () => {
    setIsMobile(window.innerWidth < 768);
  };

  const fetchUsers = async () => {
    setLoading(true);
    try {
      const response = await api.get('/api/users');
      if (response.data.returnCode === 'SUC0000') {
        setData(response.data.body);
      } else {
        message.error(response.data.errorMsg || '获取用户列表失败');
      }
    } catch (error) {
      message.error('获取用户列表失败：' + error.message);
    } finally {
      setLoading(false);
    }
  };

  const handleAdd = () => {
    setEditingUser(null);
    form.resetFields();
    setModalVisible(true);
  };

  const handleEdit = (record) => {
    setEditingUser(record);
    form.setFieldsValue(record);
    setModalVisible(true);
  };

  const handleDelete = async (userId) => {
    try {
      const response = await api.delete(`/api/users/${userId}`);
      if (response.data.returnCode === 'SUC0000') {
        message.success('删除成功');
        fetchUsers();
      } else {
        message.error(response.data.errorMsg || '删除失败');
      }
    } catch (error) {
      message.error('删除失败：' + error.message);
    }
  };

  const handleModalOk = async () => {
    try {
      const values = await form.validateFields();
      const url = editingUser 
        ? `/api/users/${editingUser.userId}`
        : '/api/users';
      
      const method = editingUser ? 'put' : 'post';
      
      const response = await api[method](url, values);
      
      if (response.data.returnCode === 'SUC0000') {
        message.success(editingUser ? '更新成功' : '创建成功');
        setModalVisible(false);
        fetchUsers();
      } else {
        message.error(response.data.errorMsg || '操作失败');
      }
    } catch (error) {
      if (error.response) {
        message.error('操作失败：' + (error.response.data.errorMsg || error.message));
      } else {
        message.error('操作失败：' + error.message);
      }
    }
  };

  const handleModalCancel = () => {
    setModalVisible(false);
    form.resetFields();
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
      title: '邮箱',
      dataIndex: 'email',
      key: 'email',
      width: isMobile ? 150 : 180,
      ellipsis: true,
      responsive: ['md', 'lg', 'xl', 'xxl']
    },
    {
      title: '电话',
      dataIndex: 'phone',
      key: 'phone',
      width: isMobile ? 100 : 130,
      responsive: ['md', 'lg', 'xl', 'xxl']
    },
    {
      title: '部门',
      dataIndex: 'department',
      key: 'department',
      width: isMobile ? 100 : 120,
      responsive: ['lg', 'xl', 'xxl']
    },
    {
      title: '职位',
      dataIndex: 'position',
      key: 'position',
      width: isMobile ? 100 : 120,
      responsive: ['lg', 'xl', 'xxl']
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
      render: (text) => text ? new Date(text).toLocaleString('zh-CN') : '-',
      responsive: ['md', 'lg', 'xl', 'xxl']
    },
    {
      title: '操作',
      key: 'action',
      width: isMobile ? 100 : 150,
      fixed: 'right',
      render: (text, record) => (
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
          scroll={{ x: isMobile ? 800 : 1400 }}
          pagination={{
            pageSize: isMobile ? 5 : 10,
            showSizeChanger: !isMobile,
            showTotal: (total) => `共 ${total} 条记录`,
            simple: isMobile
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
        okButtonProps={{ style: { background: '#1890ff', borderColor: '#1890ff' } }}
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
            rules={[
              { max: 20, message: '电话最多20个字符' },
            ]}
          >
            <Input placeholder="请输入电话" size={isMobile ? 'small' : 'middle'} />
          </Form.Item>
          
          <Form.Item
            label="部门"
            name="department"
            rules={[
              { max: 100, message: '部门最多100个字符' },
            ]}
          >
            <Select placeholder="请选择部门" size={isMobile ? 'small' : 'middle'} allowClear>
              <Option value="人事部">人事部</Option>
              <Option value="技术部">技术部</Option>
              <Option value="产品部">产品部</Option>
              <Option value="运营部">运营部</Option>
              <Option value="市场部">市场部</Option>
              <Option value="销售部">销售部</Option>
              <Option value="财务部">财务部</Option>
              <Option value="行政部">行政部</Option>
              <Option value="客服部">客服部</Option>
              <Option value="研发部">研发部</Option>
              <Option value="设计部">设计部</Option>
            </Select>
          </Form.Item>
          
          <Form.Item
            label="职位"
            name="position"
            rules={[
              { max: 100, message: '职位最多100个字符' },
            ]}
          >
            <Select placeholder="请选择职位" size={isMobile ? 'small' : 'middle'} allowClear>
              <Option value="系统管理员">系统管理员</Option>
              <Option value="人事经理">人事经理</Option>
              <Option value="人事专员">人事专员</Option>
              <Option value="技术总监">技术总监</Option>
              <Option value="技术经理">技术经理</Option>
              <Option value="高级工程师">高级工程师</Option>
              <Option value="工程师">工程师</Option>
              <Option value="初级工程师">初级工程师</Option>
              <Option value="产品经理">产品经理</Option>
              <Option value="运营经理">运营经理</Option>
              <Option value="运营专员">运营专员</Option>
              <Option value="市场经理">市场经理</Option>
              <Option value="销售经理">销售经理</Option>
              <Option value="销售专员">销售专员</Option>
              <Option value="财务经理">财务经理</Option>
              <Option value="财务专员">财务专员</Option>
              <Option value="行政主管">行政主管</Option>
              <Option value="行政专员">行政专员</Option>
              <Option value="室经理">室经理</Option>
              <Option value="团队经理">团队经理</Option>
              <Option value="分管总">分管总</Option>
            </Select>
          </Form.Item>
          
          <Form.Item
            label="状态"
            name="status"
            rules={[
              { required: true, message: '请选择状态' },
            ]}
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
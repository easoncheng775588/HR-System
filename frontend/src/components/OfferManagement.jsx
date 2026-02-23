import React, { useState, useEffect, useContext } from 'react';
import {
  Table,
  Button,
  Modal,
  Form,
  Input,
  Select,
  DatePicker,
  message,
  Card,
  Tag,
  Space,
  Descriptions,
} from 'antd';
import {
  CheckCircleOutlined,
  UserOutlined,
  MailOutlined,
  PhoneOutlined,
  SolutionOutlined,
  CalendarOutlined,
  SendOutlined,
  EditOutlined,
  EyeOutlined,
} from '@ant-design/icons';
import axios from 'axios';
import dayjs from 'dayjs';
import { useAuth } from '../contexts/AuthContext';

const { Option } = Select;
const { TextArea } = Input;

const OfferManagement = () => {
  const { user } = useAuth();
  const [candidates, setCandidates] = useState([]);
  const [offerRecords, setOfferRecords] = useState([]);
  const [loading, setLoading] = useState(false);
  const [offerModalVisible, setOfferModalVisible] = useState(false);
  const [currentCandidate, setCurrentCandidate] = useState(null);
  const [currentOfferRecord, setCurrentOfferRecord] = useState(null);
  const [recordModalVisible, setRecordModalVisible] = useState(false);
  const [form] = Form.useForm();
  const [isMobile, setIsMobile] = useState(false);

  useEffect(() => {
    const handleResize = () => {
      setIsMobile(window.innerWidth < 768);
    };

    handleResize();
    window.addEventListener('resize', handleResize);
    return () => window.removeEventListener('resize', handleResize);
  }, []);

  // 状态映射
  const statusMap = {
    PENDING: { text: '待处理', color: 'blue' },
    APPROVED: { text: '已批准', color: 'green' },
    REJECTED: { text: '已拒绝', color: 'red' },
  };

  const emailStatusMap = {
    NOT_SENT: { text: '未发送', color: 'default' },
    SENT: { text: '已发送', color: 'green' },
    FAILED: { text: '发送失败', color: 'red' },
  };

  // 获取可录用的候选人
  const fetchEligibleCandidates = async () => {
    setLoading(true);
    try {
      const response = await axios.get('http://localhost:8080/api/offer/eligible-candidates');
      if (response.data.returnCode === 'SUC0000') {
        setCandidates(response.data.body || []);
      } else {
        message.error(response.data.errorMsg || '获取可录用的候选人失败');
      }
    } catch (error) {
      console.error('获取可录用的候选人失败:', error);
      message.error('获取可录用的候选人失败');
    } finally {
      setLoading(false);
    }
  };

  // 获取录用记录
  const fetchOfferRecords = async () => {
    setLoading(true);
    try {
      console.log('开始获取录用记录');
      const response = await axios.get('http://localhost:8080/api/offer/list');
      console.log('获取录用记录响应:', response);
      if (response.data.returnCode === 'SUC0000') {
        console.log('获取录用记录成功，数据:', response.data.body);
        if (response.data.body && response.data.body.length > 0) {
          console.log('第一条录用记录:', response.data.body[0]);
          console.log('offerRecordId:', response.data.body[0].offerRecordId);
          console.log('录用记录对象的所有属性:', Object.keys(response.data.body[0]));
        }
        setOfferRecords(response.data.body || []);
      } else {
        console.log('获取录用记录失败，错误信息:', response.data.errorMsg);
        message.error(response.data.errorMsg || '获取录用记录失败');
      }
    } catch (error) {
      console.error('获取录用记录失败:', error);
      message.error('获取录用记录失败');
    } finally {
      setLoading(false);
    }
  };

  // 发送录用邮件
  const sendOfferEmail = async (offerRecordId) => {
    try {
      console.log('发送录用邮件，offerRecordId类型:', typeof offerRecordId, '值:', offerRecordId);
      if (!offerRecordId || offerRecordId === 'null' || offerRecordId === null) {
        console.log('offerRecordId无效，值为:', offerRecordId);
        message.error('录用记录ID无效');
        return;
      }
      const response = await axios.post(`http://localhost:8080/api/offer/${offerRecordId}/send-email`);
      console.log('发送录用邮件响应:', response.data);
      if (response.data.returnCode === 'SUC0000' && response.data.body) {
        message.success('录用邮件发送成功');
        fetchOfferRecords();
      } else {
        console.log('发送录用邮件失败，响应:', response.data);
        message.error('录用邮件发送失败');
      }
    } catch (error) {
      console.error('发送录用邮件失败:', error);
      message.error('录用邮件发送失败');
    }
  };

  // 处理录用操作
  const handleOffer = (candidate) => {
    setCurrentCandidate(candidate);
    // 设置表单默认值
    form.setFieldsValue({
      candidateName: candidate.candidateName,
      contactPhone: candidate.contactPhone,
      email: candidate.email,
      position: candidate.position,
      entryTime: dayjs(),
    });
    setOfferModalVisible(true);
  };

  // 确认录用
  const handleConfirmOffer = async () => {
    try {
      const values = await form.validateFields();
      
      const offerRecord = {
        resumeId: currentCandidate.resumeId,
        recruitmentRequestId: currentCandidate.recruitmentRequestId,
        candidateName: values.candidateName,
        contactPhone: values.contactPhone,
        email: values.email,
        position: values.position,
        entryTime: values.entryTime.toDate(),
        createUserId: user?.userId || '1001',
        createUserName: user?.fullName || 'admin',
        updateUserId: user?.userId || '1001',
        updateUserName: user?.fullName || 'admin',
      };

      const response = await axios.post('http://localhost:8080/api/offer/save', offerRecord);
      if (response.data.returnCode === 'SUC0000') {
        message.success('录用记录创建成功');
        setOfferModalVisible(false);
        fetchEligibleCandidates();
        fetchOfferRecords();
        
        // 发送录用邮件
        await sendOfferEmail(response.data.body.offerRecordId);
      } else {
        message.error('录用记录创建失败');
      }
    } catch (error) {
      console.error('确认录用失败:', error);
      message.error('确认录用失败');
    }
  };

  // 查看录用记录详情
  const handleViewRecord = (record) => {
    setCurrentOfferRecord(record);
    setRecordModalVisible(true);
  };

  // 候选人列表列配置
  const candidateColumns = [
    {
      title: '候选人姓名',
      dataIndex: 'candidateName',
      key: 'candidateName',
      render: (text, record) => (
        <Space>
          <UserOutlined />
          <span>{text}</span>
        </Space>
      ),
    },
    {
      title: '联系电话',
      dataIndex: 'contactPhone',
      key: 'contactPhone',
      render: (text) => (
        <Space>
          <PhoneOutlined />
          <span>{text}</span>
        </Space>
      ),
    },
    {
      title: '邮箱',
      dataIndex: 'email',
      key: 'email',
      render: (text) => (
        <Space>
          <MailOutlined />
          <span>{text}</span>
        </Space>
      ),
    },
    {
      title: '应聘岗位',
      dataIndex: 'position',
      key: 'position',
      render: (text) => (
        <Space>
          <SolutionOutlined />
          <span>{text}</span>
        </Space>
      ),
    },
    {
        title: '操作',
        key: 'action',
        width: isMobile ? 100 : 150,
        fixed: 'right',
        render: (_, record) => (
        <Space size={isMobile ? 'small' : 'middle'}>
          <Button 
            type="link" 
            icon={<CheckCircleOutlined />} 
            onClick={() => handleOffer(record)}
            style={{ color: '#52c41a' }}
            size={isMobile ? 'small' : 'middle'}
          >
            录用
          </Button>
        </Space>
      ),
    },
  ];

  // 录用记录列表列配置
  const recordColumns = [
    {
      title: '候选人姓名',
      dataIndex: 'candidateName',
      key: 'candidateName',
      render: (text) => (
        <Space>
          <UserOutlined />
          <span>{text}</span>
        </Space>
      ),
    },
    {
      title: '联系电话',
      dataIndex: 'contactPhone',
      key: 'contactPhone',
      render: (text) => (
        <Space>
          <PhoneOutlined />
          <span>{text}</span>
        </Space>
      ),
    },
    {
      title: '邮箱',
      dataIndex: 'email',
      key: 'email',
      render: (text) => (
        <Space>
          <MailOutlined />
          <span>{text}</span>
        </Space>
      ),
    },
    {
      title: '录用岗位',
      dataIndex: 'position',
      key: 'position',
      render: (text) => (
        <Space>
          <SolutionOutlined />
          <span>{text}</span>
        </Space>
      ),
    },
    {
      title: '入职时间',
      dataIndex: 'entryTime',
      key: 'entryTime',
      render: (text) => (
        <Space>
          <CalendarOutlined />
          <span>{text ? dayjs(text).format('YYYY-MM-DD') : '-'}</span>
        </Space>
      ),
    },
    {
      title: '录用状态',
      dataIndex: 'status',
      key: 'status',
      render: (status) => {
        const statusInfo = statusMap[status] || { text: status, color: 'default' };
        return <Tag color={statusInfo.color}>{statusInfo.text}</Tag>;
      },
    },
    {
      title: '邮件状态',
      dataIndex: 'emailStatus',
      key: 'emailStatus',
      render: (emailStatus) => {
        const statusInfo = emailStatusMap[emailStatus] || { text: emailStatus, color: 'default' };
        return <Tag color={statusInfo.color}>{statusInfo.text}</Tag>;
      },
    },
    {
      title: '操作',
      key: 'action',
      width: isMobile ? 180 : 240,
      fixed: 'right',
      render: (_, record) => (
        <Space size={isMobile ? 'small' : 'middle'}>
          <Button 
            type="link" 
            icon={<EyeOutlined />} 
            onClick={() => handleViewRecord(record)}
            size={isMobile ? 'small' : 'middle'}
          >
            查看
          </Button>
          <Button 
            type="link" 
            icon={<SendOutlined />} 
            onClick={() => {
              console.log('点击重新发送通知，record:', record);
              sendOfferEmail(record.offerRecordId);
            }}
            style={{ color: '#52c41a' }}
            size={isMobile ? 'small' : 'middle'}
          >
            重新发送通知
          </Button>
        </Space>
      ),
    },
  ];

  // 组件挂载时获取数据
  useEffect(() => {
    fetchEligibleCandidates();
    fetchOfferRecords();
  }, []);

  return (
    <div className="interview-scheduling">
      <h2>录用管理</h2>
      
      <Card>
        <h3>可录用的候选人（三面通过）</h3>
        <Table
          columns={candidateColumns}
          dataSource={candidates}
          rowKey="resumeId"
          loading={loading}
          pagination={{
            showSizeChanger: true,
            pageSizeOptions: ['10', '20', '50'],
          }}
          size={isMobile ? 'small' : 'middle'}
        />
      </Card>

      <Card style={{ marginTop: 12 }}>
        <h3>录用记录</h3>
        <Table
          columns={recordColumns}
          dataSource={offerRecords}
          rowKey="offerRecordId"
          loading={loading}
          pagination={{
            showSizeChanger: true,
            pageSizeOptions: ['10', '20', '50'],
          }}
          size={isMobile ? 'small' : 'middle'}
        />
      </Card>

      {/* 录用模态框 */}
      <Modal
        title="创建录用记录"
        open={offerModalVisible}
        onOk={handleConfirmOffer}
        onCancel={() => setOfferModalVisible(false)}
        width={600}
      >
        <Form form={form} layout="vertical">
          <Form.Item
            name="candidateName"
            label="候选人姓名"
            rules={[{ required: true, message: '请输入候选人姓名' }]}
          >
            <Input disabled />
          </Form.Item>
          
          <Form.Item
            name="contactPhone"
            label="联系电话"
            rules={[{ required: true, message: '请输入联系电话' }]}
          >
            <Input disabled />
          </Form.Item>
          
          <Form.Item
            name="email"
            label="邮箱"
            rules={[{ required: true, message: '请输入邮箱' }, { type: 'email', message: '请输入有效的邮箱地址' }]}
          >
            <Input disabled />
          </Form.Item>
          
          <Form.Item
            name="position"
            label="录用岗位"
            rules={[{ required: true, message: '请输入录用岗位' }]}
          >
            <Input />
          </Form.Item>
          
          <Form.Item
            name="entryTime"
            label="入职时间"
            rules={[{ required: true, message: '请选择入职时间' }]}
          >
            <DatePicker style={{ width: '100%' }} />
          </Form.Item>
        </Form>
      </Modal>

      {/* 录用记录详情模态框 */}
      <Modal
        title="录用记录详情"
        open={recordModalVisible}
        onCancel={() => setRecordModalVisible(false)}
        footer={[
          <Button key="close" onClick={() => setRecordModalVisible(false)}>
            关闭
          </Button>,
        ]}
        width={600}
      >
        {currentOfferRecord && (
          <Descriptions bordered column={1}>
            <Descriptions.Item label="候选人姓名">{currentOfferRecord.candidateName}</Descriptions.Item>
            <Descriptions.Item label="联系电话">{currentOfferRecord.contactPhone}</Descriptions.Item>
            <Descriptions.Item label="邮箱">{currentOfferRecord.email}</Descriptions.Item>
            <Descriptions.Item label="录用岗位">{currentOfferRecord.position}</Descriptions.Item>
            <Descriptions.Item label="入职时间">
              {currentOfferRecord.entryTime ? dayjs(currentOfferRecord.entryTime).format('YYYY-MM-DD') : '-'}
            </Descriptions.Item>
            <Descriptions.Item label="录用状态">
              {currentOfferRecord.status ? (
                <Tag color={statusMap[currentOfferRecord.status]?.color || 'default'}>
                  {statusMap[currentOfferRecord.status]?.text || currentOfferRecord.status}
                </Tag>
              ) : '-'}
            </Descriptions.Item>
            <Descriptions.Item label="邮件状态">
              {currentOfferRecord.emailStatus ? (
                <Tag color={emailStatusMap[currentOfferRecord.emailStatus]?.color || 'default'}>
                  {emailStatusMap[currentOfferRecord.emailStatus]?.text || currentOfferRecord.emailStatus}
                </Tag>
              ) : '-'}
            </Descriptions.Item>
            <Descriptions.Item label="创建时间">
              {currentOfferRecord.createTime ? dayjs(currentOfferRecord.createTime).format('YYYY-MM-DD HH:mm:ss') : '-'}
            </Descriptions.Item>
            <Descriptions.Item label="创建用户">{currentOfferRecord.createUserName}</Descriptions.Item>
          </Descriptions>
        )}
      </Modal>
    </div>
  );
};

export default OfferManagement;

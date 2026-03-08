import React, { useEffect, useState } from 'react';
import {
  Button,
  Card,
  DatePicker,
  Descriptions,
  Drawer,
  Form,
  Input,
  Modal,
  Space,
  Table,
  Tag,
  message,
} from 'antd';
import {
  CheckCircleOutlined,
  EyeOutlined,
  MailOutlined,
  PhoneOutlined,
  SendOutlined,
  SolutionOutlined,
  UserOutlined,
} from '@ant-design/icons';
import dayjs from 'dayjs';
import api from '../utils/api';
import { useAuth } from '../contexts/AuthContext';

const OfferManagement = () => {
  const { user } = useAuth();
  const [candidates, setCandidates] = useState([]);
  const [offerRecords, setOfferRecords] = useState([]);
  const [loading, setLoading] = useState(false);

  const [offerModalVisible, setOfferModalVisible] = useState(false);
  const [recordDrawerVisible, setRecordDrawerVisible] = useState(false);
  const [currentCandidate, setCurrentCandidate] = useState(null);
  const [currentOfferRecord, setCurrentOfferRecord] = useState(null);
  const [form] = Form.useForm();

  useEffect(() => {
    fetchEligibleCandidates();
    fetchOfferRecords();
  }, []);

  const fetchEligibleCandidates = async () => {
    setLoading(true);
    try {
      const response = await api.get('/api/offer/eligible-candidates');
      if (response.data?.returnCode === 'SUC0000') {
        setCandidates(response.data.body || []);
      } else {
        message.error(response.data?.errorMsg || '获取可录用候选人失败');
      }
    } catch (error) {
      console.error('Fetch eligible candidates failed:', error);
      message.error('获取可录用候选人失败');
    } finally {
      setLoading(false);
    }
  };

  const fetchOfferRecords = async () => {
    setLoading(true);
    try {
      const response = await api.get('/api/offer/list');
      if (response.data?.returnCode === 'SUC0000') {
        setOfferRecords(response.data.body || []);
      } else {
        message.error(response.data?.errorMsg || '获取录用记录失败');
      }
    } catch (error) {
      console.error('Fetch offer records failed:', error);
      message.error('获取录用记录失败');
    } finally {
      setLoading(false);
    }
  };

  const sendOfferEmail = async (offerRecordId) => {
    if (!offerRecordId) {
      message.error('录用记录 ID 无效');
      return;
    }

    try {
      const response = await api.post(`/api/offer/${offerRecordId}/send-email`);
      if (response.data?.returnCode === 'SUC0000') {
        message.success('录用邮件发送成功');
        fetchOfferRecords();
      } else {
        message.error(response.data?.errorMsg || '录用邮件发送失败');
      }
    } catch (error) {
      console.error('Send offer email failed:', error);
      message.error('录用邮件发送失败');
    }
  };

  const handleOffer = (candidate) => {
    setCurrentCandidate(candidate);
    form.setFieldsValue({
      candidateName: candidate.candidateName,
      contactPhone: candidate.contactPhone,
      email: candidate.email,
      position: candidate.position,
      entryTime: dayjs(),
    });
    setOfferModalVisible(true);
  };

  const handleConfirmOffer = async () => {
    try {
      const values = await form.validateFields();
      const payload = {
        resumeId: currentCandidate.resumeId,
        recruitmentRequestId: currentCandidate.recruitmentRequestId,
        candidateName: values.candidateName,
        contactPhone: values.contactPhone,
        email: values.email,
        position: values.position,
        entryTime: values.entryTime.toDate(),
        createUserId: user?.userId || '1001',
        createUserName: user?.realName || '系统用户',
        updateUserId: user?.userId || '1001',
        updateUserName: user?.realName || '系统用户',
      };

      const response = await api.post('/api/offer/save', payload);
      if (response.data?.returnCode === 'SUC0000') {
        message.success('录用记录创建成功');
        setOfferModalVisible(false);
        form.resetFields();
        fetchEligibleCandidates();
        fetchOfferRecords();

        const offerRecordId = response.data?.body?.offerRecordId;
        if (offerRecordId) {
          await sendOfferEmail(offerRecordId);
        }
      } else {
        message.error(response.data?.errorMsg || '录用记录创建失败');
      }
    } catch (error) {
      console.error('Confirm offer failed:', error);
      message.error('确认录用失败');
    }
  };

  const statusTag = (status) => {
    if (status === 'APPROVED') return <Tag color="success">已通过</Tag>;
    if (status === 'REJECTED') return <Tag color="error">已拒绝</Tag>;
    return <Tag color="processing">待处理</Tag>;
  };

  const emailStatusTag = (status) => {
    if (status === 'SENT') return <Tag color="success">已发送</Tag>;
    if (status === 'FAILED') return <Tag color="error">发送失败</Tag>;
    return <Tag>未发送</Tag>;
  };

  const candidateColumns = [
    {
      title: '候选人',
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
      title: '电话',
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
      width: 120,
      fixed: 'right',
      render: (_, record) => (
        <Button type="link" icon={<CheckCircleOutlined />} onClick={() => handleOffer(record)}>
          录用
        </Button>
      ),
    },
  ];

  const recordColumns = [
    { title: '候选人', dataIndex: 'candidateName', key: 'candidateName' },
    { title: '岗位', dataIndex: 'position', key: 'position' },
    {
      title: '录用状态',
      dataIndex: 'status',
      key: 'status',
      render: statusTag,
    },
    {
      title: '邮件状态',
      dataIndex: 'emailStatus',
      key: 'emailStatus',
      render: emailStatusTag,
    },
    {
      title: '操作',
      key: 'action',
      width: 180,
      fixed: 'right',
      render: (_, record) => (
        <Space>
          <Button
            type="link"
            icon={<EyeOutlined />}
            onClick={() => {
              setCurrentOfferRecord(record);
              setRecordDrawerVisible(true);
            }}
          >
            查看
          </Button>
          <Button type="link" icon={<SendOutlined />} onClick={() => sendOfferEmail(record.offerRecordId)}>
            发邮件
          </Button>
        </Space>
      ),
    },
  ];

  return (
    <div className="app-page">
      <Card title="可录用候选人" className="form-card">
        <div className="app-table-wrap">
          <Table
            rowKey="resumeId"
            loading={loading}
            columns={candidateColumns}
            dataSource={candidates}
            scroll={{ x: 900 }}
            pagination={{ pageSize: 5 }}
          />
        </div>
      </Card>

      <Card title="录用记录" className="form-card">
        <div className="app-table-wrap">
          <Table
            rowKey="offerRecordId"
            loading={loading}
            columns={recordColumns}
            dataSource={offerRecords}
            scroll={{ x: 920 }}
            pagination={{ pageSize: 8 }}
          />
        </div>
      </Card>

      <Modal
        title="确认录用"
        open={offerModalVisible}
        onCancel={() => {
          setOfferModalVisible(false);
          form.resetFields();
        }}
        onOk={handleConfirmOffer}
      >
        <Form form={form} layout="vertical">
          <Form.Item label="候选人" name="candidateName" rules={[{ required: true, message: '请输入候选人姓名' }]}>
            <Input />
          </Form.Item>
          <Form.Item label="联系电话" name="contactPhone" rules={[{ required: true, message: '请输入联系电话' }]}>
            <Input />
          </Form.Item>
          <Form.Item label="邮箱" name="email" rules={[{ required: true, message: '请输入邮箱' }]}>
            <Input />
          </Form.Item>
          <Form.Item label="岗位" name="position" rules={[{ required: true, message: '请输入岗位' }]}>
            <Input />
          </Form.Item>
          <Form.Item label="入职时间" name="entryTime" rules={[{ required: true, message: '请选择入职时间' }]}>
            <DatePicker style={{ width: '100%' }} />
          </Form.Item>
        </Form>
      </Modal>

      <Drawer
        title="录用详情"
        placement="right"
        open={recordDrawerVisible}
        onClose={() => {
          setRecordDrawerVisible(false);
          setCurrentOfferRecord(null);
        }}
        width={560}
      >
        {currentOfferRecord && (
          <Descriptions column={1}>
            <Descriptions.Item label="候选人">{currentOfferRecord.candidateName}</Descriptions.Item>
            <Descriptions.Item label="联系方式">{currentOfferRecord.contactPhone}</Descriptions.Item>
            <Descriptions.Item label="邮箱">{currentOfferRecord.email}</Descriptions.Item>
            <Descriptions.Item label="岗位">{currentOfferRecord.position}</Descriptions.Item>
            <Descriptions.Item label="状态">{statusTag(currentOfferRecord.status)}</Descriptions.Item>
            <Descriptions.Item label="邮件状态">{emailStatusTag(currentOfferRecord.emailStatus)}</Descriptions.Item>
          </Descriptions>
        )}
      </Drawer>
    </div>
  );
};

export default OfferManagement;

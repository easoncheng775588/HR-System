import React, { useEffect, useState } from 'react';
import {
  Button,
  Card,
  DatePicker,
  Form,
  Input,
  Modal,
  Select,
  Space,
  Table,
  Tag,
  TimePicker,
  message,
} from 'antd';
import { CalendarOutlined, CheckCircleOutlined, EditOutlined } from '@ant-design/icons';
import dayjs from 'dayjs';
import api from '../utils/api';

const InterviewScheduling = () => {
  const [loading, setLoading] = useState(false);
  const [resumes, setResumes] = useState([]);
  const [interviewRecords, setInterviewRecords] = useState([]);
  const [interviewers, setInterviewers] = useState([]);
  const [isMobile, setIsMobile] = useState(false);

  const [schedulingModalVisible, setSchedulingModalVisible] = useState(false);
  const [resultModalVisible, setResultModalVisible] = useState(false);
  const [currentResume, setCurrentResume] = useState(null);
  const [currentInterviewRecord, setCurrentInterviewRecord] = useState(null);

  const [form] = Form.useForm();
  const [resultForm] = Form.useForm();

  useEffect(() => {
    const onResize = () => setIsMobile(window.innerWidth < 768);
    onResize();
    window.addEventListener('resize', onResize);

    fetchData();

    return () => window.removeEventListener('resize', onResize);
  }, []);

  const fetchData = async () => {
    setLoading(true);
    try {
      const [resumeRes, interviewRes] = await Promise.all([
        api.get('/api/resume/list'),
        api.get('/api/interview/list'),
      ]);

      if (resumeRes.data?.returnCode === 'SUC0000') setResumes(resumeRes.data.body || []);
      if (interviewRes.data?.returnCode === 'SUC0000') setInterviewRecords(interviewRes.data.body || []);
    } catch (error) {
      console.error('Fetch interview data failed:', error);
      message.error('加载面试数据失败，请稍后重试');
    } finally {
      setLoading(false);
    }
  };

  const loadInterviewers = async (position = '团队经理') => {
    try {
      const encodedPosition = encodeURIComponent(position);
      const response = await api.get(`/api/users/by-position/${encodedPosition}`);
      if (response.data?.returnCode === 'SUC0000') {
        setInterviewers(response.data.body || []);
      }
    } catch (error) {
      console.error('Load interviewers failed:', error);
      setInterviewers([]);
    }
  };

  const openScheduleModal = async (resume) => {
    setCurrentResume(resume);
    setSchedulingModalVisible(true);
    form.resetFields();
    await loadInterviewers();
  };

  const submitSchedule = async () => {
    try {
      const values = await form.validateFields();
      const date = values.interviewDate;
      const time = values.interviewTime;

      const interviewTime = dayjs(`${date.format('YYYY-MM-DD')} ${time.format('HH:mm')}`);
      const interviewer = interviewers.find((x) => String(x.userId) === String(values.interviewerId));

      const payload = {
        resumeId: currentResume.resumeId,
        recruitmentRequestId: currentResume.recruitmentRequestId,
        interviewRound: values.interviewRound,
        interviewerId: values.interviewerId,
        interviewerName: interviewer?.realName || interviewer?.userName || '面试官',
        interviewerRole: values.interviewerRole,
        interviewTime: interviewTime.toDate(),
        interviewComment: values.interviewComment || '',
        createUserId: '1001',
        createUserName: '系统用户',
        updateUserId: '1001',
        updateUserName: '系统用户',
      };

      const response = await api.post('/api/interview/save', payload);
      if (response.data?.returnCode === 'SUC0000') {
        message.success('面试安排成功');
        setSchedulingModalVisible(false);
        fetchData();
      } else {
        message.error(`面试安排失败：${response.data?.errorMsg || '未知错误'}`);
      }
    } catch (error) {
      console.error('Submit schedule failed:', error);
      message.error('面试安排失败，请稍后重试');
    }
  };

  const openResultModal = (record) => {
    setCurrentInterviewRecord(record);
    resultForm.setFieldsValue({
      interviewResult: record.interviewResult,
      interviewComment: record.interviewComment,
    });
    setResultModalVisible(true);
  };

  const submitResult = async () => {
    try {
      const values = await resultForm.validateFields();
      const response = await api.put(`/api/interview/${currentInterviewRecord.interviewRecordId}/result`, values);
      if (response.data?.returnCode === 'SUC0000') {
        message.success('面试结果已更新');
        setResultModalVisible(false);
        fetchData();
      } else {
        message.error(`更新失败：${response.data?.errorMsg || '未知错误'}`);
      }
    } catch (error) {
      console.error('Submit result failed:', error);
      message.error('更新面试结果失败，请稍后重试');
    }
  };

  const resumeColumns = [
    { title: '候选人', dataIndex: 'applicantName', key: 'applicantName', width: 140 },
    { title: '岗位', dataIndex: 'jobTitle', key: 'jobTitle', width: 180, ellipsis: true },
    { title: '电话', dataIndex: 'contactPhone', key: 'contactPhone', width: 140, responsive: ['lg', 'xl', 'xxl'] },
    {
      title: '操作',
      key: 'action',
      width: 120,
      fixed: 'right',
      render: (_, record) => (
        <Button type="link" icon={<CalendarOutlined />} onClick={() => openScheduleModal(record)}>
          安排面试
        </Button>
      ),
    },
  ];

  const resultTag = (result) => {
    if (result === 'PASSED') return <Tag color="success">通过</Tag>;
    if (result === 'FAILED') return <Tag color="error">不通过</Tag>;
    return <Tag color="processing">待填写</Tag>;
  };

  const interviewColumns = [
    { title: '候选人', dataIndex: 'candidateName', key: 'candidateName', width: 120 },
    { title: '轮次', dataIndex: 'interviewRound', key: 'interviewRound', width: 110 },
    { title: '面试官', dataIndex: 'interviewerName', key: 'interviewerName', width: 120 },
    {
      title: '面试时间',
      dataIndex: 'interviewTime',
      key: 'interviewTime',
      width: 180,
      render: (v) => (v ? dayjs(v).format('YYYY-MM-DD HH:mm') : '-'),
    },
    {
      title: '结果',
      dataIndex: 'interviewResult',
      key: 'interviewResult',
      width: 100,
      render: resultTag,
    },
    {
      title: '操作',
      key: 'action',
      width: 120,
      fixed: 'right',
      render: (_, record) => (
        <Button type="link" icon={<EditOutlined />} onClick={() => openResultModal(record)}>
          录入结果
        </Button>
      ),
    },
  ];

  return (
    <div className="app-page">
      <Card title="待安排面试简历" className="form-card">
        <div className="app-table-wrap">
          <Table
            rowKey="resumeId"
            loading={loading}
            dataSource={resumes}
            columns={resumeColumns}
            scroll={{ x: isMobile ? 760 : 980 }}
            pagination={{ pageSize: isMobile ? 5 : 8, showSizeChanger: !isMobile, simple: isMobile }}
          />
        </div>
      </Card>

      <Card title="面试记录" className="form-card">
        <div className="app-table-wrap">
          <Table
            rowKey="interviewRecordId"
            loading={loading}
            dataSource={interviewRecords}
            columns={interviewColumns}
            scroll={{ x: isMobile ? 960 : 1200 }}
            pagination={{ pageSize: isMobile ? 5 : 8, showSizeChanger: !isMobile, simple: isMobile }}
          />
        </div>
      </Card>

      <Modal
        title="安排面试"
        open={schedulingModalVisible}
        onCancel={() => setSchedulingModalVisible(false)}
        onOk={submitSchedule}
        width={isMobile ? '95%' : 640}
      >
        <Form form={form} layout="vertical">
          <Form.Item name="interviewRound" label="面试轮次" rules={[{ required: true, message: '请选择面试轮次' }]}>
            <Select
              options={[
                { value: 'FIRST_ROUND', label: '一面' },
                { value: 'SECOND_ROUND', label: '二面' },
                { value: 'THIRD_ROUND', label: '三面' },
              ]}
            />
          </Form.Item>
          <Form.Item name="interviewerRole" label="面试官角色" rules={[{ required: true, message: '请选择面试官角色' }]}>
            <Select
              options={[
                { value: 'ROOM_MANAGER', label: '室经理' },
                { value: 'TEAM_MANAGER', label: '团队经理' },
                { value: 'DEPARTMENT_HEAD', label: '分管总监' },
              ]}
            />
          </Form.Item>
          <Form.Item name="interviewerId" label="面试官" rules={[{ required: true, message: '请选择面试官' }]}>
            <Select
              options={interviewers.map((x) => ({
                value: x.userId,
                label: `${x.realName || x.userName} (${x.position || '-'})`,
              }))}
            />
          </Form.Item>
          <Space style={{ width: '100%' }} size={12} wrap>
            <Form.Item name="interviewDate" label="面试日期" rules={[{ required: true, message: '请选择日期' }]} style={{ minWidth: 180, flex: 1 }}>
              <DatePicker style={{ width: '100%' }} />
            </Form.Item>
            <Form.Item name="interviewTime" label="面试时间" rules={[{ required: true, message: '请选择时间' }]} style={{ minWidth: 180, flex: 1 }}>
              <TimePicker style={{ width: '100%' }} format="HH:mm" />
            </Form.Item>
          </Space>
          <Form.Item name="interviewComment" label="备注">
            <Input.TextArea rows={3} placeholder="请输入备注" />
          </Form.Item>
        </Form>
      </Modal>

      <Modal
        title="录入面试结果"
        open={resultModalVisible}
        onCancel={() => setResultModalVisible(false)}
        onOk={submitResult}
        width={isMobile ? '95%' : 560}
      >
        <Form form={resultForm} layout="vertical">
          <Form.Item name="interviewResult" label="面试结果" rules={[{ required: true, message: '请选择结果' }]}>
            <Select
              options={[
                { value: 'PASSED', label: '通过' },
                { value: 'FAILED', label: '不通过' },
              ]}
            />
          </Form.Item>
          <Form.Item name="interviewComment" label="评语">
            <Input.TextArea rows={4} placeholder="请输入评语" />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
};

export default InterviewScheduling;

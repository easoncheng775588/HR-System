import React, { useState, useEffect, useContext } from 'react';
import {
  Table,
  Button,
  Modal,
  Form,
  Input,
  Select,
  DatePicker,
  TimePicker,
  message,
  Card,
  Tag,
  Space,
  Descriptions,
} from 'antd';
import {
  CalendarOutlined,
  UserOutlined,
  TeamOutlined,
  CheckCircleOutlined,
  CloseCircleOutlined,
  EditOutlined,
  DeleteOutlined,
  FileTextOutlined,
} from '@ant-design/icons';
import axios from 'axios';
import dayjs from 'dayjs';
import { useAuth } from '../contexts/AuthContext';

const { Option } = Select;
const { TextArea } = Input;

const InterviewScheduling = () => {
  const { user } = useAuth();
  const [resumes, setResumes] = useState([]);
  const [interviewRecords, setInterviewRecords] = useState([]);
  const [loading, setLoading] = useState(false);
  const [schedulingModalVisible, setSchedulingModalVisible] = useState(false);
  const [resultModalVisible, setResultModalVisible] = useState(false);
  const [currentResume, setCurrentResume] = useState(null);
  const [currentInterviewRecord, setCurrentInterviewRecord] = useState(null);
  const [recordModalVisible, setRecordModalVisible] = useState(false);
  const [form] = Form.useForm();
  const [resultForm] = Form.useForm();
  const [interviewers, setInterviewers] = useState([]);
  const [loadingInterviewers, setLoadingInterviewers] = useState(false);

  // 面试环节选项
  const interviewRoundOptions = [
    { value: 'FIRST_ROUND', label: '一面' },
    { value: 'SECOND_ROUND', label: '二面' },
    { value: 'THIRD_ROUND', label: '三面' },
  ];

  // 面试官角色选项
  const interviewerRoleOptions = [
    { value: 'ROOM_MANAGER', label: '室经理' },
    { value: 'TEAM_MANAGER', label: '团队经理' },
    { value: 'DEPARTMENT_HEAD', label: '分管总' },
    { value: 'ADMIN', label: '管理员' },
  ];

  // 面试结果选项
  const interviewResultOptions = [
    { value: 'PASSED', label: '通过' },
    { value: 'FAILED', label: '不通过' },
  ];

  // 面试状态映射
  const interviewStatusMap = {
    NOT_SCHEDULED: { text: '未安排', color: 'default' },
    FIRST_ROUND: { text: '一面中', color: 'blue' },
    SECOND_ROUND: { text: '二面中', color: 'purple' },
    THIRD_ROUND: { text: '三面中', color: 'orange' },
    PASSED: { text: '全部通过', color: 'green' },
    FAILED: { text: '未通过', color: 'red' },
  };

  // 面试环节映射
  const interviewRoundMap = {
    FIRST_ROUND: '一面',
    SECOND_ROUND: '二面',
    THIRD_ROUND: '三面',
  };

  // 面试官角色映射
  const interviewerRoleMap = {
    ROOM_MANAGER: '室经理',
    TEAM_MANAGER: '团队经理',
    DEPARTMENT_HEAD: '分管总',
    ADMIN: '管理员',
  };

  // 面试结果映射
  const interviewResultMap = {
    PASSED: { text: '通过', color: 'green' },
    FAILED: { text: '不通过', color: 'red' },
  };

  // 检查用户是否有权限录入面试结果
  const hasPermissionToUpdateResult = (interviewRound) => {
    if (!user || !user.position) {
      return false;
    }
    
    const position = user.position;
    // 管理员可以录入所有面试环节的结果
    if (position === '管理员') {
      return true;
    }
    
    switch (interviewRound) {
      case 'FIRST_ROUND':
        return position === '室经理';
      case 'SECOND_ROUND':
        return position === '团队经理';
      case 'THIRD_ROUND':
        return position === '分管总';
      default:
        return false;
    }
  };

  // 加载通过简历筛选的简历
  useEffect(() => {
    loadResumes();
    loadInterviewRecords();
  }, []);

  // 获取面试官列表
  const fetchInterviewers = async (position) => {
    setLoadingInterviewers(true);
    try {
      console.log('开始获取面试官列表，position:', position);
      const encodedPosition = encodeURIComponent(position);
      console.log('编码后的position:', encodedPosition);
      console.log('API请求路径:', `http://localhost:8080/api/users/by-position/${encodedPosition}`);
      const response = await axios.get(`http://localhost:8080/api/users/by-position/${encodedPosition}`);
      console.log('API响应:', response);
      if (response.data && response.data.returnCode === 'SUC0000') {
        console.log('获取面试官列表成功，数据:', response.data.body);
        // 检查response.data.body是否是数组
        if (Array.isArray(response.data.body)) {
          setInterviewers(response.data.body || []);
          // 如果有面试官，默认选择第一个
          if (response.data.body.length > 0) {
            form.setFieldsValue({
              interviewerId: response.data.body[0].userId,
              interviewerName: response.data.body[0].realName
            });
          }
        } else {
          console.error('获取面试官列表失败，数据格式错误:', response.data.body);
          setInterviewers([]);
        }
      } else {
        message.error('获取面试官列表失败：' + (response.data ? response.data.errorMsg : '未知错误'));
        console.error('获取面试官列表失败，错误信息:', response.data ? response.data.errorMsg : '未知错误');
        setInterviewers([]);
      }
    } catch (error) {
      message.error('获取面试官列表失败，请稍后重试');
      console.error('获取面试官列表失败:', error);
      setInterviewers([]);
    } finally {
      setLoadingInterviewers(false);
    }
  };

  // 监听面试环节变化，更新面试官角色和面试官列表
  const handleInterviewRoundChange = (round) => {
    const defaultRole = getDefaultInterviewerRole(round);
    form.setFieldsValue({ interviewerRole: defaultRole });
    
    // 根据面试环节获取对应的岗位
    let position = '';
    switch (round) {
      case 'FIRST_ROUND':
        position = '室经理';
        break;
      case 'SECOND_ROUND':
        position = '团队经理';
        break;
      case 'THIRD_ROUND':
        position = '分管总';
        break;
      default:
        position = '';
    }
    
    if (position) {
      fetchInterviewers(position);
    }
  };

  // 当表单加载时，初始化面试官列表
  useEffect(() => {
    const values = form.getFieldsValue();
    if (values.interviewRound) {
      handleInterviewRoundChange(values.interviewRound);
    }
  }, []);

  const loadResumes = async () => {
    setLoading(true);
    try {
      const response = await axios.get('http://localhost:8080/api/resume/list');
      if (response.data.returnCode === 'SUC0000') {
        // 只显示状态为已筛选通过的简历
        const filteredResumes = response.data.body.filter(resume => 
          resume.status === 'SCREENED'
        );
        setResumes(filteredResumes);
      }
    } catch (error) {
      message.error('获取简历列表失败');
      console.error('获取简历列表失败:', error);
    } finally {
      setLoading(false);
    }
  };

  const loadInterviewRecords = async () => {
    try {
      const response = await axios.get('http://localhost:8080/api/interview/list');
      if (response.data.returnCode === 'SUC0000') {
        setInterviewRecords(response.data.body);
      }
    } catch (error) {
      message.error('获取面试记录失败');
      console.error('获取面试记录失败:', error);
    }
  };

  const handleScheduling = (resume) => {
    // 检查权限：只有外包招聘岗的用户才能安排面试
    const isOutsourcingRecruiter = user && user.position === '外包招聘岗';
    if (!isOutsourcingRecruiter) {
      message.error('只有外包招聘岗的用户才能安排面试');
      return;
    }
    
    setCurrentResume(resume);
    // 检查是否已经有面试记录
    const existingRecords = interviewRecords.filter(record => 
      record.resumeId === resume.resumeId
    );
    
    // 根据现有面试记录确定下一轮面试
    let nextRound = 'FIRST_ROUND';
    if (existingRecords.length > 0) {
      // 按面试环节排序
      existingRecords.sort((a, b) => {
        const roundOrder = {
          FIRST_ROUND: 1,
          SECOND_ROUND: 2,
          THIRD_ROUND: 3,
        };
        return roundOrder[a.interviewRound] - roundOrder[b.interviewRound];
      });
      
      // 获取最后一轮面试
      const lastRound = existingRecords[existingRecords.length - 1];
      if (lastRound.interviewResult === 'PASSED') {
        // 如果最后一轮通过，安排下一轮
        if (lastRound.interviewRound === 'FIRST_ROUND') {
          nextRound = 'SECOND_ROUND';
        } else if (lastRound.interviewRound === 'SECOND_ROUND') {
          nextRound = 'THIRD_ROUND';
        } else {
          // 已经是三面，不能再安排
          message.error('该简历已经完成所有面试环节');
          return;
        }
      } else {
        // 如果最后一轮未通过，不能再安排
        message.error('该简历未通过上一轮面试，不能安排下一轮');
        return;
      }
    }
    
    // 设置表单默认值
    form.setFieldsValue({
      interviewRound: nextRound,
      interviewerRole: getDefaultInterviewerRole(nextRound),
    });
    
    // 初始化面试官列表
    setTimeout(() => {
      handleInterviewRoundChange(nextRound);
    }, 100);
    
    setSchedulingModalVisible(true);
  };

  const getDefaultInterviewerRole = (interviewRound) => {
    switch (interviewRound) {
      case 'FIRST_ROUND':
        return 'ROOM_MANAGER';
      case 'SECOND_ROUND':
        return 'TEAM_MANAGER';
      case 'THIRD_ROUND':
        return 'DEPARTMENT_HEAD';
      default:
        return 'ROOM_MANAGER';
    }
  };

  const handleConfirmScheduling = async () => {
    try {
      const values = await form.validateFields();
      
      // 构建面试记录对象
      const interviewRecord = {
        resumeId: currentResume.resumeId,
        recruitmentRequestId: currentResume.recruitmentRequestId,
        interviewRound: values.interviewRound,
        interviewerId: values.interviewerId,
        interviewerName: values.interviewerName,
        interviewerRole: values.interviewerRole,
        interviewTime: values.interviewDate
          ? dayjs(`${values.interviewDate.format('YYYY-MM-DD')} ${values.interviewTime.format('HH:mm')}`).toDate()
          : null,
        interviewResult: null,
        interviewComment: null,
      };
      
      // 保存面试记录
      const response = await axios.post('http://localhost:8080/api/interview/save', interviewRecord);
      if (response.data.returnCode === 'SUC0000') {
        message.success('面试安排成功');
        setSchedulingModalVisible(false);
        form.resetFields();
        loadInterviewRecords();
        loadResumes();
      } else {
        message.error('面试安排失败：' + response.data.errorMsg);
      }
    } catch (error) {
      message.error('表单验证失败');
      console.error('表单验证失败:', error);
    }
  };

  const handleUpdateResult = (record) => {
    setCurrentInterviewRecord(record);
    resultForm.setFieldsValue({
      interviewResult: record.interviewResult,
      interviewComment: record.interviewComment,
    });
    // 直接打开面试结果录入模态框，设置更高的zIndex
    setResultModalVisible(true);
  };

  const handleConfirmResult = async () => {
    try {
      const values = await resultForm.validateFields();
      
      // 更新面试结果
      const response = await axios.put(
        `http://localhost:8080/api/interview/${currentInterviewRecord.interviewRecordId}/result`,
        {
          interviewResult: values.interviewResult,
          interviewComment: values.interviewComment,
        }
      );
      
      if (response.data.returnCode === 'SUC0000') {
        message.success('面试结果更新成功');
        setResultModalVisible(false);
        resultForm.resetFields();
        loadInterviewRecords();
        loadResumes();
        // 通知MainLayout更新待面试数量
        localStorage.setItem('interviewResultUpdated', 'true');
      } else {
        message.error('面试结果更新失败：' + response.data.errorMsg);
      }
    } catch (error) {
      message.error('表单验证失败');
      console.error('表单验证失败:', error);
    }
  };

  const getInterviewRecordsByResumeId = (resumeId) => {
    return interviewRecords.filter(record => record.resumeId === resumeId);
  };

  const [isMobile, setIsMobile] = useState(false);

  useEffect(() => {
    const handleResize = () => {
      setIsMobile(window.innerWidth < 768);
    };

    handleResize();
    window.addEventListener('resize', handleResize);
    return () => window.removeEventListener('resize', handleResize);
  }, []);

  // 简历列表列定义
  const resumeColumns = [
    {
      title: '简历ID',
      dataIndex: 'resumeId',
      key: 'resumeId',
    },
    {
      title: '应聘者姓名',
      dataIndex: 'applicantName',
      key: 'applicantName',
      render: text => <a>{text}</a>,
    },
    {
      title: '应聘岗位',
      dataIndex: 'jobTitle',
      key: 'jobTitle',
    },
    {
      title: '面试状态',
      dataIndex: 'interviewStatus',
      key: 'interviewStatus',
      render: status => {
        const statusInfo = interviewStatusMap[status] || { text: status, color: 'default' };
        return <Tag color={statusInfo.color}>{statusInfo.text}</Tag>;
      },
    },
    {
      title: '操作',
      key: 'action',
      width: isMobile ? 180 : 240,
      fixed: 'right',
      render: (_, record) => {
        const isOutsourcingRecruiter = user && user.position === '外包招聘岗';
        return (
          <Space size={isMobile ? 'small' : 'middle'}>
            {isOutsourcingRecruiter && (
              <Button
                type="link"
                icon={<CalendarOutlined />}
                onClick={() => handleScheduling(record)}
                style={{ color: '#52c41a' }}
                size={isMobile ? 'small' : 'middle'}
              >
                安排面试
              </Button>
            )}
            <Button
              type="link"
              icon={<FileTextOutlined />}
              onClick={() => {
                const records = getInterviewRecordsByResumeId(record.resumeId);
                if (records.length > 0) {
                  // 显示面试记录详情
                  setCurrentResume(record);
                  setRecordModalVisible(true);
                } else {
                  message.info('该简历暂无面试记录');
                }
              }}
              size={isMobile ? 'small' : 'middle'}
            >
              面试记录
            </Button>
          </Space>
        );
      },
    },
  ];

  return (
    <div className="interview-scheduling">
      
      <Card>
        <h3>已通过筛选的简历</h3>
        <Table
          columns={resumeColumns}
          dataSource={resumes}
          rowKey="resumeId"
          loading={loading}
          pagination={{
            showSizeChanger: true,
            pageSizeOptions: ['10', '20', '50'],
          }}
        />
      </Card>

      {/* 面试安排模态框 */}
      <Modal
        title="安排面试"
        open={schedulingModalVisible}
        onOk={handleConfirmScheduling}
        onCancel={() => setSchedulingModalVisible(false)}
        width={600}
      >
        <Form form={form} layout="vertical">
          <Form.Item
            name="interviewRound"
            label="面试环节"
            rules={[{ required: true, message: '请选择面试环节' }]}
          >
            <Select disabled>
              {interviewRoundOptions.map(option => (
                <Option key={option.value} value={option.value}>
                  {option.label}
                </Option>
              ))}
            </Select>
          </Form.Item>
          
          <Form.Item
            name="interviewerRole"
            label="面试官角色"
            rules={[{ required: true, message: '请选择面试官角色' }]}
          >
            <Select disabled>
              {interviewerRoleOptions.map(option => (
                <Option key={option.value} value={option.value}>
                  {option.label}
                </Option>
              ))}
            </Select>
          </Form.Item>
          
          <Form.Item
            name="interviewerId"
            label="面试官"
            rules={[{ required: true, message: '请选择面试官' }]}
          >
            <Select
              loading={loadingInterviewers}
              placeholder="请选择面试官"
              onChange={(value) => {
                // 根据选择的面试官ID，更新面试官姓名
                const selectedInterviewer = interviewers.find(item => item.userId === value);
                if (selectedInterviewer) {
                  form.setFieldsValue({ interviewerName: selectedInterviewer.realName });
                }
              }}
            >
              {interviewers.map(interviewer => (
                <Option key={interviewer.userId} value={interviewer.userId}>
                  {interviewer.realName}
                </Option>
              ))}
            </Select>
          </Form.Item>
          
          <Form.Item
            name="interviewerName"
            label="面试官姓名"
            rules={[{ required: true, message: '请输入面试官姓名' }]}
          >
            <Input placeholder="请输入面试官姓名" disabled />
          </Form.Item>
          
          <Form.Item
            name="interviewDate"
            label="面试日期"
            rules={[{ required: true, message: '请选择面试日期' }]}
          >
            <DatePicker style={{ width: '100%' }} />
          </Form.Item>
          
          <Form.Item
            name="interviewTime"
            label="面试时间"
            rules={[{ required: true, message: '请选择面试时间' }]}
          >
            <TimePicker style={{ width: '100%' }} format="HH:mm" />
          </Form.Item>
        </Form>
      </Modal>

      {/* 面试记录模态框 */}
      <Modal
        title={currentResume ? `${currentResume.applicantName}的面试记录` : '面试记录'}
        open={recordModalVisible}
        onCancel={() => setRecordModalVisible(false)}
        footer={[
          <Button key="ok" type="primary" onClick={() => setRecordModalVisible(false)}>
            知道了
          </Button>
        ]}
        width={800}
        zIndex={1000}
      >
        {currentResume && (
          <div>
            {getInterviewRecordsByResumeId(currentResume.resumeId).map((item, index) => (
              <Card key={item.interviewRecordId} style={{ marginBottom: 16 }}>
                <Descriptions column={2}>
                  <Descriptions.Item label="面试环节">
                    {interviewRoundMap[item.interviewRound]}
                  </Descriptions.Item>
                  <Descriptions.Item label="面试官角色">
                    {interviewerRoleMap[item.interviewerRole]}
                  </Descriptions.Item>
                  <Descriptions.Item label="面试官">
                    {item.interviewerName}
                  </Descriptions.Item>
                  <Descriptions.Item label="面试时间">
                    {item.interviewTime ? dayjs(item.interviewTime).format('YYYY-MM-DD HH:mm') : '未设置'}
                  </Descriptions.Item>
                  <Descriptions.Item label="面试结果" span={2}>
                    {item.interviewResult ? (
                      <Tag color={interviewResultMap[item.interviewResult].color}>
                        {interviewResultMap[item.interviewResult].text}
                      </Tag>
                    ) : (
                      <Tag color="default">未评价</Tag>
                    )}
                  </Descriptions.Item>
                  <Descriptions.Item label="面试评语" span={2}>
                    {item.interviewComment || '无'}
                  </Descriptions.Item>
                </Descriptions>
                {!item.interviewResult && hasPermissionToUpdateResult(item.interviewRound) && (
                  <Button
                    type="primary"
                    icon={<CheckCircleOutlined />}
                    style={{ marginTop: 16 }}
                    onClick={() => {
                      // 关闭当前模态框
                      setRecordModalVisible(false);
                      // 打开面试结果录入模态框
                      setTimeout(() => {
                        handleUpdateResult(item);
                      }, 300);
                    }}
                  >
                    录入面试结果
                  </Button>
                )}
                {!item.interviewResult && !hasPermissionToUpdateResult(item.interviewRound) && (
                  <div style={{ marginTop: 16, fontSize: 12, color: '#999' }}>
                    无权限录入此面试环节的结果
                  </div>
                )}
              </Card>
            ))}
          </div>
        )}
      </Modal>

      {/* 面试结果录入模态框 */}
      <Modal
        title="录入面试结果"
        open={resultModalVisible}
        onOk={handleConfirmResult}
        onCancel={() => setResultModalVisible(false)}
        width={600}
        zIndex={2000}
      >
        <Form form={resultForm} layout="vertical">
          <Form.Item
            name="interviewResult"
            label="面试结果"
            rules={[{ required: true, message: '请选择面试结果' }]}
          >
            <Select>
              {interviewResultOptions.map(option => (
                <Option key={option.value} value={option.value}>
                  {option.label}
                </Option>
              ))}
            </Select>
          </Form.Item>
          
          <Form.Item
            name="interviewComment"
            label="面试评语"
            rules={[{ required: true, message: '请输入面试评语' }]}
          >
            <TextArea rows={4} placeholder="请输入面试评语" />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
};

export default InterviewScheduling;

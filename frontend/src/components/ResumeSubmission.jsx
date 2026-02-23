import React, { useState, useEffect } from 'react';
import { Table, Button, Card, Modal, Form, Input, Upload, message, Space, Tag, Drawer, Descriptions } from 'antd';
import { EyeOutlined, CheckCircleOutlined, UploadOutlined, FileTextOutlined, UserOutlined } from '@ant-design/icons';
import api from '../utils/api';

const ResumeSubmission = () => {
  const [publishedJobs, setPublishedJobs] = useState([]);
  const [loading, setLoading] = useState(false);
  const [viewDrawerVisible, setViewDrawerVisible] = useState(false);
  const [applyModalVisible, setApplyModalVisible] = useState(false);
  const [selectedJob, setSelectedJob] = useState(null);
  const [resumeForm] = Form.useForm();
  const [isMobile, setIsMobile] = useState(false);
  const [uploadedFile, setUploadedFile] = useState(null);

  useEffect(() => {
    fetchPublishedJobs();
    handleResize();
    window.addEventListener('resize', handleResize);
    return () => window.removeEventListener('resize', handleResize);
  }, []);

  const handleResize = () => {
    setIsMobile(window.innerWidth < 768);
  };

  const fetchPublishedJobs = async () => {
    setLoading(true);
    try {
      // 调用后端API获取已发布的岗位
      console.log('开始获取已发布的岗位...');
      const response = await api.get('/api/recruitment-request/approval/status/3RDAPPROVED');
      console.log('API响应:', response);
      if (response.data && response.data.returnCode === 'SUC0000') {
        // 筛选已发布的岗位
        const jobs = (response.data.body || []).filter(item => item.positionPublishStatus === 'PUBLISHED');
        console.log('已发布的岗位数据:', jobs);
        setPublishedJobs(jobs);
      } else {
        console.error('获取已发布的岗位失败:', response.data ? response.data.errorMsg : '未知错误');
        message.error('获取已发布的岗位失败：' + (response.data ? response.data.errorMsg : '未知错误'));
      }
    } catch (error) {
      console.error('Fetch error:', error);
      message.error('获取已发布的岗位失败，请稍后重试');
    } finally {
      setLoading(false);
    }
  };

  const handleView = (record) => {
    setSelectedJob(record);
    setViewDrawerVisible(true);
  };

  const handleApply = (record) => {
    setSelectedJob(record);
    resumeForm.resetFields();
    setUploadedFile(null);
    setApplyModalVisible(true);
  };

  const handleCancelApply = () => {
    setApplyModalVisible(false);
    resumeForm.resetFields();
    setUploadedFile(null);
  };

  const handleConfirmApply = async () => {
    try {
      const values = await resumeForm.validateFields();
      if (!uploadedFile) {
        message.error('请上传简历');
        return;
      }
      
      // 调用后端API提交简历
      console.log('提交简历:', {
        jobId: selectedJob.recruitmentRequestId,
        jobTitle: selectedJob.requestTitle,
        applicantName: values.applicantName,
        contactPhone: values.contactPhone,
        email: values.email,
        education: values.education,
        workExperience: values.workExperience,
        resumeFile: uploadedFile
      });
      
      const response = await api.post('/api/resume/submit', {
        recruitmentRequestId: selectedJob.recruitmentRequestId,
        jobTitle: selectedJob.requestTitle,
        applicantName: values.applicantName,
        contactPhone: values.contactPhone,
        email: values.email,
        education: values.education,
        workExperience: values.workExperience,
        resumeFileName: uploadedFile.name,
        resumeFileUrl: uploadedFile.url,
        status: 'PENDING_SCREENING'
      });
      
      if (response.data && response.data.returnCode === 'SUC0000') {
        message.success('简历提交成功，状态为待筛选');
        setApplyModalVisible(false);
        resumeForm.resetFields();
        setUploadedFile(null);
      } else {
        message.error('提交失败：' + (response.data ? response.data.errorMsg : '未知错误'));
      }
    } catch (error) {
      message.error('提交失败，请稍后重试');
      console.error('Apply error:', error);
    }
  };

  const getCategoryText = (category) => {
    const categoryMap = {
      'technical': '技术',
      'business': '业务',
      'operation': '运营',
      'other': '其他'
    };
    return categoryMap[category] || category;
  };

  const getPlatformText = (platform) => {
    const platformMap = {
      'frontend': '前端',
      'backend': '后端',
      'mobile': '移动端',
      'ai': '人工智能'
    };
    return platformMap[platform] || platform;
  };

  const getTypeText = (type) => {
    const typeMap = {
      'fulltime': '全职',
      'parttime': '兼职',
      'intern': '实习生'
    };
    return typeMap[type] || type;
  };

  const getLevelText = (level) => {
    const levelMap = {
      'junior': '初级',
      'senior': '中级',
      'advanced': '高级',
      'expert': '专家'
    };
    return levelMap[level] || level;
  };

  const columns = [
    {
      title: '岗位标题',
      dataIndex: 'requestTitle',
      key: 'requestTitle',
      width: isMobile ? 120 : 200,
      ellipsis: true
    },
    {
      title: '岗位/班组',
      dataIndex: 'positionOrTeam',
      key: 'positionOrTeam',
      width: isMobile ? 100 : 150,
      ellipsis: true,
      responsive: ['md', 'lg', 'xl', 'xxl']
    },
    {
      title: '所属分类',
      dataIndex: 'category',
      key: 'category',
      width: isMobile ? 80 : 100,
      render: getCategoryText,
      responsive: ['md', 'lg', 'xl', 'xxl']
    },
    {
      title: '招聘人数',
      dataIndex: 'supplementCount',
      key: 'supplementCount',
      width: isMobile ? 80 : 100,
      align: 'center',
      responsive: ['md', 'lg', 'xl', 'xxl']
    },
    {
      title: '岗位级别',
      dataIndex: 'proposedLevel',
      key: 'proposedLevel',
      width: isMobile ? 80 : 100,
      render: getLevelText,
      responsive: ['lg', 'xl', 'xxl']
    },
    {
      title: '操作',
      key: 'action',
      width: isMobile ? 180 : 240,
      fixed: 'right',
      render: (text, record) => (
        <Space size="small">
          <Button 
            type="link" 
            icon={<EyeOutlined />}
            onClick={() => handleView(record)}
            size={isMobile ? 'small' : 'middle'}
          >
            查看
          </Button>
          <Button 
            type="link" 
            icon={<CheckCircleOutlined />}
            onClick={() => handleApply(record)}
            style={{ color: '#52c41a' }}
            size={isMobile ? 'small' : 'middle'}
          >
            申请
          </Button>
        </Space>
      )
    }
  ];

  const normFile = (e) => {
    console.log('Upload event:', e);
    if (Array.isArray(e)) {
      return e;
    }
    if (e.file.status === 'done') {
      message.success(`${e.file.name} 上传成功`);
      // 正确处理后端返回的响应格式
      const fileUrl = e.file.response?.body?.url || e.file.response?.url || e.file.url;
      console.log('File URL:', fileUrl);
      setUploadedFile({
        name: e.file.name,
        url: fileUrl,
        size: e.file.size
      });
    } else if (e.file.status === 'error') {
      message.error(`${e.file.name} 上传失败`);
    }
    return e && e.fileList;
  };

  return (
    <div className="interview-scheduling">
      <Card 
        title="简历提交管理" 
        extra={
          <div style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
            <Button 
              type="primary" 
              size={isMobile ? 'small' : 'middle'}
              onClick={fetchPublishedJobs}
              loading={loading}
            >
              刷新岗位
            </Button>
            <Tag color="blue">共 {publishedJobs.length} 个已发布岗位</Tag>
          </div>
        }
      >
        <div style={{ overflow: 'auto' }}>
          <Table
            dataSource={publishedJobs}
            loading={loading}
            rowKey="recruitmentRequestId"
            columns={columns}
            scroll={{ x: isMobile ? 800 : 1200 }}
            pagination={{
              pageSize: isMobile ? 5 : 10,
              showSizeChanger: !isMobile,
              showTotal: (total) => `共 ${total} 条记录`,
              simple: isMobile
            }}
            size={isMobile ? 'small' : 'middle'}
            locale={{
              emptyText: '暂无已发布的岗位，请先发布岗位',
              loadingText: '加载中...',
              pagination: {
                items_per_page: '条/页',
                jump_to: '跳至',
                page: '页',
                prev_page: '上一页',
                next_page: '下一页',
                total: '共 {total} 条记录',
                show_total: true
              }
            }}
          />
        </div>
      </Card>

      <Drawer
        title="岗位详情"
        placement="right"
        onClose={() => setViewDrawerVisible(false)}
        open={viewDrawerVisible}
        width={isMobile ? '100%' : 600}
        closeIcon={<EyeOutlined />}
      >
        {selectedJob && (
          <Descriptions column={1} bordered size={isMobile ? 'small' : 'default'}>
            <Descriptions.Item label="岗位标题">{selectedJob.requestTitle}</Descriptions.Item>
            <Descriptions.Item label="岗位/班组">{selectedJob.positionOrTeam}</Descriptions.Item>
            <Descriptions.Item label="所属分类">{getCategoryText(selectedJob.category)}</Descriptions.Item>
            <Descriptions.Item label="技术平台">{getPlatformText(selectedJob.technicalPlatform)}</Descriptions.Item>
            <Descriptions.Item label="类型">{getTypeText(selectedJob.type)}</Descriptions.Item>
            <Descriptions.Item label="招聘人数">{selectedJob.supplementCount}</Descriptions.Item>
            <Descriptions.Item label="岗位级别">{getLevelText(selectedJob.proposedLevel)}</Descriptions.Item>
            <Descriptions.Item label="工作经验">{selectedJob.experienceYears}</Descriptions.Item>
            <Descriptions.Item label="创建时间">
              {selectedJob.createTime ? new Date(selectedJob.createTime).toLocaleString('zh-CN') : '-'}
            </Descriptions.Item>
            <Descriptions.Item label="岗位职责" span={3}>
              {selectedJob.positionResponsibility}
            </Descriptions.Item>
            <Descriptions.Item label="发布状态">
              <Tag color="success">已发布</Tag>
            </Descriptions.Item>
          </Descriptions>
        )}
      </Drawer>

      <Modal
        title="申请岗位"
        open={applyModalVisible}
        onOk={handleConfirmApply}
        onCancel={handleCancelApply}
        okText="提交"
        cancelText="取消"
        okButtonProps={{ style: { background: '#52c41a', borderColor: '#52c41a' } }}
        width={isMobile ? '90%' : 700}
      >
        {selectedJob && (
          <Card 
            title="岗位信息" 
            style={{ marginBottom: 20 }}
            size={isMobile ? 'small' : 'default'}
          >
            <Descriptions column={2} size={isMobile ? 'small' : 'default'}>
              <Descriptions.Item label="岗位标题">{selectedJob.requestTitle}</Descriptions.Item>
              <Descriptions.Item label="岗位/班组">{selectedJob.positionOrTeam}</Descriptions.Item>
              <Descriptions.Item label="所属分类">{getCategoryText(selectedJob.category)}</Descriptions.Item>
              <Descriptions.Item label="招聘人数">{selectedJob.supplementCount}</Descriptions.Item>
              <Descriptions.Item label="岗位级别">{getLevelText(selectedJob.proposedLevel)}</Descriptions.Item>
              <Descriptions.Item label="工作经验">{selectedJob.experienceYears}</Descriptions.Item>
            </Descriptions>
          </Card>
        )}

        <Form
          form={resumeForm}
          layout="vertical"
        >
          <Form.Item
            name="applicantName"
            label="姓名"
            rules={[{ required: true, message: '请输入姓名' }]}
          >
            <Input placeholder="请输入姓名" size={isMobile ? 'small' : 'middle'} />
          </Form.Item>

          <Form.Item
            name="contactPhone"
            label="联系电话"
            rules={[{ required: true, message: '请输入联系电话' }]}
          >
            <Input placeholder="请输入联系电话" size={isMobile ? 'small' : 'middle'} />
          </Form.Item>

          <Form.Item
            name="email"
            label="邮箱"
            rules={[{ required: true, message: '请输入邮箱' }, { type: 'email', message: '请输入正确的邮箱格式' }]}
          >
            <Input placeholder="请输入邮箱" size={isMobile ? 'small' : 'middle'} />
          </Form.Item>

          <Form.Item
            name="education"
            label="学历"
            rules={[{ required: true, message: '请输入学历' }]}
          >
            <Input placeholder="请输入学历" size={isMobile ? 'small' : 'middle'} />
          </Form.Item>

          <Form.Item
            name="workExperience"
            label="工作经验"
            rules={[{ required: true, message: '请输入工作经验' }]}
          >
            <Input placeholder="请输入工作经验" size={isMobile ? 'small' : 'middle'} />
          </Form.Item>

          <Form.Item
            name="resume"
            label="上传简历"
            rules={[{ required: true, message: '请上传简历' }]}
            valuePropName="fileList"
            getValueFromEvent={normFile}
          >
            <Upload
              name="resume"
              action="http://localhost:8080/api/upload"
              accept=".pdf,.doc,.docx"
              maxCount={1}
              showUploadList={true}
              beforeUpload={(file) => {
                const isPDF = file.type === 'application/pdf';
                const isDoc = file.type === 'application/msword';
                const isDocx = file.type === 'application/vnd.openxmlformats-officedocument.wordprocessingml.document';
                const isLt2M = file.size / 1024 / 1024 < 2;
                if (!isPDF && !isDoc && !isDocx) {
                  message.error('只能上传 PDF、Word 文档！');
                  return false;
                }
                if (!isLt2M) {
                  message.error('文件大小不能超过 2MB！');
                  return false;
                }
                return true;
              }}
            >
              <Button icon={<UploadOutlined />} size={isMobile ? 'small' : 'middle'}>
                选择文件
              </Button>
              <span style={{ marginLeft: 8, fontSize: 12, color: '#999' }}>
                支持 PDF、Word 文档，最大 2MB
              </span>
            </Upload>
          </Form.Item>

          <Form.Item
            name="selfIntroduction"
            label="自我介绍"
          >
            <Input.TextArea 
              rows={4} 
              placeholder="请简要介绍自己" 
              size={isMobile ? 'small' : 'middle'}
            />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
};

export default ResumeSubmission;
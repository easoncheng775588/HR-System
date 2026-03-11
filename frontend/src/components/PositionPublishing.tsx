import React, { useEffect, useState } from 'react';
import {
  Button,
  Card,
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
import { CheckCircleOutlined, CloseOutlined, EyeOutlined } from '@ant-design/icons';
import api, { CacheManager } from '../utils/api';
import { handleApiError } from '../utils/errorHandler';
import { useParam } from '../contexts/ParamContext';

const PositionPublishing = () => {
  const { getLevelText, getPlatformText } = useParam();
  const [data, setData] = useState([]);
  const [loading, setLoading] = useState(false);
  const [isMobile, setIsMobile] = useState(false);

  const [selectedRecord, setSelectedRecord] = useState(null);
  const [viewDrawerVisible, setViewDrawerVisible] = useState(false);
  const [approvalHistory, setApprovalHistory] = useState([]);

  const [publishModalVisible, setPublishModalVisible] = useState(false);
  const [positionForm] = Form.useForm();

  useEffect(() => {
    fetchApprovedRequests();
    const onResize = () => setIsMobile(window.innerWidth < 768);
    onResize();
    window.addEventListener('resize', onResize);
    return () => window.removeEventListener('resize', onResize);
  }, []);

  const fetchApprovedRequests = async () => {
    setLoading(true);
    const cacheKey = 'approved_requests';

    try {
      const cachedData = CacheManager.get(cacheKey);
      if (cachedData) {
        setData(cachedData);
        setLoading(false);
        return;
      }

      const response = await api.get('/api/recruitment-request/approval/status/3RDAPPROVED');
      if (response.data?.returnCode === 'SUC0000') {
        const approvedRequests = (response.data.body || []).map((item) => ({
          ...item,
          positionPublishStatus:
            item.positionPublishStatus === 'PUBLISHED' ? 'PUBLISHED' : 'NOT_PUBLISHED',
        }));
        setData(approvedRequests);
        CacheManager.set(cacheKey, approvedRequests, 5 * 60 * 1000);
      } else {
        message.error(`获取已审批通过申请失败：${response.data?.errorMsg || '未知错误'}`);
      }
    } catch (error) {
      handleApiError(error, '获取已审批通过申请失败，请稍后重试');
    } finally {
      setLoading(false);
    }
  };

  const fetchApprovalHistory = async (recruitmentRequestId) => {
    try {
      const response = await api.get(`/api/recruitment-request/approval-history/${recruitmentRequestId}`);
      if (response.data?.returnCode === 'SUC0000') {
        setApprovalHistory(response.data.body || []);
      } else {
        setApprovalHistory([]);
        message.error(`获取审批历史失败：${response.data?.errorMsg || '未知错误'}`);
      }
    } catch (_error) {
      setApprovalHistory([]);
      message.error('获取审批历史失败，请稍后重试');
    }
  };

  const handleView = (record) => {
    setSelectedRecord(record);
    setViewDrawerVisible(true);
    fetchApprovalHistory(record.recruitmentRequestId);
  };

  const handlePublish = (record) => {
    setSelectedRecord(record);
    positionForm.setFieldsValue({
      positionTitle: record.requestTitle,
      recruitCount: record.supplementCount,
      positionLevel: getLevelText(record.proposedLevel),
      experienceYears: record.experienceYears,
      jobDescription: record.positionResponsibility,
      technicalRequirements: record.skillRequirement || '',
    });
    setPublishModalVisible(true);
  };

  const handleConfirmPublish = async () => {
    try {
      await positionForm.validateFields();
      const response = await api.put(
        `/api/recruitment-request/${selectedRecord.recruitmentRequestId}/publish-status`,
        { publishStatus: 'PUBLISHED' }
      );

      if (response.data?.returnCode === 'SUC0000') {
        setPublishModalVisible(false);
        positionForm.resetFields();
        CacheManager.remove('approved_requests');
        fetchApprovedRequests();
      } else {
        message.error(`发布失败：${response.data?.errorMsg || '未知错误'}`);
      }
    } catch (error) {
      if (error?.errorFields) {
        message.error('请完善岗位发布信息');
      } else {
        handleApiError(error, '发布失败，请稍后重试');
      }
    }
  };

  const handleRevoke = async (record) => {
    try {
      const response = await api.put(
        `/api/recruitment-request/${record.recruitmentRequestId}/publish-status`,
        { publishStatus: 'NOT_PUBLISHED' }
      );

      if (response.data?.returnCode === 'SUC0000') {
        CacheManager.remove('approved_requests');
        fetchApprovedRequests();
      } else {
        message.error(`撤销失败：${response.data?.errorMsg || '未知错误'}`);
      }
    } catch (error) {
      handleApiError(error, '撤销失败，请稍后重试');
    }
  };

  const getPublishStatusTag = (status) => {
    if (status === 'PUBLISHED') return <Tag color="success">已发布</Tag>;
    return <Tag>未发布</Tag>;
  };

  const columns = [
    {
      title: '岗位标题',
      dataIndex: 'requestTitle',
      key: 'requestTitle',
      width: isMobile ? 160 : 220,
      ellipsis: true,
    },
    {
      title: '所属团队',
      dataIndex: 'team',
      key: 'team',
      width: 140,
      ellipsis: true,
      responsive: ['md', 'lg', 'xl', 'xxl'],
      render: (text) => text || '-',
    },
    {
      title: '技术平台',
      dataIndex: 'technicalPlatform',
      key: 'technicalPlatform',
      width: 120,
      responsive: ['lg', 'xl', 'xxl'],
      render: (text) => (text ? getPlatformText(text) : '-'),
    },
    {
      title: '补充人数',
      dataIndex: 'supplementCount',
      key: 'supplementCount',
      width: 100,
      align: 'center',
      responsive: ['md', 'lg', 'xl', 'xxl'],
    },
    {
      title: '建议级别',
      dataIndex: 'proposedLevel',
      key: 'proposedLevel',
      width: 120,
      responsive: ['lg', 'xl', 'xxl'],
      render: (text) => (text ? getLevelText(text) : '-'),
    },
    {
      title: '发布状态',
      dataIndex: 'positionPublishStatus',
      key: 'positionPublishStatus',
      width: 120,
      render: getPublishStatusTag,
    },
    {
      title: '操作',
      key: 'action',
      width: 220,
      fixed: 'right',
      render: (_, record) => (
        <Space size="small">
          <Button type="link" icon={<EyeOutlined />} onClick={() => handleView(record)}>
            查看
          </Button>
          {record.positionPublishStatus === 'NOT_PUBLISHED' ? (
            <Button type="link" icon={<CheckCircleOutlined />} onClick={() => handlePublish(record)}>
              发布
            </Button>
          ) : (
            <Button type="link" danger icon={<CloseOutlined />} onClick={() => handleRevoke(record)}>
              撤销
            </Button>
          )}
        </Space>
      ),
    },
  ];
  const hasData = data.length > 0;
  const tableColumns = hasData ? columns : columns.map(({ width, fixed, ...rest }) => rest);

  return (
    <div className="app-page">
      <Card title="岗位发布">
        <div className="app-table-wrap">
          <Table
            className={hasData ? '' : 'table-empty-state'}
            rowKey="recruitmentRequestId"
            loading={loading}
            dataSource={data}
            columns={tableColumns}
            scroll={hasData ? { x: isMobile ? 900 : 1300 } : undefined}
            pagination={{
              defaultPageSize: 10,
              showSizeChanger: true,
              pageSizeOptions: [10, 20, 30, 50, 100],
              hideOnSinglePage: false,
              showTotal: (total) => `共 ${total} 条记录`,
            }}
          />
        </div>
      </Card>

      <Drawer
        title="申请详情"
        placement="right"
        open={viewDrawerVisible}
        onClose={() => {
          setViewDrawerVisible(false);
          setSelectedRecord(null);
          setApprovalHistory([]);
        }}
        width={isMobile ? '100%' : 680}
      >
        {selectedRecord && (
          <>
            <Descriptions column={1}>
              <Descriptions.Item label="岗位标题">{selectedRecord.requestTitle}</Descriptions.Item>
              <Descriptions.Item label="所属团队">{selectedRecord.team || '-'}</Descriptions.Item>
              <Descriptions.Item label="技术平台">{getPlatformText(selectedRecord.technicalPlatform)}</Descriptions.Item>
              <Descriptions.Item label="补充人数">{selectedRecord.supplementCount}</Descriptions.Item>
              <Descriptions.Item label="建议级别">{getLevelText(selectedRecord.proposedLevel)}</Descriptions.Item>
              <Descriptions.Item label="岗位职责">{selectedRecord.positionResponsibility || '-'}</Descriptions.Item>
            </Descriptions>

            <Card title="审批历史" size="small" style={{ marginTop: 16 }}>
              <Table
                rowKey="approvalHistoryId"
                pagination={false}
                size="small"
                dataSource={approvalHistory}
                columns={[
                  { title: '审批级别', dataIndex: 'approvalLevel', key: 'approvalLevel', width: 100 },
                  { title: '审批人', dataIndex: 'approverName', key: 'approverName', width: 120 },
                  { title: '审批状态', dataIndex: 'approvalStatus', key: 'approvalStatus', width: 120 },
                  {
                    title: '审批时间',
                    dataIndex: 'approvalTime',
                    key: 'approvalTime',
                    render: (v) => (v ? new Date(v).toLocaleString('zh-CN') : '-'),
                  },
                ]}
              />
            </Card>
          </>
        )}
      </Drawer>

      <Modal
        title="发布岗位"
        open={publishModalVisible}
        onCancel={() => {
          setPublishModalVisible(false);
          positionForm.resetFields();
        }}
        onOk={handleConfirmPublish}
        width={isMobile ? '95%' : 680}
      >
        <Form form={positionForm} layout="vertical">
          <Form.Item label="岗位标题" name="positionTitle" rules={[{ required: true, message: '请输入岗位标题' }]}>
            <Input placeholder="请输入岗位标题" />
          </Form.Item>
          <Form.Item label="招聘人数" name="recruitCount" rules={[{ required: true, message: '请输入招聘人数' }]}>
            <Input placeholder="请输入招聘人数" />
          </Form.Item>
          <Form.Item label="岗位级别" name="positionLevel" rules={[{ required: true, message: '请输入岗位级别' }]}>
            <Input placeholder="请输入岗位级别" />
          </Form.Item>
          <Form.Item label="经验年限" name="experienceYears" rules={[{ required: true, message: '请输入经验年限' }]}>
            <Input placeholder="请输入经验年限" />
          </Form.Item>
          <Form.Item label="岗位描述" name="jobDescription" rules={[{ required: true, message: '请输入岗位描述' }]}>
            <Input.TextArea rows={3} placeholder="请输入岗位描述" />
          </Form.Item>
          <Form.Item label="技术要求" name="technicalRequirements">
            <Input.TextArea rows={3} placeholder="请输入技术要求" />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
};

export default PositionPublishing;

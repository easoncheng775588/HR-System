import React, { useCallback, useEffect, useMemo, useState } from 'react';
import { Button, Descriptions, Drawer, Input, message, Space, Table, Tabs, Tag, Timeline } from 'antd';
import { CheckOutlined, CloseOutlined, EyeOutlined } from '@ant-design/icons';
import api from '../utils/api';
import { useAuth } from '../contexts/AuthContext';
import { useParam } from '../contexts/ParamContext';

type TabKey = 'todo' | 'initiated' | 'processed';

interface WorkflowItem {
  requestId: number;
  processName: string;
  summary: string;
  currentNode?: string;
  applicant?: string;
  applicantDept?: string;
  arriveTime?: string;
  processStatus?: string;
  startTime?: string;
  applyTime?: string;
  handleTime?: string;
}

interface WorkflowDetailBody {
  request?: Record<string, unknown>;
  approvalHistory?: Array<Record<string, unknown>>;
  nodeConfigs?: Array<Record<string, unknown>>;
  processLogs?: Array<Record<string, unknown>>;
}

const PROCESS_NAME = '用人申请流程';

const formatTime = (val?: string) => (val ? new Date(val).toLocaleString('zh-CN') : '-');

const statusTag = (status?: string) => {
  if (status === '已通过') return <Tag color="success">已通过</Tag>;
  if (status === '已拒绝') return <Tag color="error">已拒绝</Tag>;
  return <Tag color="processing">处理中</Tag>;
};

const WorkflowCenter: React.FC = () => {
  const { user } = useAuth();
  const { getLevelText, getPlatformText } = useParam();

  const [activeTab, setActiveTab] = useState<TabKey>('todo');
  const [loading, setLoading] = useState(false);
  const [todoData, setTodoData] = useState<WorkflowItem[]>([]);
  const [initiatedData, setInitiatedData] = useState<WorkflowItem[]>([]);
  const [processedData, setProcessedData] = useState<WorkflowItem[]>([]);

  const [drawerVisible, setDrawerVisible] = useState(false);
  const [approveMode, setApproveMode] = useState(false);
  const [currentItem, setCurrentItem] = useState<WorkflowItem | null>(null);
  const [detail, setDetail] = useState<WorkflowDetailBody | null>(null);
  const [detailLoading, setDetailLoading] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [comment, setComment] = useState('');

  const userId = String(user?.userId || '1001');
  const userName = String(user?.realName || '系统用户');
  const userRole = String(user?.position || user?.positionName || user?.role || '');

  const fetchData = useCallback(async (tab: TabKey) => {
    setLoading(true);
    try {
      if (tab === 'todo') {
        const res = await api.get('/api/workflow-center/todo', { params: { userId, userRole } });
        if (res.data?.returnCode === 'SUC0000') setTodoData(res.data.body || []);
        else message.error(res.data?.errorMsg || '获取我的待办失败');
        return;
      }
      if (tab === 'initiated') {
        const res = await api.get('/api/workflow-center/initiated', { params: { userId } });
        if (res.data?.returnCode === 'SUC0000') setInitiatedData(res.data.body || []);
        else message.error(res.data?.errorMsg || '获取我发起的流程失败');
        return;
      }
      const res = await api.get('/api/workflow-center/processed', { params: { userId } });
      if (res.data?.returnCode === 'SUC0000') setProcessedData(res.data.body || []);
      else message.error(res.data?.errorMsg || '获取我处理的流程失败');
    } catch (_error) {
      message.error('加载流程数据失败，请稍后重试');
    } finally {
      setLoading(false);
    }
  }, [userId, userRole]);

  useEffect(() => {
    fetchData(activeTab);
  }, [activeTab, fetchData]);

  const openDetail = useCallback(async (item: WorkflowItem, canApprove: boolean) => {
    setCurrentItem(item);
    setApproveMode(canApprove);
    setComment('');
    setDrawerVisible(true);
    setDetailLoading(true);
    try {
      const res = await api.get(`/api/workflow-center/detail/${item.requestId}`, {
        params: { viewerId: userId, viewerName: userName, viewerRole: userRole },
      });
      if (res.data?.returnCode === 'SUC0000') setDetail(res.data.body || {});
      else message.error(res.data?.errorMsg || '获取流程详情失败');
    } catch (_error) {
      message.error('获取流程详情失败，请稍后重试');
    } finally {
      setDetailLoading(false);
    }
  }, [userId, userName, userRole]);

  const handleApprove = async (action: 'APPROVE' | 'REJECT') => {
    if (!currentItem) return;
    setSubmitting(true);
    try {
      const res = await api.post(`/api/workflow-center/${currentItem.requestId}/approve`, {
        action,
        approvalUserId: userId,
        approvalUserName: userName,
        approvalUserRole: userRole,
        approvalComment: comment || (action === 'APPROVE' ? '通过' : '拒绝'),
      });
      if (res.data?.returnCode === 'SUC0000') {
        message.success(action === 'APPROVE' ? '审批通过成功' : '审批拒绝成功');
        setDrawerVisible(false);
        setCurrentItem(null);
        await fetchData('todo');
      } else {
        message.error(res.data?.errorMsg || '审批失败');
      }
    } catch (_error) {
      message.error('审批失败，请稍后重试');
    } finally {
      setSubmitting(false);
    }
  };

  const commonOperateColumn = useCallback((canApprove: boolean) => ({
    title: '操作',
    key: 'action',
    width: canApprove ? 180 : 100,
    fixed: 'right' as const,
    render: (_: unknown, record: WorkflowItem) => (
      <Space size="small">
        <Button type="link" icon={<EyeOutlined />} onClick={() => openDetail(record, false)}>
          查看
        </Button>
        {canApprove && (
          <Button type="link" icon={<CheckOutlined />} onClick={() => openDetail(record, true)}>
            审批
          </Button>
        )}
      </Space>
    ),
  }), [openDetail]);

  const todoColumns = useMemo(
    () => [
      { title: '序号', key: 'index', width: 70, render: (_: unknown, _r: WorkflowItem, i: number) => i + 1 },
      { title: '流程名称', dataIndex: 'processName', key: 'processName', width: 140 },
      { title: '摘要', dataIndex: 'summary', key: 'summary', width: 280, ellipsis: true },
      { title: '当前环节', dataIndex: 'currentNode', key: 'currentNode', width: 120 },
      { title: '申请人', dataIndex: 'applicant', key: 'applicant', width: 120 },
      { title: '申请部门', dataIndex: 'applicantDept', key: 'applicantDept', width: 160 },
      { title: '到达时间', dataIndex: 'arriveTime', key: 'arriveTime', width: 180, render: (v: string) => formatTime(v) },
      commonOperateColumn(true),
    ],
    [commonOperateColumn],
  );

  const initiatedColumns = useMemo(
    () => [
      { title: '序号', key: 'index', width: 70, render: (_: unknown, _r: WorkflowItem, i: number) => i + 1 },
      { title: '流程名称', dataIndex: 'processName', key: 'processName', width: 140 },
      { title: '摘要', dataIndex: 'summary', key: 'summary', width: 280, ellipsis: true },
      { title: '当前环节', dataIndex: 'currentNode', key: 'currentNode', width: 120 },
      { title: '申请部门', dataIndex: 'applicantDept', key: 'applicantDept', width: 160 },
      { title: '流程状态', dataIndex: 'processStatus', key: 'processStatus', width: 120, render: (v: string) => statusTag(v) },
      { title: '发起时间', dataIndex: 'startTime', key: 'startTime', width: 180, render: (v: string) => formatTime(v) },
      commonOperateColumn(false),
    ],
    [commonOperateColumn],
  );

  const processedColumns = useMemo(
    () => [
      { title: '序号', key: 'index', width: 70, render: (_: unknown, _r: WorkflowItem, i: number) => i + 1 },
      { title: '流程名称', dataIndex: 'processName', key: 'processName', width: 140 },
      { title: '摘要', dataIndex: 'summary', key: 'summary', width: 280, ellipsis: true },
      { title: '申请人', dataIndex: 'applicant', key: 'applicant', width: 120 },
      { title: '申请部门', dataIndex: 'applicantDept', key: 'applicantDept', width: 160 },
      { title: '申请时间', dataIndex: 'applyTime', key: 'applyTime', width: 180, render: (v: string) => formatTime(v) },
      { title: '处理时间', dataIndex: 'handleTime', key: 'handleTime', width: 180, render: (v: string) => formatTime(v) },
      commonOperateColumn(false),
    ],
    [commonOperateColumn],
  );

  const request = (detail?.request || {}) as Record<string, unknown>;
  const history = (detail?.approvalHistory || []) as Array<Record<string, unknown>>;
  const logs = (detail?.processLogs || []) as Array<Record<string, unknown>>;

  return (
    <div style={{ width: '100%' }}>
      <Tabs
        activeKey={activeTab}
        onChange={(key) => setActiveTab(key as TabKey)}
        items={[
          {
            key: 'todo',
            label: '我的待办',
            children: (
              <div className="app-table-wrap">
                <Table rowKey="requestId" loading={loading} dataSource={todoData} columns={todoColumns} scroll={{ x: 1400 }} pagination={{ defaultPageSize: 10, showSizeChanger: true, pageSizeOptions: [10, 20, 30, 50, 100], showTotal: (total) => `共 ${total} 条记录` }} />
              </div>
            ),
          },
          {
            key: 'initiated',
            label: '我发起的流程',
            children: (
              <div className="app-table-wrap">
                <Table rowKey="requestId" loading={loading} dataSource={initiatedData} columns={initiatedColumns} scroll={{ x: 1400 }} pagination={{ defaultPageSize: 10, showSizeChanger: true, pageSizeOptions: [10, 20, 30, 50, 100], showTotal: (total) => `共 ${total} 条记录` }} />
              </div>
            ),
          },
          {
            key: 'processed',
            label: '我处理的流程',
            children: (
              <div className="app-table-wrap">
                <Table rowKey="requestId" loading={loading} dataSource={processedData} columns={processedColumns} scroll={{ x: 1400 }} pagination={{ defaultPageSize: 10, showSizeChanger: true, pageSizeOptions: [10, 20, 30, 50, 100], showTotal: (total) => `共 ${total} 条记录` }} />
              </div>
            ),
          },
        ]}
      />

      <Drawer
        title={approveMode ? '流程审批' : '流程详情'}
        placement="right"
        width={680}
        open={drawerVisible}
        onClose={() => {
          setDrawerVisible(false);
          setCurrentItem(null);
          setDetail(null);
          setApproveMode(false);
        }}
      >
        {detailLoading ? null : (
          <>
              <Descriptions title="申请信息" column={1} size="small">
              <Descriptions.Item label="流程名称">{PROCESS_NAME}</Descriptions.Item>
              <Descriptions.Item label="岗位标题">{String(request.requestTitle || '-')}</Descriptions.Item>
              <Descriptions.Item label="申请部门">{String(request.applicationDepartment || request.team || '-')}</Descriptions.Item>
              <Descriptions.Item label="技术平台">{getPlatformText(String(request.technicalPlatform || ''))}</Descriptions.Item>
              <Descriptions.Item label="建议级别">{getLevelText(String(request.proposedLevel || ''))}</Descriptions.Item>
              <Descriptions.Item label="补充人数">{String(request.supplementCount || '-')}</Descriptions.Item>
              <Descriptions.Item label="相关经验年限要求">{String(request.experienceYears || '-')}</Descriptions.Item>
              <Descriptions.Item label="任职要求">{String(request.skillRequirement || '-')}</Descriptions.Item>
              <Descriptions.Item label="岗位职责">{String(request.positionResponsibility || '-')}</Descriptions.Item>
            </Descriptions>

            <div style={{ marginTop: 16 }}>
              <div style={{ marginBottom: 8, fontWeight: 600 }}>审批轨迹</div>
              <Timeline
                items={history.map((h) => ({
                  children: `${formatTime(String(h.approvalTime || ''))} ${String(h.approverName || '-')} ${String(h.approvalStatus || '-')} ${String(h.approvalComment || '')}`,
                }))}
              />
            </div>

            <div style={{ marginTop: 16 }}>
              <div style={{ marginBottom: 8, fontWeight: 600 }}>流程日志</div>
              <Timeline
                items={logs.map((l) => ({
                  children: `${formatTime(String(l.actionTime || ''))} ${String(l.operatorName || '-')} ${String(l.actionType || '-')} ${String(l.actionResult || '-')}`,
                }))}
              />
            </div>

            {approveMode && (
              <div style={{ marginTop: 16 }}>
                <Input.TextArea
                  rows={3}
                  placeholder="请输入审批意见（选填）"
                  value={comment}
                  onChange={(e) => setComment(e.target.value)}
                />
                <Space style={{ marginTop: 12 }}>
                  <Button type="primary" icon={<CheckOutlined />} loading={submitting} onClick={() => handleApprove('APPROVE')}>
                    通过
                  </Button>
                  <Button danger icon={<CloseOutlined />} loading={submitting} onClick={() => handleApprove('REJECT')}>
                    拒绝
                  </Button>
                </Space>
              </div>
            )}
          </>
        )}
      </Drawer>
    </div>
  );
};

export default WorkflowCenter;

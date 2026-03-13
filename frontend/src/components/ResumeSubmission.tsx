import React, { useCallback, useEffect, useMemo, useState } from 'react';
import {
  Button,
  Card,
  DatePicker,
  Descriptions,
  Drawer,
  Form,
  Input,
  message,
  Modal,
  Popconfirm,
  Select,
  Space,
  Table,
  Tabs,
} from 'antd';
import {
  CheckCircleOutlined,
  CloseCircleOutlined,
  DeleteOutlined,
  EditOutlined,
  EyeOutlined,
  PlusOutlined,
  SendOutlined,
} from '@ant-design/icons';
import dayjs from 'dayjs';
import api from '../utils/api';
import { useAuth } from '../contexts/AuthContext';
import { useParam } from '../contexts/ParamContext';
import AttachmentUploader, { AttachmentItem } from './AttachmentUploader';
import ResumeConfirmDialog from './ResumeConfirmDialog';
import { submitInterviewerChoice } from '../services/interviewApi';
import { canAbandonInterviewChoice, canOpenResumeConfirmDialog } from '../utils/interviewPermission';
import {
  buildOperatorPayload,
  buildResumeListQuery,
  canCreateResume,
  canDispatchResume,
  canEditOrDeleteResume,
  canShowResumeTabs,
  canShowScreenActions,
  filterResumesByTab,
  INTERVIEWER_CHOICE,
  normalizeResumeRecord,
  RESUME_TAB,
  SCREEN_ACTION,
} from './resumeManagementHelpers';

const ATTACHMENT_SPLITTER = '||';
const DEGREE_OPTIONS = [
  { value: '大专', label: '大专' },
  { value: '本科', label: '本科' },
  { value: '硕士', label: '硕士' },
  { value: '其他同等学历水平', label: '其他同等学历水平' },
];
const DEFAULT_REQUIREMENT_OPTIONS = [
  { value: 'INIT_1', label: '系统研发岗' },
  { value: 'INIT_2', label: '测试' },
  { value: 'INIT_3', label: '项目助理' },
  { value: 'INIT_4', label: '行政' },
  { value: 'INIT_5', label: '人力' },
];
const RECRUITMENT_PLATFORM_OPTIONS = ['开放', '主机', '测试', 'T24', '其他'];
const RECRUITMENT_CATEGORY_OPTIONS = ['系统研发岗', '产品助理', '测试', '项目助理', '其他'];
const RECRUITMENT_LEVEL_OPTIONS = ['PT', 'PG', 'AP', 'ASA', 'SA', 'SSA', '初级行政', '中级行政', '高级行政'];
const ENGLISH_LEVEL_LABEL_MAP = {
  CET4: '四级',
  CET6: '六级',
  OTHER: '其他同等水平',
};

const ResumeSubmission = () => {
  const { user } = useAuth();
  const { getParamOptions } = useParam();

  const [loading, setLoading] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [resumes, setResumes] = useState([]);
  const [requirementOptions, setRequirementOptions] = useState([]);
  const [isMobile, setIsMobile] = useState(false);

  const [modalVisible, setModalVisible] = useState(false);
  const [viewVisible, setViewVisible] = useState(false);
  const [editingRecord, setEditingRecord] = useState(null);
  const [viewRecord, setViewRecord] = useState(null);
  const [attachmentFiles, setAttachmentFiles] = useState<AttachmentItem[]>([]);
  const [dispatchVisible, setDispatchVisible] = useState(false);
  const [dispatching, setDispatching] = useState(false);
  const [dispatchDemandOptions, setDispatchDemandOptions] = useState([]);
  const [dispatchResumeId, setDispatchResumeId] = useState<number | null>(null);
  const [confirmDialogVisible, setConfirmDialogVisible] = useState(false);
  const [confirmSubmitting, setConfirmSubmitting] = useState(false);
  const [confirmRecord, setConfirmRecord] = useState(null);

  const [form] = Form.useForm();
  const [dispatchForm] = Form.useForm();

  const toFallbackOptions = (values: string[]) => values.map((value) => ({ value, label: value }));
  const withFallbackOptions = (primaryOptions, fallbackValues: string[]) => {
    const normalized = (primaryOptions || []).filter((item) => item?.value && item?.label);
    return normalized.length > 0 ? normalized : toFallbackOptions(fallbackValues);
  };

  const platformOptions = withFallbackOptions(getParamOptions('PLATFORM'), RECRUITMENT_PLATFORM_OPTIONS);
  const categoryOptions = withFallbackOptions(getParamOptions('CATEGORY'), RECRUITMENT_CATEGORY_OPTIONS);
  const levelOptions = withFallbackOptions(getParamOptions('LEVEL'), RECRUITMENT_LEVEL_OPTIONS);
  const isSupplierHr = canCreateResume(user);
  const showStatusTabs = canShowResumeTabs(user);
  const [activeTab, setActiveTab] = useState(RESUME_TAB.ALL);

  const requirementMap = useMemo(() => {
    const map = new Map();
    requirementOptions.forEach((item) => map.set(String(item.value), String(item.label)));
    return map;
  }, [requirementOptions]);

  const fetchResumes = useCallback(async () => {
    setLoading(true);
    try {
      const response = await api.get('/api/resume/list', {
        params: buildResumeListQuery(user),
      });
      if (response.data?.returnCode === 'SUC0000') {
        setResumes((response.data.body || []).map((item) => normalizeResumeRecord(item)));
      } else {
        message.error(response.data?.errorMsg || '获取简历列表失败');
      }
    } catch (error) {
      message.error(`获取简历列表失败: ${error?.message || '未知错误'}`);
    } finally {
      setLoading(false);
    }
  }, [user]);

  useEffect(() => {
    const onResize = () => setIsMobile(window.innerWidth < 768);
    onResize();
    window.addEventListener('resize', onResize);
    fetchResumes();
    fetchRequirementOptions();
    return () => window.removeEventListener('resize', onResize);
  }, [fetchResumes]);

  const fetchRequirementOptions = async () => {
    try {
      const response = await api.get('/api/resume/requirement-options');
      if (response.data?.returnCode === 'SUC0000') {
        const list = (response.data.body || [])
          .map((item) => ({
            value: String(item?.value ?? ''),
            label: String(item?.label ?? ''),
          }))
          .filter((item) => item.value && item.label);
        const labelSet = new Set(list.map((item) => item.label));
        const merged = [
          ...list,
          ...DEFAULT_REQUIREMENT_OPTIONS.filter((item) => !labelSet.has(item.label)),
        ];
        setRequirementOptions(merged);
        return;
      }
    } catch (_error) {
      // ignore and fallback
    }
    setRequirementOptions(DEFAULT_REQUIREMENT_OPTIONS);
  };

  const parseAttachments = (record) => {
    const splitValues = (value) => {
      const text = String(value || '');
      if (!text) return [];
      if (text.includes(ATTACHMENT_SPLITTER)) {
        return text.split(ATTACHMENT_SPLITTER).map((item) => item.trim()).filter(Boolean);
      }
      return text.split(',').map((item) => item.trim()).filter(Boolean);
    };

    const normalizeUrl = (url) => {
      const raw = String(url || '').trim();
      if (!raw) return '';
      if (raw.startsWith('http://') || raw.startsWith('https://')) return raw;
      if (raw.startsWith('/api/uploads/')) return raw;
      if (raw.startsWith('/uploads/')) return `/api${raw}`;
      if (raw.startsWith('uploads/')) return `/api/${raw}`;
      return `/api/uploads/${raw}`;
    };

    const names = splitValues(record?.attachmentNames);
    const urls = splitValues(record?.attachmentUrls);
    const size = Math.max(names.length, urls.length);
    return Array.from({ length: size }, (_, index) => {
      const resolvedUrl = normalizeUrl(urls[index] || '');
      const fallbackName = resolvedUrl ? decodeURIComponent(resolvedUrl.split('/').pop() || '') : '';
      return {
        uid: `local-${index}`,
        fileName: names[index] || fallbackName || `附件${index + 1}`,
        status: 'done',
        fileUrl: resolvedUrl,
        fileKey: '',
      };
    }).filter((item) => item.fileUrl || item.fileName);
  };

  const resolveRelatedRequestNames = (record) => {
    const text = String(record?.relatedRequestNames || '').trim();
    if (text && !/^\d+(,\d+)*$/.test(text)) {
      return text;
    }

    const ids = String(record?.relatedRequestIds || text)
      .split(',')
      .map((item) => item.trim())
      .filter(Boolean);
    if (!ids.length) {
      return '-';
    }
    const labels = ids.map((id) => requirementMap.get(String(id)) || id);
    return labels.join(', ');
  };

  const openCreateModal = () => {
    setEditingRecord(null);
    setAttachmentFiles([]);
    form.resetFields();
    setModalVisible(true);
  };

  const openEditModal = async (record) => {
    try {
      const response = await api.get(`/api/resume/${record.resumeId}`);
      const fullRecord =
        response.data?.returnCode === 'SUC0000' && response.data?.body ? response.data.body : record;
      const normalizedRecord = normalizeResumeRecord(fullRecord);

      setEditingRecord(normalizedRecord);
      setAttachmentFiles(parseAttachments(normalizedRecord));
      form.setFieldsValue({
        ...normalizedRecord,
        relatedRequestIds: (normalizedRecord.relatedRequestIds || '').split(',').filter(Boolean),
        interviewAvailableTime:
          normalizedRecord.interviewAvailableStartTime && normalizedRecord.interviewAvailableEndTime
            ? [dayjs(normalizedRecord.interviewAvailableStartTime), dayjs(normalizedRecord.interviewAvailableEndTime)]
            : undefined,
        onboardDate: normalizedRecord.onboardDate ? dayjs(normalizedRecord.onboardDate) : undefined,
      });
      setModalVisible(true);
    } catch (error) {
      message.error(`获取简历详情失败: ${error?.message || '未知错误'}`);
    }
  };

  const openViewDrawer = async (record) => {
    try {
      const response = await api.get(`/api/resume/${record.resumeId}`);
      const fullRecord =
        response.data?.returnCode === 'SUC0000' && response.data?.body ? response.data.body : record;
      setViewRecord(normalizeResumeRecord(fullRecord));
      setViewVisible(true);
    } catch (error) {
      message.error(`获取简历详情失败: ${error?.message || '未知错误'}`);
    }
  };

  const handleDelete = async (resumeId) => {
    try {
      const response = await api.delete(`/api/resume/${resumeId}`, {
        params: {
          operatorUserId: String(user?.userId || ''),
          operatorRole: String(user?.position || user?.positionName || user?.role || ''),
        },
      });
      if (response.data?.returnCode === 'SUC0000') {
        message.success('删除成功');
        fetchResumes();
      } else {
        message.error(response.data?.errorMsg || '删除失败');
      }
    } catch (error) {
      message.error(`删除失败: ${error?.message || '未知错误'}`);
    }
  };

  const handleScreen = async (record, action) => {
    try {
      const response = await api.post(`/api/resume/${record.resumeId}/screen`, {
        action,
        ...buildOperatorPayload(user),
      });
      if (response.data?.returnCode === 'SUC0000') {
        message.success('操作成功');
        await fetchResumes();
      } else {
        message.error(response.data?.errorMsg || '操作失败');
      }
    } catch (error) {
      message.error(`操作失败: ${error?.message || '未知错误'}`);
    }
  };

  const loadDispatchDemandOptions = async () => {
    try {
      const response = await api.get('/api/resume/dispatch-demand-options', {
        params: {
          operatorUserId: String(user?.userId || ''),
          operatorRole: String(user?.position || user?.positionName || user?.role || ''),
        },
      });
      if (response.data?.returnCode === 'SUC0000') {
        setDispatchDemandOptions(response.data.body || []);
      } else {
        message.error(response.data?.errorMsg || '获取关联需求失败');
      }
    } catch (error) {
      message.error(`获取关联需求失败: ${error?.message || '未知错误'}`);
    }
  };

  const openDispatchModal = async (record) => {
    setDispatchResumeId(record.resumeId);
    dispatchForm.resetFields();
    setDispatchVisible(true);
    await loadDispatchDemandOptions();
  };

  const submitDispatch = async () => {
    try {
      const values = await dispatchForm.validateFields();
      setDispatching(true);
      const response = await api.post(`/api/resume/${dispatchResumeId}/dispatch`, {
        demandIds: values.demandIds || [],
        ...buildOperatorPayload(user),
      });
      if (response.data?.returnCode === 'SUC0000') {
        message.success('分发成功');
        setDispatchVisible(false);
        dispatchForm.resetFields();
        await fetchResumes();
      } else {
        message.error(response.data?.errorMsg || '分发失败');
      }
    } catch (error) {
      if (!error?.errorFields) {
        message.error(`分发失败: ${error?.message || '未知错误'}`);
      }
    } finally {
      setDispatching(false);
    }
  };

  const openResumeConfirmDialog = (record) => {
    setConfirmRecord(record);
    setConfirmDialogVisible(true);
  };

  const submitResumeConfirm = async (payload) => {
    if (!confirmRecord?.resumeId) return;
    try {
      setConfirmSubmitting(true);
      const response = await submitInterviewerChoice(confirmRecord.resumeId, {
        choice: INTERVIEWER_CHOICE.CONFIRM,
        ...buildOperatorPayload(user),
        ...payload,
      });
      if (response.data?.returnCode === 'SUC0000') {
        message.success('操作成功');
        setConfirmDialogVisible(false);
        setConfirmRecord(null);
        await fetchResumes();
      } else {
        message.error(response.data?.errorMsg || '操作失败');
      }
    } catch (error) {
      message.error(`操作失败: ${error?.message || '未知错误'}`);
    } finally {
      setConfirmSubmitting(false);
    }
  };

  const handleAbandonChoice = async (record) => {
    try {
      const response = await submitInterviewerChoice(record.resumeId, {
        choice: INTERVIEWER_CHOICE.ABANDON,
        ...buildOperatorPayload(user),
      });
      if (response.data?.returnCode === 'SUC0000') {
        message.success('操作成功');
        await fetchResumes();
      } else {
        message.error(response.data?.errorMsg || '操作失败');
      }
    } catch (error) {
      message.error(`操作失败: ${error?.message || '未知错误'}`);
    }
  };

  const handleSubmit = async () => {
    if (!editingRecord && !isSupplierHr) {
      message.error('仅供应商HR可提交简历');
      return;
    }
    try {
      const values = await form.validateFields();
      const uploadingFiles = attachmentFiles.filter((file) => file.status === 'uploading');
      if (uploadingFiles.length > 0) {
        message.warning('附件正在上传，请稍后再提交');
        return;
      }

      const relatedIds = values.relatedRequestIds || [];
      const relatedNames = relatedIds.map((id) => requirementMap.get(String(id))).filter(Boolean);

      const doneFiles = attachmentFiles.filter((f) => f.status === 'done' || !f.status);
      const normalizeSubmitUrl = (file) => {
        const rawUrl = String(file.fileUrl || '').trim();
        if (!rawUrl) return '';
        if (rawUrl.startsWith('/api/uploads/')) return rawUrl.replace('/api', '');
        if (rawUrl.startsWith('/uploads/')) return rawUrl;
        if (rawUrl.startsWith('http://') || rawUrl.startsWith('https://')) {
          const marker = '/uploads/';
          const markerIndex = rawUrl.indexOf(marker);
          return markerIndex >= 0 ? rawUrl.substring(markerIndex) : rawUrl;
        }
        if (rawUrl.startsWith('uploads/')) return `/${rawUrl}`;
        return `/uploads/${rawUrl}`;
      };
      const normalizedFiles = doneFiles
        .map((file) => ({ name: String(file.fileName || '').trim(), url: normalizeSubmitUrl(file) }))
        .filter((file) => file.name || file.url);
      const attachmentNames = normalizedFiles.map((f) => f.name).join(ATTACHMENT_SPLITTER);
      const attachmentUrls = normalizedFiles.map((f) => f.url).join(ATTACHMENT_SPLITTER);

      const payload = {
        ...values,
        relatedRequestIds: relatedIds.join(','),
        relatedRequestNames: relatedNames.join(','),
        candidateName: values.candidateName,
        applicantName: values.candidateName,
        recruitmentRequestId:
          relatedIds.length && !Number.isNaN(Number(relatedIds[0])) ? Number(relatedIds[0]) : null,
        interviewAvailableStartTime: values.interviewAvailableTime?.[0]
          ? values.interviewAvailableTime[0].format('YYYY-MM-DD HH:mm:ss')
          : '',
        interviewAvailableEndTime: values.interviewAvailableTime?.[1]
          ? values.interviewAvailableTime[1].format('YYYY-MM-DD HH:mm:ss')
          : '',
        onboardDate: values.onboardDate ? values.onboardDate.format('YYYY-MM-DD') : '',
        attachmentNames,
        attachmentUrls,
        createUserId: user?.userId ? String(user.userId) : '1001',
        createUserName: user?.realName || '系统用户',
        updateUserId: user?.userId ? String(user.userId) : '1001',
        updateUserName: user?.realName || '系统用户',
      };

      delete payload.interviewAvailableTime;

      setSubmitting(true);
      const response = editingRecord
        ? await api.put(`/api/resume/${editingRecord.resumeId}`, payload)
        : await api.post('/api/resume/submit', payload);

      if (response.data?.returnCode === 'SUC0000') {
        message.success(editingRecord ? '更新成功' : '新增成功');
        setModalVisible(false);
        form.resetFields();
        setAttachmentFiles([]);
        fetchResumes();
      } else {
        message.error(response.data?.errorMsg || '保存失败');
      }
    } catch (error) {
      if (!error?.errorFields) {
        message.error(`保存失败: ${error?.message || '未知错误'}`);
      }
    } finally {
      setSubmitting(false);
    }
  };

  const columns = [
    {
      title: '关联需求',
      dataIndex: 'relatedRequestNames',
      key: 'relatedRequestNames',
      width: 220,
      ellipsis: true,
      render: (_, record) => resolveRelatedRequestNames(record),
    },
    {
      title: '候选人',
      dataIndex: 'candidateName',
      key: 'candidateName',
      width: 120,
    },
    {
      title: '性别',
      dataIndex: 'gender',
      key: 'gender',
      width: 80,
      render: (v) => (v === 'MALE' ? '男' : v === 'FEMALE' ? '女' : '-'),
    },
    {
      title: '第一学历毕业专业',
      dataIndex: 'firstDegreeMajor',
      key: 'firstDegreeMajor',
      width: 180,
      ellipsis: true,
    },
    {
      title: '第一学历毕业院校',
      dataIndex: 'firstDegreeSchool',
      key: 'firstDegreeSchool',
      width: 220,
      ellipsis: true,
    },
    {
      title: '可面试时间',
      key: 'interviewAvailableTime',
      width: 260,
      render: (_, record) => {
        const start = record.interviewAvailableStartTime || '-';
        const end = record.interviewAvailableEndTime || '-';
        return `${start} ~ ${end}`;
      },
    },
    {
      title: '供应商名称',
      dataIndex: 'supplierName',
      key: 'supplierName',
      width: 180,
      render: (v) => v || '-',
    },
    {
      title: '供应商推荐日期',
      dataIndex: 'supplierRecommendDate',
      key: 'supplierRecommendDate',
      width: 180,
      render: (v) => (v ? dayjs(v).format('YYYY-MM-DD HH:mm:ss') : '-'),
    },
    {
      title: '简历状态',
      dataIndex: 'status',
      key: 'status',
      width: 160,
      render: (value) => value || '-',
    },
    {
      title: '面试官选择状态',
      dataIndex: 'currentInterviewerChoiceStatus',
      key: 'currentInterviewerChoiceStatus',
      width: 140,
      render: (value) => value || '-',
    },
    {
      title: '已确认面试官',
      dataIndex: 'confirmedInterviewerName',
      key: 'confirmedInterviewerName',
      width: 140,
      render: (value) => value || '-',
    },
    {
      title: '操作',
      key: 'action',
      width: 260,
      fixed: 'right',
      render: (_, record) => (
        <Space size={4} wrap>
          <Button size="small" type="link" icon={<EyeOutlined />} onClick={() => openViewDrawer(record)}>
            查看
          </Button>
          {canEditOrDeleteResume(user, record) && (
            <Button size="small" type="link" icon={<EditOutlined />} onClick={() => openEditModal(record)}>
              编辑
            </Button>
          )}
          {canEditOrDeleteResume(user, record) && (
            <Popconfirm title="确认删除该简历吗？" onConfirm={() => handleDelete(record.resumeId)}>
              <Button size="small" type="link" danger icon={<DeleteOutlined />}>
                删除
              </Button>
            </Popconfirm>
          )}
          {canShowScreenActions(user, record) && (
            <>
              <Button size="small" type="link" onClick={() => handleScreen(record, SCREEN_ACTION.PASS)}>
                通过
              </Button>
              <Button size="small" type="link" onClick={() => handleScreen(record, SCREEN_ACTION.MATERIAL_EDIT)}>
                材料需修改
              </Button>
              <Button size="small" type="link" danger onClick={() => handleScreen(record, SCREEN_ACTION.REJECT)}>
                不通过
              </Button>
            </>
          )}
          {canDispatchResume(user, record) && (
            <Button size="small" type="link" icon={<SendOutlined />} onClick={() => openDispatchModal(record)}>
              分发
            </Button>
          )}
          {canOpenResumeConfirmDialog(user, record) && (
            <Button
              size="small"
              type="link"
              icon={<CheckCircleOutlined />}
              onClick={() => openResumeConfirmDialog(record)}
            >
              确认选择
            </Button>
          )}
          {canAbandonInterviewChoice(user, record) && (
            <Button
              size="small"
              type="link"
              icon={<CloseCircleOutlined />}
              onClick={() => handleAbandonChoice(record)}
            >
              放弃选择
            </Button>
          )}
        </Space>
      ),
    },
  ];

  const filteredResumes = useMemo(
    () => filterResumesByTab(resumes, activeTab),
    [resumes, activeTab],
  );

  const statusTabItems = [
    { key: RESUME_TAB.ALL, label: '全部' },
    { key: RESUME_TAB.PENDING_REVIEW, label: '待审核' },
    { key: RESUME_TAB.PASSED, label: '通过' },
    { key: RESUME_TAB.NEED_MATERIAL_EDIT, label: '需修改材料' },
    { key: RESUME_TAB.REJECTED, label: '未通过' },
  ];

  return (
    <div className="app-page">
      {isSupplierHr && (
        <div style={{ marginBottom: 16 }}>
          <Button type="primary" icon={<PlusOutlined />} onClick={openCreateModal}>
            新增简历
          </Button>
        </div>
      )}

      <Card title="简历管理">
        {showStatusTabs && (
          <Tabs
            activeKey={activeTab}
            items={statusTabItems}
            onChange={setActiveTab}
            style={{ marginBottom: 12 }}
          />
        )}
        <div className="app-table-wrap">
          <Table
            rowKey="resumeId"
            loading={loading}
            dataSource={filteredResumes}
            columns={columns}
            scroll={{ x: isMobile ? 1400 : 2400 }}
            pagination={{
              defaultPageSize: 10,
              showSizeChanger: true,
              pageSizeOptions: [10, 20, 30, 50, 100],
              showTotal: (total) => `共 ${total} 条记录`,
            }}
          />
        </div>
      </Card>

      <Modal
        title={editingRecord ? '编辑简历' : '新增简历'}
        open={modalVisible}
        onOk={handleSubmit}
        onCancel={() => {
          setModalVisible(false);
          form.resetFields();
          setAttachmentFiles([]);
        }}
        confirmLoading={submitting}
        width={isMobile ? '96%' : 980}
      >
        <Form form={form} layout="vertical">
          <Form.Item label="关联需求" name="relatedRequestIds" rules={[{ required: true, message: '请选择关联需求' }]}>
            <Select
              mode="multiple"
              allowClear
              placeholder="请选择关联需求"
              options={requirementOptions}
              optionFilterProp="label"
              showSearch
            />
          </Form.Item>

          <div style={{ display: 'grid', gridTemplateColumns: isMobile ? '1fr' : '1fr 1fr', gap: 12 }}>
            <Form.Item label="候选人" name="candidateName" rules={[{ required: true, message: '请输入候选人' }]}><Input /></Form.Item>
            <Form.Item label="性别" name="gender" rules={[{ required: true, message: '请选择性别' }]}>
              <Select options={[{ value: 'MALE', label: '男' }, { value: 'FEMALE', label: '女' }]} />
            </Form.Item>
            <Form.Item label="出生年月日" name="birthDate"><Input placeholder="YYYY-MM-DD" /></Form.Item>
            <Form.Item label="第一学历" name="firstDegree">
              <Select options={DEGREE_OPTIONS} allowClear />
            </Form.Item>
            <Form.Item label="第一学历毕业年份" name="firstDegreeGraduateYear"><Input /></Form.Item>
            <Form.Item label="第一学历毕业院校" name="firstDegreeSchool"><Input /></Form.Item>
            <Form.Item label="第一学历毕业专业" name="firstDegreeMajor"><Input /></Form.Item>
            <Form.Item label="第一学历是否全日制" name="firstDegreeFullTime">
              <Select options={[{ value: 'YES', label: '是' }, { value: 'NO', label: '否' }]} />
            </Form.Item>
            <Form.Item label="最高学历" name="highestDegree">
              <Select options={DEGREE_OPTIONS} allowClear />
            </Form.Item>
            <Form.Item label="最高学历毕业专业" name="highestDegreeMajor"><Input /></Form.Item>
            <Form.Item label="最高学历毕业年份" name="highestDegreeGraduateYear"><Input /></Form.Item>
            <Form.Item label="最高学历毕业院校" name="highestDegreeSchool"><Input /></Form.Item>
            <Form.Item label="最高学历是否全日制" name="highestDegreeFullTime">
              <Select options={[{ value: 'YES', label: '是' }, { value: 'NO', label: '否' }]} />
            </Form.Item>
            <Form.Item label="英语水平" name="englishLevel">
              <Select options={[{ value: 'CET4', label: '四级' }, { value: 'CET6', label: '六级' }, { value: 'OTHER', label: '其他同等水平' }]} />
            </Form.Item>
            <Form.Item label="候选人技术平台" name="candidatePlatform">
              <Select options={platformOptions} allowClear />
            </Form.Item>
            <Form.Item label="申请岗位" name="appliedCategory">
              <Select options={categoryOptions} allowClear />
            </Form.Item>
            <Form.Item label="申请职级" name="appliedLevel">
              <Select options={levelOptions} allowClear />
            </Form.Item>
            <Form.Item label="IT工作年限" name="itWorkYears"><Input /></Form.Item>
            <Form.Item label="IT实习年限" name="itInternshipYears"><Input /></Form.Item>
            <Form.Item label="最近服务的公司名称" name="latestCompany"><Input /></Form.Item>
            <Form.Item label="候选人是否在深圳" name="inShenzhen">
              <Select options={[{ value: 'YES', label: '是' }, { value: 'NO', label: '否' }]} />
            </Form.Item>
            <Form.Item label="可到岗时间" name="onboardDate"><DatePicker style={{ width: '100%' }} /></Form.Item>
            <Form.Item label="供应商是否已初面" name="supplierInitialInterview">
              <Select options={[{ value: 'YES', label: '是' }, { value: 'NO', label: '否' }]} />
            </Form.Item>
            <Form.Item label="笔试成绩" name="writtenTestScore"><Input /></Form.Item>
          </div>

          <Form.Item label="可参加面试时间" name="interviewAvailableTime" rules={[{ required: true, message: '请选择可参加面试时间段' }]}>
            <DatePicker.RangePicker showTime style={{ width: '100%' }} />
          </Form.Item>

          <Form.Item label="供应商初面意见" name="supplierInterviewComment"><Input.TextArea rows={3} /></Form.Item>

          <Form.Item label="附件">
            <AttachmentUploader businessType="resume" value={attachmentFiles} onChange={setAttachmentFiles} />
            <div style={{ marginTop: 8, color: '#666' }}>支持 zip、Excel、Word、PDF、PPT</div>
          </Form.Item>

          <Form.Item label="备注" name="remark"><Input.TextArea rows={3} /></Form.Item>
        </Form>
      </Modal>

      <Modal
        title="分发简历"
        open={dispatchVisible}
        onCancel={() => {
          setDispatchVisible(false);
          dispatchForm.resetFields();
        }}
        onOk={submitDispatch}
        confirmLoading={dispatching}
      >
        <Form form={dispatchForm} layout="vertical">
          <Form.Item name="demandIds" label="关联需求" rules={[{ required: true, message: '请选择关联需求' }]}>
            <Select
              mode="multiple"
              placeholder="请选择关联需求"
              options={dispatchDemandOptions.map((item) => ({
                label: item.label,
                value: item.demandId,
              }))}
            />
          </Form.Item>
        </Form>
      </Modal>

      <ResumeConfirmDialog
        open={confirmDialogVisible}
        submitting={confirmSubmitting}
        onCancel={() => {
          setConfirmDialogVisible(false);
          setConfirmRecord(null);
        }}
        onSubmit={submitResumeConfirm}
      />

      <Drawer title="简历详情" width={isMobile ? '100%' : 680} open={viewVisible} onClose={() => setViewVisible(false)}>
        {viewRecord && (
          <Descriptions column={1} size="small">
            <Descriptions.Item label="关联需求">{resolveRelatedRequestNames(viewRecord)}</Descriptions.Item>
            <Descriptions.Item label="候选人">{viewRecord.candidateName || '-'}</Descriptions.Item>
            <Descriptions.Item label="性别">{viewRecord.gender === 'MALE' ? '男' : viewRecord.gender === 'FEMALE' ? '女' : '-'}</Descriptions.Item>
            <Descriptions.Item label="出生年月日">{viewRecord.birthDate || '-'}</Descriptions.Item>
            <Descriptions.Item label="第一学历">{viewRecord.firstDegree || '-'}</Descriptions.Item>
            <Descriptions.Item label="第一学历毕业年份">{viewRecord.firstDegreeGraduateYear || '-'}</Descriptions.Item>
            <Descriptions.Item label="第一学历毕业专业">{viewRecord.firstDegreeMajor || '-'}</Descriptions.Item>
            <Descriptions.Item label="第一学历毕业院校">{viewRecord.firstDegreeSchool || '-'}</Descriptions.Item>
            <Descriptions.Item label="第一学历是否全日制">{viewRecord.firstDegreeFullTime === 'YES' ? '是' : viewRecord.firstDegreeFullTime === 'NO' ? '否' : '-'}</Descriptions.Item>
            <Descriptions.Item label="最高学历">{viewRecord.highestDegree || '-'}</Descriptions.Item>
            <Descriptions.Item label="最高学历毕业专业">{viewRecord.highestDegreeMajor || '-'}</Descriptions.Item>
            <Descriptions.Item label="最高学历毕业年份">{viewRecord.highestDegreeGraduateYear || '-'}</Descriptions.Item>
            <Descriptions.Item label="最高学历毕业院校">{viewRecord.highestDegreeSchool || '-'}</Descriptions.Item>
            <Descriptions.Item label="最高学历是否全日制">{viewRecord.highestDegreeFullTime === 'YES' ? '是' : viewRecord.highestDegreeFullTime === 'NO' ? '否' : '-'}</Descriptions.Item>
            <Descriptions.Item label="英语水平">
              {ENGLISH_LEVEL_LABEL_MAP[viewRecord.englishLevel] || viewRecord.englishLevel || '-'}
            </Descriptions.Item>
            <Descriptions.Item label="候选人技术平台">{viewRecord.candidatePlatform || '-'}</Descriptions.Item>
            <Descriptions.Item label="申请岗位">{viewRecord.appliedCategory || '-'}</Descriptions.Item>
            <Descriptions.Item label="申请职级">{viewRecord.appliedLevel || '-'}</Descriptions.Item>
            <Descriptions.Item label="IT工作年限">{viewRecord.itWorkYears || '-'}</Descriptions.Item>
            <Descriptions.Item label="IT实习年限">{viewRecord.itInternshipYears || '-'}</Descriptions.Item>
            <Descriptions.Item label="最近服务的公司名称">{viewRecord.latestCompany || '-'}</Descriptions.Item>
            <Descriptions.Item label="可面试时间">{`${viewRecord.interviewAvailableStartTime || '-'} ~ ${viewRecord.interviewAvailableEndTime || '-'}`}</Descriptions.Item>
            <Descriptions.Item label="候选人是否在深圳">{viewRecord.inShenzhen === 'YES' ? '是' : viewRecord.inShenzhen === 'NO' ? '否' : '-'}</Descriptions.Item>
            <Descriptions.Item label="可到岗时间">{viewRecord.onboardDate || '-'}</Descriptions.Item>
            <Descriptions.Item label="供应商是否已初面">{viewRecord.supplierInitialInterview === 'YES' ? '是' : viewRecord.supplierInitialInterview === 'NO' ? '否' : '-'}</Descriptions.Item>
            <Descriptions.Item label="笔试成绩">{viewRecord.writtenTestScore || '-'}</Descriptions.Item>
            <Descriptions.Item label="供应商初面意见">{viewRecord.supplierInterviewComment || '-'}</Descriptions.Item>
            <Descriptions.Item label="供应商名称">{viewRecord.supplierName || '-'}</Descriptions.Item>
            <Descriptions.Item label="供应商推荐日期">{viewRecord.supplierRecommendDate ? dayjs(viewRecord.supplierRecommendDate).format('YYYY-MM-DD HH:mm:ss') : '-'}</Descriptions.Item>
            <Descriptions.Item label="附件">
              {parseAttachments(viewRecord).length > 0 ? (
                <Space direction="vertical" size={4}>
                  {parseAttachments(viewRecord).map((file) => (
                    <a key={file.uid} href={file.fileUrl} target="_blank" rel="noreferrer">
                      {file.fileName}
                    </a>
                  ))}
                </Space>
              ) : (
                '-'
              )}
            </Descriptions.Item>
            <Descriptions.Item label="备注">{viewRecord.remark || '-'}</Descriptions.Item>
            <Descriptions.Item label="创建人">{viewRecord.createUserName || '-'}</Descriptions.Item>
            <Descriptions.Item label="创建时间">{viewRecord.createTime ? dayjs(viewRecord.createTime).format('YYYY-MM-DD HH:mm:ss') : '-'}</Descriptions.Item>
            <Descriptions.Item label="修改人">{viewRecord.updateUserName || '-'}</Descriptions.Item>
            <Descriptions.Item label="修改时间">{viewRecord.updateTime ? dayjs(viewRecord.updateTime).format('YYYY-MM-DD HH:mm:ss') : '-'}</Descriptions.Item>
          </Descriptions>
        )}
      </Drawer>
    </div>
  );
};

export default ResumeSubmission;

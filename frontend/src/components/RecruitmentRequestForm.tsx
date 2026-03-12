import React, { useCallback, useEffect, useRef, useState } from 'react';
import { Button, Card, Form, Input, InputNumber, Radio, Select, Space, Spin, message } from 'antd';
import { useNavigate, useParams } from 'react-router-dom';
import api from '../utils/api';
import { handleLoadError, handleSaveDraftError, handleSubmitError } from '../utils/errorHandler';
import { useAuth } from '../contexts/AuthContext';
import {
  buildRecruitmentRequestPayload,
  getOrgUnitName,
  normalizeRecruitmentRequestFormValues,
  REQUEST_TYPE_OPTIONS,
  TEXTAREA_MAX_LENGTH,
} from './recruitmentRequestHelpers';

type InterviewerOption = {
  value: string;
  label: string;
};

const CATEGORY_OPTIONS = ['系统研发岗', '产品助理', '测试', '项目助理', '其他'];
const PLATFORM_OPTIONS = ['开放', '主机', '测试', 'T24', '其他'];
const LEVEL_OPTIONS = ['PT', 'PG', 'AP', 'ASA', 'SA', 'SSA', '初级行政', '中级行政', '高级行政'];

const RecruitmentRequestForm = () => {
  const [form] = Form.useForm();
  const navigate = useNavigate();
  const { id } = useParams();
  const { user } = useAuth();

  const [isSubmitting, setIsSubmitting] = useState(false);
  const [loading, setLoading] = useState(false);
  const [isEditMode, setIsEditMode] = useState(false);
  const [interviewerOptions, setInterviewerOptions] = useState<InterviewerOption[]>([]);
  const interviewerSearchTimer = useRef<number | null>(null);

  const userOrgUnitName = getOrgUnitName(user || {});

  const fetchStaffingByOrgUnit = useCallback(async (orgUnitName: string) => {
    if (!orgUnitName) {
      form.setFieldsValue({ totalRecruitmentCount: 0, vacancyCount: 0 });
      return;
    }

    try {
      const response = await api.get('/api/staffings/by-org-unit', { params: { orgUnitName } });
      if (response.data?.returnCode === 'SUC0000' && response.data.body) {
        const staffing = response.data.body;
        form.setFieldsValue({
          totalRecruitmentCount: Number(staffing.totalHeadcount || 0),
          vacancyCount: Number(staffing.vacancyHeadcount || 0),
        });
      } else {
        form.setFieldsValue({ totalRecruitmentCount: 0, vacancyCount: 0 });
      }
    } catch (_error) {
      form.setFieldsValue({ totalRecruitmentCount: 0, vacancyCount: 0 });
    }
  }, [form]);

  const loadRequestData = useCallback(async (requestId: string) => {
    setLoading(true);
    try {
      const response = await api.get(`/api/recruitment-request/${requestId}`);
      if (response.data?.returnCode === 'SUC0000') {
        const requestData = response.data.body || {};
        form.setFieldsValue(normalizeRecruitmentRequestFormValues(requestData, user || {}));
        if (requestData.interviewerId && requestData.interviewerName) {
          setInterviewerOptions((prev) => {
            const exists = prev.some((item) => item.value === requestData.interviewerId);
            if (exists) {
              return prev;
            }
            return [
              {
                value: requestData.interviewerId,
                label: `${requestData.interviewerName} (${requestData.interviewerId})`,
              },
              ...prev,
            ];
          });
        }
      } else {
        message.error(`加载数据失败：${response.data?.errorMsg || '未知错误'}`);
        navigate('/recruitment-request');
      }
    } catch (error) {
      handleLoadError(error, navigate);
    } finally {
      setLoading(false);
    }
  }, [form, navigate, user]);

  useEffect(() => {
    if (id) {
      setIsEditMode(true);
      loadRequestData(id);
      return;
    }
    form.setFieldsValue(normalizeRecruitmentRequestFormValues({}, user || {}));
  }, [id, loadRequestData, form, user]);

  useEffect(() => {
    if (id) {
      return;
    }
    fetchStaffingByOrgUnit(userOrgUnitName);
  }, [fetchStaffingByOrgUnit, id, userOrgUnitName]);

  const loadInterviewers = useCallback(async (keyword: string) => {
    try {
      const response = await api.get('/api/users/search', { params: { keyword } });
      if (response.data?.returnCode !== 'SUC0000') {
        return;
      }
      const options = (response.data.body || [])
        .map((item: { userId?: string; realName?: string; username?: string }) => {
          const displayName = item.realName || item.username || item.userId || '';
          return {
            value: String(item.userId || ''),
            label: `${displayName} (${item.userId || '-'})`,
          };
        })
        .filter((item: InterviewerOption) => item.value);
      setInterviewerOptions(options);
    } catch (_error) {
      setInterviewerOptions([]);
    }
  }, []);

  const handleInterviewerSearch = (keyword: string) => {
    if (interviewerSearchTimer.current) {
      window.clearTimeout(interviewerSearchTimer.current);
    }
    interviewerSearchTimer.current = window.setTimeout(() => {
      loadInterviewers(keyword);
    }, 250);
  };

  const handleInterviewerChange = (_value: string, option: unknown) => {
    const currentOption = option as { label?: string };
    const labelText = currentOption?.label || '';
    const name = labelText.includes('(') ? labelText.split('(')[0].trim() : labelText;
    form.setFieldsValue({ interviewerName: name });
  };

  useEffect(() => () => {
    if (interviewerSearchTimer.current) {
      window.clearTimeout(interviewerSearchTimer.current);
    }
  }, []);

  const handleSubmit = async (values: Record<string, unknown>) => {
    setIsSubmitting(true);
    try {
      const payload = buildRecruitmentRequestPayload(values);
      let response;
      if (isEditMode) {
        response = await api.put(`/api/recruitment-request/${id}`, { ...payload, recruitmentRequestId: id });
      } else {
        response = await api.post('/api/recruitment-request/submit', payload);
      }

      if (response.data?.returnCode === 'SUC0000') {
        message.success('提交成功');
        localStorage.setItem('recruitmentRequestCreated', 'true');
        navigate('/recruitment-request');
      } else {
        message.error(`提交失败：${response.data?.errorMsg || '未知错误'}`);
      }
    } catch (error) {
      handleSubmitError(error, isEditMode);
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleSaveDraft = async () => {
    try {
      const values = form.getFieldsValue(true);
      setIsSubmitting(true);
      const payload = buildRecruitmentRequestPayload(values);
      const requestPayload = isEditMode ? { ...payload, recruitmentRequestId: id } : payload;
      const response = await api.post('/api/recruitment-request/save-draft', requestPayload);

      if (response.data?.returnCode === 'SUC0000') {
        message.success('草稿保存成功');
        navigate('/recruitment-request');
      } else {
        message.error(`草稿保存失败：${response.data?.errorMsg || '未知错误'}`);
      }
    } catch (error) {
      handleSaveDraftError(error);
    } finally {
      setIsSubmitting(false);
    }
  };

  if (loading) {
    return (
      <div className="app-page">
        <Card>
          <Spin />
        </Card>
      </div>
    );
  }

  return (
    <div className="app-page">
      <Card title={isEditMode ? '编辑用人申请' : '发起用人申请'} className="form-card">
        <Form form={form} layout="vertical" onFinish={handleSubmit}>
          <Form.Item name="requestTitle" label="申请标题" rules={[{ required: true, message: '请输入申请标题' }]}>
            <Input placeholder="请输入申请标题" />
          </Form.Item>

          <Space style={{ width: '100%' }} size={12} wrap>
            <Form.Item name="applicationDepartment" label="申请部门" style={{ minWidth: 220, flex: 1 }}>
              <Input disabled placeholder="自动带出当前用户部门" />
            </Form.Item>
            <Form.Item name="totalRecruitmentCount" label="总编制数" rules={[{ required: true, message: '总编制数缺失' }]} style={{ minWidth: 220, flex: 1 }}>
              <Input type="number" placeholder="从编制管理自动带出" disabled />
            </Form.Item>
            <Form.Item name="vacancyCount" label="空缺编制数" rules={[{ required: true, message: '空缺编制数缺失' }]} style={{ minWidth: 220, flex: 1 }}>
              <Input type="number" placeholder="从编制管理自动带出" disabled />
            </Form.Item>
          </Space>

          <Space style={{ width: '100%' }} size={12} wrap>
            <Form.Item name="requestType" label="所属类型" rules={[{ required: true, message: '请选择所属类型' }]} style={{ minWidth: 220, flex: 1 }}>
              <Select placeholder="请选择所属类型" options={REQUEST_TYPE_OPTIONS} />
            </Form.Item>
            <Form.Item name="category" label="所属分类" rules={[{ required: true, message: '请选择所属分类' }]} style={{ minWidth: 220, flex: 1 }}>
              <Select placeholder="请选择所属分类">
                {CATEGORY_OPTIONS.map((item) => (
                  <Select.Option key={item} value={item}>
                    {item}
                  </Select.Option>
                ))}
              </Select>
            </Form.Item>
            <Form.Item name="technicalPlatform" label="技术平台" rules={[{ required: true, message: '请选择技术平台' }]} style={{ minWidth: 220, flex: 1 }}>
              <Select placeholder="请选择技术平台">
                {PLATFORM_OPTIONS.map((item) => (
                  <Select.Option key={item} value={item}>
                    {item}
                  </Select.Option>
                ))}
              </Select>
            </Form.Item>
          </Space>

          <Space style={{ width: '100%' }} size={12} wrap>
            <Form.Item
              name="supplementCount"
              label="补充人数"
              rules={[
                { required: true, message: '请输入补充人数' },
                {
                  validator: async (_, value) => {
                    if (value === undefined || value === null || value === '') {
                      return;
                    }
                    if (Number(value) > 0) {
                      return;
                    }
                    throw new Error('补充人数必须大于 0');
                  },
                },
              ]}
              style={{ minWidth: 220, flex: 1 }}
            >
              <InputNumber min={1} style={{ width: '100%' }} placeholder="请输入补充人数" />
            </Form.Item>
            <Form.Item name="urgentRequirement" label="是否满足编制要求" rules={[{ required: true, message: '请选择是否满足编制要求' }]} style={{ minWidth: 220, flex: 1 }}>
              <Radio.Group>
                <Radio value="YES">是</Radio>
                <Radio value="NO">否</Radio>
              </Radio.Group>
            </Form.Item>
          </Space>

          <Space style={{ width: '100%' }} size={12} wrap>
            <Form.Item name="proposedLevel" label="建议级别" rules={[{ required: true, message: '请选择建议级别' }]} style={{ minWidth: 220, flex: 1 }}>
              <Select placeholder="请选择建议级别">
                {LEVEL_OPTIONS.map((item) => (
                  <Select.Option key={item} value={item}>
                    {item}
                  </Select.Option>
                ))}
              </Select>
            </Form.Item>
            <Form.Item name="experienceYears" label="相关经验年限要求" rules={[{ required: true, message: '请选择相关经验年限要求' }]} style={{ minWidth: 220, flex: 1 }}>
              <Select placeholder="请选择相关经验年限要求">
                <Select.Option value="0-1年">0-1年</Select.Option>
                <Select.Option value="1-3年">1-3年</Select.Option>
                <Select.Option value="3-5年">3-5年</Select.Option>
                <Select.Option value="5年以上">5年以上</Select.Option>
              </Select>
            </Form.Item>
          </Space>

          <Space style={{ width: '100%' }} size={12} wrap>
            <Form.Item
              name="interviewerId"
              label="面试官"
              rules={[{ required: true, message: '请选择面试官' }]}
              style={{ minWidth: 220, flex: 1 }}
            >
              <Select
                showSearch
                placeholder="请输入用户名搜索"
                filterOption={false}
                onSearch={handleInterviewerSearch}
                onFocus={() => loadInterviewers('')}
                onChange={handleInterviewerChange}
                options={interviewerOptions}
              />
            </Form.Item>
          </Space>

          <Form.Item name="interviewerName" hidden>
            <Input />
          </Form.Item>

          <Form.Item
            name="skillRequirement"
            label="任职要求"
            rules={[
              { required: true, message: '请填写任职要求' },
              { max: TEXTAREA_MAX_LENGTH, message: `任职要求不能超过 ${TEXTAREA_MAX_LENGTH} 个字符` },
            ]}
          >
            <Input.TextArea rows={4} maxLength={TEXTAREA_MAX_LENGTH} showCount placeholder="请填写任职要求" />
          </Form.Item>

          <Form.Item
            name="positionResponsibility"
            label="岗位职责"
            rules={[
              { required: true, message: '请填写岗位职责' },
              { max: TEXTAREA_MAX_LENGTH, message: `岗位职责不能超过 ${TEXTAREA_MAX_LENGTH} 个字符` },
            ]}
          >
            <Input.TextArea rows={4} maxLength={TEXTAREA_MAX_LENGTH} showCount placeholder="请填写岗位职责" />
          </Form.Item>

          <Form.Item
            name="remark"
            label="备注"
            rules={[{ max: TEXTAREA_MAX_LENGTH, message: `备注不能超过 ${TEXTAREA_MAX_LENGTH} 个字符` }]}
          >
            <Input.TextArea
              rows={4}
              maxLength={TEXTAREA_MAX_LENGTH}
              showCount
              placeholder={'需说明的情况/可写"无"'}
            />
          </Form.Item>

          <Space>
            <Button onClick={() => navigate('/recruitment-request')}>取消</Button>
            <Button onClick={handleSaveDraft} loading={isSubmitting}>保存草稿</Button>
            <Button type="primary" htmlType="submit" loading={isSubmitting}>
              {isEditMode ? '保存修改' : '发起用人申请'}
            </Button>
          </Space>
        </Form>
      </Card>
      <div className="app-footer-note">© 2026 外包招聘管理系统</div>
    </div>
  );
};

export default RecruitmentRequestForm;

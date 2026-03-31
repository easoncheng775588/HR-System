import React, { useCallback, useEffect, useRef, useState } from 'react';
import { Button, Card, Col, Form, Input, InputNumber, Row, Select, Space, Spin, message } from 'antd';
import { useNavigate, useParams } from 'react-router-dom';
import api from '../utils/api';
import { handleLoadError, handleSaveDraftError, handleSubmitError } from '../utils/errorHandler';
import { useAuth } from '../contexts/AuthContext';
import RecruitmentLevelHint from './RecruitmentLevelHint';
import {
  buildResponsibleDepartmentOptions,
  buildRecruitmentRequestPayload,
  getOrgUnitName,
  normalizeRecruitmentRequestFormValues,
  RECRUITMENT_CATEGORY_OPTIONS,
  RECRUITMENT_PLATFORM_OPTIONS,
  REQUEST_TYPE_OPTIONS,
  TEXTAREA_MAX_LENGTH,
} from './recruitmentRequestHelpers';
import { RECRUITMENT_LEVEL_OPTIONS } from './interviewEvaluationHelpers';

type InterviewerOption = {
  value: string;
  label: string;
};

type DepartmentOption = {
  value: string;
  label: string;
  orgUnitName: string;
  totalRecruitmentCount: number;
  vacancyCount: number;
};

const RecruitmentRequestForm = () => {
  const [form] = Form.useForm();
  const navigate = useNavigate();
  const { id } = useParams();
  const { user } = useAuth();

  const [isSubmitting, setIsSubmitting] = useState(false);
  const [loading, setLoading] = useState(false);
  const [isEditMode, setIsEditMode] = useState(false);
  const [interviewerOptions, setInterviewerOptions] = useState<InterviewerOption[]>([]);
  const [departmentOptions, setDepartmentOptions] = useState<DepartmentOption[]>([]);
  const interviewerSearchTimer = useRef<number | null>(null);

  const userOrgUnitName = getOrgUnitName(user || {});
  const fieldColSpan = { xs: 24, sm: 12, xl: 6 };
  const skillRequirementHint = `其他基本要求：
1、统招全日制本科及以上学历；
2、两年以上相关工作经验；
3、英语四级及以上水平，读写良好；
4、工作态度好，责任心强，纪律性强，有团队精神，服从工作安排`

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

  const applyDepartmentSelection = useCallback((selectedOrgUnitName?: string) => {
    const option = departmentOptions.find((item) => item.value === selectedOrgUnitName);
    if (!option) {
      form.setFieldsValue({
        orgUnitName: selectedOrgUnitName,
        applicationDepartment: selectedOrgUnitName,
        totalRecruitmentCount: 0,
        vacancyCount: 0,
      });
      if (selectedOrgUnitName) {
        fetchStaffingByOrgUnit(selectedOrgUnitName);
      }
      return;
    }

    form.setFieldsValue({
      orgUnitName: option.orgUnitName,
      applicationDepartment: option.label,
      totalRecruitmentCount: option.totalRecruitmentCount,
      vacancyCount: option.vacancyCount,
    });
  }, [departmentOptions, fetchStaffingByOrgUnit, form]);

  const loadResponsibleDepartments = useCallback(async () => {
    if (!user?.userId) {
      setDepartmentOptions([]);
      return;
    }

    try {
      const [staffingResponse, orgUnitResponse] = await Promise.all([
        api.get('/api/staffings/responsible-options', { params: { userId: user.userId } }),
        api.get('/api/org-units/active'),
      ]);

      if (staffingResponse.data?.returnCode !== 'SUC0000') {
        message.error(staffingResponse.data?.errorMsg || '获取负责室组失败');
        setDepartmentOptions([]);
        return;
      }

      const options = buildResponsibleDepartmentOptions(
        staffingResponse.data?.body || [],
        orgUnitResponse.data?.body || [],
      );
      setDepartmentOptions(options);
    } catch (_error) {
      setDepartmentOptions([]);
    }
  }, [user?.userId]);

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
    loadResponsibleDepartments();
  }, [loadResponsibleDepartments]);

  useEffect(() => {
    if (!departmentOptions.length) {
      return;
    }

    const currentOrgUnitName = String(form.getFieldValue('orgUnitName') || '');
    if (currentOrgUnitName) {
      applyDepartmentSelection(currentOrgUnitName);
      return;
    }

    if (id) {
      return;
    }

    const defaultOption =
      departmentOptions.find((item) => item.value === userOrgUnitName) ||
      departmentOptions[0];
    if (defaultOption) {
      applyDepartmentSelection(defaultOption.value);
    }
  }, [applyDepartmentSelection, departmentOptions, form, id, userOrgUnitName]);

  useEffect(() => {
    if (id || departmentOptions.length) {
      return;
    }
    fetchStaffingByOrgUnit(userOrgUnitName);
  }, [departmentOptions.length, fetchStaffingByOrgUnit, id, userOrgUnitName]);

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

  const handleDepartmentChange = (selectedOrgUnitName: string) => {
    applyDepartmentSelection(selectedOrgUnitName);
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
      <Card title={isEditMode ? '编辑用人申请' : '发起用人申请'} className="form-card recruitment-request-form-card">
        <Form form={form} layout="vertical" onFinish={handleSubmit}>
          <Row gutter={[16, 0]}>
            <Col span={24}>
              <Form.Item name="requestTitle" label="申请标题" rules={[{ required: true, message: '请输入申请标题' }]}>
                <Input placeholder="请输入申请标题" />
              </Form.Item>
            </Col>

            <Col {...fieldColSpan}>
              <Form.Item
                name="orgUnitName"
                label="申请部门"
                rules={[{ required: true, message: '请选择申请部门' }]}
              >
                <Select
                  showSearch
                  placeholder="请选择申请部门"
                  optionFilterProp="label"
                  options={departmentOptions}
                  onChange={handleDepartmentChange}
                />
              </Form.Item>
            </Col>
            <Col {...fieldColSpan}>
              <Form.Item name="totalRecruitmentCount" label="总编制数" rules={[{ required: true, message: '总编制数缺失' }]}>
                <Input type="number" placeholder="从编制管理自动带出" disabled />
              </Form.Item>
            </Col>
            <Col {...fieldColSpan}>
              <Form.Item name="vacancyCount" label="空缺编制数" rules={[{ required: true, message: '空缺编制数缺失' }]}>
                <Input type="number" placeholder="从编制管理自动带出" disabled />
              </Form.Item>
            </Col>
            <Col {...fieldColSpan}>
              <Form.Item name="requestType" label="所属类型" rules={[{ required: true, message: '请选择所属类型' }]}>
                <Select placeholder="请选择所属类型" options={REQUEST_TYPE_OPTIONS} />
              </Form.Item>
            </Col>

            <Col {...fieldColSpan}>
              <Form.Item name="category" label="所属分类" rules={[{ required: true, message: '请选择所属分类' }]}>
                <Select placeholder="请选择所属分类">
                  {RECRUITMENT_CATEGORY_OPTIONS.map((item) => (
                    <Select.Option key={item} value={item}>
                      {item}
                    </Select.Option>
                  ))}
                </Select>
              </Form.Item>
            </Col>
            <Col {...fieldColSpan}>
              <Form.Item name="technicalPlatform" label="技术平台" rules={[{ required: true, message: '请选择技术平台' }]}>
                <Select placeholder="请选择技术平台">
                  {RECRUITMENT_PLATFORM_OPTIONS.map((item) => (
                    <Select.Option key={item} value={item}>
                      {item}
                    </Select.Option>
                  ))}
                </Select>
              </Form.Item>
            </Col>
            <Col {...fieldColSpan}>
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
              >
                <InputNumber min={1} style={{ width: '100%' }} placeholder="请输入补充人数" />
              </Form.Item>
            </Col>
            <Col {...fieldColSpan}>
              <Form.Item name="proposedLevel" label={<RecruitmentLevelHint />} rules={[{ required: true, message: '请选择建议级别' }]}>
                <Select placeholder="请选择建议级别">
                  {RECRUITMENT_LEVEL_OPTIONS.map((item) => (
                    <Select.Option key={item} value={item}>
                      {item}
                    </Select.Option>
                  ))}
                </Select>
              </Form.Item>
            </Col>
            <Col {...fieldColSpan}>
              <Form.Item name="experienceYears" label="相关经验年限要求" rules={[{ required: true, message: '请选择相关经验年限要求' }]}>
                <Select placeholder="请选择相关经验年限要求">
                  <Select.Option value="0-1年">0-1年</Select.Option>
                  <Select.Option value="1-3年">1-3年</Select.Option>
                  <Select.Option value="3-5年">3-5年</Select.Option>
                  <Select.Option value="5年以上">5年以上</Select.Option>
                </Select>
              </Form.Item>
            </Col>
            <Col {...fieldColSpan}>
              <Form.Item
                name="interviewerId"
                label="面试官"
                rules={[{ required: true, message: '请选择面试官' }]}
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
            </Col>
          </Row>

          <Form.Item name="interviewerName" hidden>
            <Input />
          </Form.Item>

          <Form.Item name="urgentRequirement" hidden>
            <Input />
          </Form.Item>

          <Form.Item name="applicationDepartment" hidden>
            <Input />
          </Form.Item>

          <Form.Item
            name="skillRequirement"
            label={<RecruitmentLevelHint label="任职要求" content={skillRequirementHint} />}
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

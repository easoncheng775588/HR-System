import React, { useEffect, useRef, useState } from 'react';
import { Button, Form, Input, InputNumber, message, Modal, Popconfirm, Select, Space, Table, Tag, Upload } from 'antd';
import { DeleteOutlined, DownloadOutlined, EditOutlined, PlusOutlined, UploadOutlined } from '@ant-design/icons';
import type { UploadFile } from 'antd/es/upload/interface';
import api from '../utils/api';
import { useAuth } from '../contexts/AuthContext';

interface OrgUnitItem {
  unitId: number;
  unitName: string;
  unitType: string;
  parentUnitName?: string;
}

interface StaffingItem {
  staffingId: number;
  orgUnitName: string;
  responsibleUserId?: string;
  responsibleUserName?: string;
  totalHeadcount: number;
  vacancyHeadcount: number;
  outsourcingHeadcount: number;
  employeeHeadcount: number;
  createUserName?: string;
  createTime?: string;
  updateUserName?: string;
  updateTime?: string;
}

interface SubmitError {
  errorFields?: unknown;
  message?: string;
}

interface UserOption {
  value: string;
  label: string;
}

const getErrorMessage = (error: unknown): string => {
  if (typeof error === 'object' && error && 'message' in error) {
    return String((error as { message?: string }).message || '未知错误');
  }
  return '未知错误';
};

const StaffingManagement: React.FC = () => {
  const { user } = useAuth();
  const [loading, setLoading] = useState(false);
  const [submitLoading, setSubmitLoading] = useState(false);
  const [modalVisible, setModalVisible] = useState(false);
  const [importVisible, setImportVisible] = useState(false);
  const [data, setData] = useState<StaffingItem[]>([]);
  const [orgUnits, setOrgUnits] = useState<OrgUnitItem[]>([]);
  const [responsibleOptions, setResponsibleOptions] = useState<UserOption[]>([]);
  const [editing, setEditing] = useState<StaffingItem | null>(null);
  const [fileList, setFileList] = useState<UploadFile[]>([]);
  const responsibleSearchTimer = useRef<number | null>(null);
  const [form] = Form.useForm();

  useEffect(() => {
    fetchOrgUnits();
    fetchStaffings();
  }, [user?.userId]);

  useEffect(() => () => {
    if (responsibleSearchTimer.current) {
      window.clearTimeout(responsibleSearchTimer.current);
    }
  }, []);

  const fetchOrgUnits = async () => {
    try {
      const response = await api.get('/api/org-units/active');
      if (response.data?.returnCode === 'SUC0000') {
        setOrgUnits(response.data.body || []);
      } else {
        message.error(response.data?.errorMsg || '获取组织数据失败');
      }
    } catch (error: unknown) {
      message.error(`获取组织数据失败: ${getErrorMessage(error)}`);
    }
  };

  const fetchStaffings = async () => {
    setLoading(true);
    try {
      const response = await api.get('/api/staffings', {
        params: {
          viewerId: String(user?.userId || ''),
          viewerRole: String(user?.position || user?.positionName || user?.role || ''),
        },
      });
      if (response.data?.returnCode === 'SUC0000') {
        setData(response.data.body || []);
      } else {
        message.error(response.data?.errorMsg || '获取编制列表失败');
      }
    } catch (error: unknown) {
      message.error(`获取编制列表失败: ${getErrorMessage(error)}`);
    } finally {
      setLoading(false);
    }
  };

  const loadResponsibleUsers = async (keyword = '') => {
    try {
      const response = await api.get('/api/users/search', { params: { keyword } });
      if (response.data?.returnCode !== 'SUC0000') {
        setResponsibleOptions([]);
        return;
      }
      const options = (response.data.body || [])
        .map((item: { userId?: string; realName?: string; username?: string }) => ({
          value: String(item.userId || ''),
          label: `${item.realName || item.username || item.userId || ''} (${item.userId || '-'})`,
        }))
        .filter((item: UserOption) => item.value);
      setResponsibleOptions(options);
    } catch (_error) {
      setResponsibleOptions([]);
    }
  };

  const handleResponsibleSearch = (keyword: string) => {
    if (responsibleSearchTimer.current) {
      window.clearTimeout(responsibleSearchTimer.current);
    }
    responsibleSearchTimer.current = window.setTimeout(() => {
      loadResponsibleUsers(keyword);
    }, 250);
  };

  const handleResponsibleChange = (_value: string, option: unknown) => {
    const label = String((option as { label?: string })?.label || '');
    const responsibleUserName = label.includes('(') ? label.split('(')[0].trim() : label;
    form.setFieldsValue({ responsibleUserName });
  };

  const openCreateModal = () => {
    setEditing(null);
    form.resetFields();
    form.setFieldsValue({
      totalHeadcount: 0,
      vacancyHeadcount: 0,
      outsourcingHeadcount: 0,
      employeeHeadcount: 0,
    });
    loadResponsibleUsers('');
    setModalVisible(true);
  };

  const openEditModal = (record: StaffingItem) => {
    setEditing(record);
    form.setFieldsValue(record);
    if (record.responsibleUserId && record.responsibleUserName) {
      setResponsibleOptions((prev) => {
        const exists = prev.some((item) => item.value === record.responsibleUserId);
        if (exists) return prev;
        return [
          { value: record.responsibleUserId || '', label: `${record.responsibleUserName} (${record.responsibleUserId})` },
          ...prev,
        ];
      });
    }
    setModalVisible(true);
  };

  const handleDelete = async (staffingId: number) => {
    try {
      const response = await api.delete(`/api/staffings/${staffingId}`);
      if (response.data?.returnCode === 'SUC0000') {
        message.success('删除成功');
        fetchStaffings();
      } else {
        message.error(response.data?.errorMsg || '删除失败');
      }
    } catch (error: unknown) {
      message.error(`删除失败: ${getErrorMessage(error)}`);
    }
  };

  const handleSubmit = async () => {
    try {
      const values = await form.validateFields();
      const payload = {
        orgUnitName: values.orgUnitName,
        responsibleUserId: values.responsibleUserId,
        responsibleUserName: values.responsibleUserName,
        totalHeadcount: Number(values.totalHeadcount || 0),
        vacancyHeadcount: Number(values.vacancyHeadcount || 0),
        outsourcingHeadcount: Number(values.outsourcingHeadcount || 0),
        employeeHeadcount: Number(values.employeeHeadcount || 0),
      };

      setSubmitLoading(true);
      const response = editing
        ? await api.put(`/api/staffings/${editing.staffingId}`, payload)
        : await api.post('/api/staffings', payload);

      if (response.data?.returnCode === 'SUC0000') {
        message.success(editing ? '编制更新成功' : '编制创建成功');
        setModalVisible(false);
        fetchStaffings();
      } else {
        message.error(response.data?.errorMsg || '操作失败');
      }
    } catch (error: unknown) {
      const submitError = error as SubmitError;
      if (submitError?.errorFields) return;
      message.error(`操作失败: ${getErrorMessage(error)}`);
    } finally {
      setSubmitLoading(false);
    }
  };

  const handleDownloadTemplate = async () => {
    try {
      const response = await api.get('/api/staffings/template', { responseType: 'blob' });
      const blob = new Blob([response.data], {
        type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
      });
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = '编制管理导入模板.xlsx';
      a.click();
      window.URL.revokeObjectURL(url);
    } catch (error: unknown) {
      message.error(`模板下载失败: ${getErrorMessage(error)}`);
    }
  };

  const handleImport = async () => {
    if (!fileList.length || !fileList[0].originFileObj) {
      message.warning('请先选择导入文件');
      return;
    }
    const formData = new FormData();
    formData.append('file', fileList[0].originFileObj);
    try {
      const response = await api.post('/api/staffings/import', formData, {
        headers: { 'Content-Type': 'multipart/form-data' },
      });
      if (response.data?.returnCode === 'SUC0000') {
        const body = response.data.body || {};
        const successCount = body.successCount || 0;
        const failCount = body.failCount || 0;
        const errors = body.errors || [];
        if (failCount > 0) {
          message.warning(`导入完成：成功 ${successCount} 条，失败 ${failCount} 条`);
          if (errors.length) {
            Modal.info({
              title: '导入失败明细',
              content: (
                <div style={{ maxHeight: 260, overflow: 'auto' }}>
                  {errors.map((item: string, index: number) => (
                    <div key={index}>{item}</div>
                  ))}
                </div>
              ),
              width: 620,
            });
          }
        } else {
          message.success(`导入成功：${successCount} 条`);
        }
        setImportVisible(false);
        setFileList([]);
        fetchStaffings();
      } else {
        message.error(response.data?.errorMsg || '导入失败');
      }
    } catch (error: unknown) {
      message.error(`导入失败: ${getErrorMessage(error)}`);
    }
  };

  const columns = [
    { title: '团队/室组名称', dataIndex: 'orgUnitName', key: 'orgUnitName', width: 220 },
    {
      title: '负责人',
      dataIndex: 'responsibleUserName',
      key: 'responsibleUserName',
      width: 140,
      render: (value: string) => value || '-',
    },
    { title: '总编制数', dataIndex: 'totalHeadcount', key: 'totalHeadcount', width: 100 },
    { title: '空缺编制数', dataIndex: 'vacancyHeadcount', key: 'vacancyHeadcount', width: 110 },
    { title: '外包编制数', dataIndex: 'outsourcingHeadcount', key: 'outsourcingHeadcount', width: 110 },
    { title: '员工编制数', dataIndex: 'employeeHeadcount', key: 'employeeHeadcount', width: 110 },
    {
      title: '创建人',
      dataIndex: 'createUserName',
      key: 'createUserName',
      width: 120,
      responsive: ['xl' as const],
      render: (v: string) => v || '-',
    },
    {
      title: '创建时间',
      dataIndex: 'createTime',
      key: 'createTime',
      width: 180,
      responsive: ['xxl' as const],
      render: (v: string) => (v ? new Date(v).toLocaleString('zh-CN') : '-'),
    },
    {
      title: '修改人',
      dataIndex: 'updateUserName',
      key: 'updateUserName',
      width: 120,
      responsive: ['xl' as const],
      render: (v: string) => v || '-',
    },
    {
      title: '修改时间',
      dataIndex: 'updateTime',
      key: 'updateTime',
      width: 180,
      responsive: ['xxl' as const],
      render: (v: string) => (v ? new Date(v).toLocaleString('zh-CN') : '-'),
    },
    {
      title: '操作',
      key: 'action',
      width: 150,
      fixed: 'right' as const,
      render: (_value: unknown, record: StaffingItem) => (
        <Space size="small">
          <Button type="link" icon={<EditOutlined />} onClick={() => openEditModal(record)}>
            编辑
          </Button>
          <Popconfirm
            title="确认删除该编制记录吗？"
            onConfirm={() => handleDelete(record.staffingId)}
            okText="确认"
            cancelText="取消"
          >
            <Button type="link" danger icon={<DeleteOutlined />}>
              删除
            </Button>
          </Popconfirm>
        </Space>
      ),
    },
  ];

  const orgUnitOptions = orgUnits.map((u) => ({
    value: u.unitName,
    label: u.unitName,
  }));
  const selectedOrgUnitName = Form.useWatch('orgUnitName', form);
  const selectedOrgUnit = orgUnits.find((item) => item.unitName === selectedOrgUnitName);
  const responsibleRequired = selectedOrgUnit?.unitType === 'GROUP';
  const hasData = data.length > 0;
  const tableColumns = hasData ? columns : columns.map(({ width, fixed, ...rest }) => rest);

  return (
    <div style={{ width: '100%' }}>
      <div style={{ marginBottom: 16, display: 'flex', gap: 8, flexWrap: 'wrap' }}>
        <Button type="primary" icon={<PlusOutlined />} onClick={openCreateModal}>
          新增编制
        </Button>
        <Button icon={<DownloadOutlined />} onClick={handleDownloadTemplate}>
          下载Excel模板
        </Button>
        <Button icon={<UploadOutlined />} onClick={() => setImportVisible(true)}>
          批量导入
        </Button>
        <Tag color="blue">固定模板导入</Tag>
      </div>

      <div className="app-table-wrap">
        <Table
          className={hasData ? '' : 'table-empty-state'}
          rowKey="staffingId"
          loading={loading}
          dataSource={data}
          columns={tableColumns}
          scroll={hasData ? { x: 1500 } : undefined}
          pagination={{
            defaultPageSize: 10,
            showSizeChanger: true,
            pageSizeOptions: [10, 20, 30, 50, 100],
            hideOnSinglePage: false,
            showTotal: (total) => `共 ${total} 条`,
          }}
        />
      </div>

      <Modal
        title={editing ? '编辑编制' : '新增编制'}
        open={modalVisible}
        onOk={handleSubmit}
        confirmLoading={submitLoading}
        onCancel={() => setModalVisible(false)}
        okText="确认"
        cancelText="取消"
        width={640}
      >
        <Form form={form} layout="vertical">
          <Form.Item label="团队/室组名称" name="orgUnitName" rules={[{ required: true, message: '请选择团队/室组名称' }]}>
            <Select showSearch placeholder="请选择团队/室组名称" options={orgUnitOptions} optionFilterProp="label" />
          </Form.Item>
          <Form.Item
            label="负责人"
            name="responsibleUserId"
            rules={responsibleRequired ? [{ required: true, message: '请选择室组负责人' }] : []}
            extra={responsibleRequired ? '室组编制必须配置负责人' : '团队编制可不填'}
          >
            <Select
              showSearch
              placeholder="请输入负责人姓名搜索"
              filterOption={false}
              optionFilterProp="label"
              onSearch={handleResponsibleSearch}
              onFocus={() => loadResponsibleUsers('')}
              onChange={handleResponsibleChange}
              options={responsibleOptions}
              allowClear
            />
          </Form.Item>
          <Form.Item name="responsibleUserName" hidden>
            <Input />
          </Form.Item>
          <Form.Item label="总编制数" name="totalHeadcount" rules={[{ required: true, message: '请输入总编制数' }]}>
            <InputNumber min={0} style={{ width: '100%' }} />
          </Form.Item>
          <Form.Item label="空缺编制数" name="vacancyHeadcount" rules={[{ required: true, message: '请输入空缺编制数' }]}>
            <InputNumber min={0} style={{ width: '100%' }} />
          </Form.Item>
          <Form.Item label="外包编制数" name="outsourcingHeadcount" rules={[{ required: true, message: '请输入外包编制数' }]}>
            <InputNumber min={0} style={{ width: '100%' }} />
          </Form.Item>
          <Form.Item label="员工编制数" name="employeeHeadcount" rules={[{ required: true, message: '请输入员工编制数' }]}>
            <InputNumber min={0} style={{ width: '100%' }} />
          </Form.Item>
        </Form>
      </Modal>

      <Modal
        title="批量导入编制"
        open={importVisible}
        onOk={handleImport}
        onCancel={() => {
          setImportVisible(false);
          setFileList([]);
        }}
        okText="开始导入"
        cancelText="取消"
      >
        <Upload
          accept=".xlsx"
          beforeUpload={() => false}
          fileList={fileList}
          onChange={({ fileList: files }) => setFileList(files.slice(-1))}
          maxCount={1}
        >
          <Button icon={<UploadOutlined />}>选择Excel文件</Button>
        </Upload>
        <div style={{ marginTop: 12, color: '#666' }}>请先下载固定模板，按模板填写后再导入。</div>
      </Modal>
    </div>
  );
};

export default StaffingManagement;

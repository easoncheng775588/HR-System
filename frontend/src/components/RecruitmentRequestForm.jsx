import React, { useState, useEffect } from 'react';
import { Form, Input, Select, Radio, Button, Card, message } from 'antd';
import api from '../utils/api';
import { useNavigate, useParams } from 'react-router-dom';

const { Option } = Select;

const RecruitmentRequestForm = () => {
  const [form] = Form.useForm();
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [isMobile, setIsMobile] = useState(false);
  const [isEditMode, setIsEditMode] = useState(false);
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();
  const { id } = useParams();

  useEffect(() => {
    handleResize();
    window.addEventListener('resize', handleResize);
    return () => window.removeEventListener('resize', handleResize);
  }, []);

  useEffect(() => {
    if (id) {
      setIsEditMode(true);
      loadRequestData(id);
    }
  }, [id]);

  const handleResize = () => {
    setIsMobile(window.innerWidth < 768);
  };

  const loadRequestData = async (requestId) => {
    setLoading(true);
    try {
      const response = await api.get(`/api/recruitment-request/${requestId}`);
      if (response.data.returnCode === 'SUC0000') {
        const data = response.data.body;
        form.setFieldsValue(data);
      } else {
        message.error('加载数据失败：' + response.data.errorMsg);
        navigate('/recruitment-request');
      }
    } catch (error) {
      message.error('加载数据失败，请稍后重试');
      console.error('Load error:', error);
      navigate('/recruitment-request');
    } finally {
      setLoading(false);
    }
  };

  const onFinish = async (values) => {
    setIsSubmitting(true);
    try {
      let response;
      if (isEditMode) {
        values.recruitmentRequestId = id;
        response = await api.put(`/api/recruitment-request/${id}`, values);
      } else {
        response = await api.post('/api/recruitment-request/submit', values);
      }
      
      if (response.data.returnCode === 'SUC0000') {
        message.success(isEditMode ? '更新成功！' : '提交成功！');
        form.resetFields();
        // 触发待办事项更新
        if (!isEditMode) {
          localStorage.setItem('recruitmentRequestCreated', 'true');
        }
        navigate('/recruitment-request');
      } else {
        message.error(`${isEditMode ? '更新' : '提交'}失败：${response.data.errorMsg}`);
      }
    } catch (error) {
      message.error(`${isEditMode ? '更新' : '提交'}失败，请稍后重试`);
      console.error('Submit error:', error);
    } finally {
      setIsSubmitting(false);
    }
  };

  const onSaveDraft = async (values) => {
    setIsSubmitting(true);
    try {
      const response = await api.post('/api/recruitment-request/save-draft', values);
      if (response.data.returnCode === 'SUC0000') {
        message.success('草稿保存成功！');
        navigate('/recruitment-request');
      } else {
        message.error(`草稿保存失败：${response.data.errorMsg}`);
      }
    } catch (error) {
      message.error('草稿保存失败，请稍后重试');
      console.error('Save draft error:', error);
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleBackToList = () => {
    navigate('/recruitment-request');
  };

  const formItemStyle = {
    marginBottom: isMobile ? 12 : 16
  };

  const cardStyle = {
    marginBottom: isMobile ? 16 : 20
  };

  const buttonStyle = {
    fontSize: isMobile ? 12 : 14,
    padding: isMobile ? '4px 12px' : '6px 16px'
  };

  return (
    <div className="page-container">
      <div className="page-title">
        {isEditMode ? '编辑用人申请' : '用人申请表单'}
      </div>
      <p style={{ textAlign: 'left', marginBottom: isMobile ? '20px' : '30px', color: '#666', fontSize: isMobile ? 12 : 14 }}>
        {isEditMode ? '请修改用人申请信息' : '请填写完整的用人申请信息'}
      </p>
      
      <Form
        form={form}
        layout="vertical"
        onFinish={onFinish}
      >
        <Card className="form-card" bordered={true} style={{ borderTop: 'none', borderLeft: 'none', borderRight: 'none' }}>
          <div className="form-card-title">基本信息</div>
          <div style={{ display: 'flex', gap: isMobile ? 12 : 20, flexDirection: isMobile ? 'column' : 'row' }}>
            <Form.Item
              name="requestTitle"
              label="申请标题"
              rules={[{ required: true, message: '请输入申请标题' }]}
              style={formItemStyle}
            >
              <Input placeholder="请输入申请标题" size={isMobile ? 'small' : 'middle'} />
            </Form.Item>
          </div>
          
          <div style={{ display: 'flex', gap: isMobile ? 12 : 20, flexDirection: isMobile ? 'column' : 'row' }}>
            <Form.Item
              name="totalRecruitmentCount"
              label="总编制人数"
              rules={[{ required: true, message: '请输入总编制人数' }]}
              style={{ flex: 1, ...formItemStyle }}
            >
              <Input type="number" placeholder="请输入总编制人数" size={isMobile ? 'small' : 'middle'} />
            </Form.Item>
            
            <Form.Item
              name="vacancyCount"
              label="空缺编制"
              rules={[{ required: true, message: '请输入空缺编制' }]}
              style={{ flex: 1, ...formItemStyle }}
            >
              <Input type="number" placeholder="请输入空缺编制" size={isMobile ? 'small' : 'middle'} />
            </Form.Item>
          </div>
          
          <div style={{ display: 'flex', gap: isMobile ? 12 : 20, flexDirection: isMobile ? 'column' : 'row' }}>
            <Form.Item
              name="interviewer"
              label="面试官"
              rules={[{ required: true, message: '请输入面试官姓名' }]}
              style={{ flex: 1, ...formItemStyle }}
            >
              <Input placeholder="请输入面试官姓名" size={isMobile ? 'small' : 'middle'} />
            </Form.Item>
            
            <Form.Item
              name="positionOrTeam"
              label="岗位/用人班组"
              rules={[{ required: true, message: '请输入岗位或用人班组' }]}
              style={{ flex: 1, ...formItemStyle }}
            >
              <Input placeholder="请输入岗位或用人班组" size={isMobile ? 'small' : 'middle'} />
            </Form.Item>
          </div>
        </Card>
        
        <Card className="form-card" bordered={true} style={{ borderTop: 'none', borderLeft: 'none', borderRight: 'none' }}>
          <div className="form-card-title">组织与分类</div>
          <div style={{ display: 'flex', gap: isMobile ? 12 : 20, flexDirection: isMobile ? 'column' : 'row' }}>
            <Form.Item
              name="teamManager"
              label="所属团队经理"
              rules={[{ required: true, message: '请输入团队经理姓名' }]}
              style={{ flex: 1, ...formItemStyle }}
            >
              <Input placeholder="请输入团队经理姓名" size={isMobile ? 'small' : 'middle'} />
            </Form.Item>
            
            <Form.Item
              name="category"
              label="所属分类"
              rules={[{ required: true, message: '请选择分类' }]}
              style={{ flex: 1, ...formItemStyle }}
            >
              <Select placeholder="请选择分类" size={isMobile ? 'small' : 'middle'}>
                <Option value="technical">技术</Option>
                <Option value="business">业务</Option>
                <Option value="operation">运营</Option>
                <Option value="other">其他</Option>
              </Select>
            </Form.Item>
          </div>
          
          <div style={{ display: 'flex', gap: isMobile ? 12 : 20, flexDirection: isMobile ? 'column' : 'row' }}>
            <Form.Item
              name="technicalPlatform"
              label="技术平台"
              rules={[{ required: true, message: '请选择技术平台' }]}
              style={{ flex: 1, ...formItemStyle }}
            >
              <Select placeholder="请选择技术平台" size={isMobile ? 'small' : 'middle'}>
                <Option value="frontend">前端</Option>
                <Option value="backend">后端</Option>
                <Option value="mobile">移动端</Option>
                <Option value="ai">人工智能</Option>
              </Select>
            </Form.Item>
            
            <Form.Item
              name="type"
              label="所属类型"
              rules={[{ required: true, message: '请选择类型' }]}
              style={{ flex: 1, ...formItemStyle }}
            >
              <Select placeholder="请选择类型" size={isMobile ? 'small' : 'middle'}>
                <Option value="fulltime">全职</Option>
                <Option value="parttime">兼职</Option>
                <Option value="intern">实习生</Option>
              </Select>
            </Form.Item>
          </div>
        </Card>
        
        <Card className="form-card" bordered={true} style={{ borderTop: 'none', borderLeft: 'none', borderRight: 'none' }}>
          <div className="form-card-title">招聘需求</div>
          <div style={{ display: 'flex', gap: isMobile ? 12 : 20, flexDirection: isMobile ? 'column' : 'row' }}>
            <Form.Item
              name="supplementCount"
              label="补充人数"
              rules={[{ required: true, message: '请输入补充人数' }]}
              style={{ flex: 1, ...formItemStyle }}
            >
              <Input type="number" placeholder="请输入补充人数" size={isMobile ? 'small' : 'middle'} />
            </Form.Item>
            
            <Form.Item
              name="urgentRequirement"
              label="是否近期紧急要求"
              rules={[{ required: true, message: '请选择是否近期紧急要求' }]}
              style={{ flex: 1, ...formItemStyle }}
            >
              <Radio.Group size={isMobile ? 'small' : 'middle'}>
                <Radio value="yes">是</Radio>
                <Radio value="no">否</Radio>
              </Radio.Group>
            </Form.Item>
          </div>
          
          <div style={{ display: 'flex', gap: isMobile ? 12 : 20, flexDirection: isMobile ? 'column' : 'row' }}>
            <Form.Item
              name="proposedLevel"
              label="建议级别"
              rules={[{ required: true, message: '请选择建议级别' }]}
              style={{ flex: 1, ...formItemStyle }}
            >
              <Select placeholder="请选择建议级别" size={isMobile ? 'small' : 'middle'}>
                <Option value="junior">初级</Option>
                <Option value="senior">中级</Option>
                <Option value="advanced">高级</Option>
                <Option value="expert">专家</Option>
              </Select>
            </Form.Item>
            
            <Form.Item
              name="experienceYears"
              label="相关经验年限要求"
              rules={[{ required: true, message: '请选择经验年限' }]}
              style={{ flex: 1, ...formItemStyle }}
            >
              <Select placeholder="请选择经验年限" size={isMobile ? 'small' : 'middle'}>
                <Option value="0-1">0-1年</Option>
                <Option value="1-3">1-3年</Option>
                <Option value="3-5">3-5年</Option>
                <Option value="5+">5年以上</Option>
              </Select>
            </Form.Item>
          </div>
          
          <Form.Item
            name="positionResponsibility"
            label="岗位职责"
            rules={[{ required: true, message: '请详细描述岗位职责和工作内容' }]}
            style={formItemStyle}
          >
            <Input.TextArea 
              rows={isMobile ? 3 : 4} 
              placeholder="请详细描述岗位职责和工作内容..."
              size={isMobile ? 'small' : 'middle'}
            />
          </Form.Item>
          
          <p style={{ color: '#999', fontSize: isMobile ? 10 : 12 }}>建议包含：主要工作内容、技能要求、工作目标等</p>
        </Card>
        
        <div style={{ 
          display: 'flex', 
          justifyContent: 'flex-end', 
          gap: isMobile ? 8 : 10, 
          marginTop: isMobile ? 16 : 20,
          flexDirection: isMobile ? 'column' : 'row'
        }}>
          <Button 
            htmlType="button"
            onClick={handleBackToList}
            style={buttonStyle}
            size={isMobile ? 'small' : 'middle'}
          >
            取消
          </Button>
          <Button 
            type="default" 
            onClick={() => form.validateFields().then(onSaveDraft)}
            loading={isSubmitting}
            style={buttonStyle}
            size={isMobile ? 'small' : 'middle'}
          >
            保存草稿
          </Button>
          <Button 
            type="primary" 
            htmlType="submit"
            loading={isSubmitting}
            size={isMobile ? 'small' : 'middle'}
          >
            提交申请
          </Button>
        </div>
      </Form>
      
      <div style={{ 
        textAlign: 'center', 
        marginTop: isMobile ? 15 : 30, 
        color: '#999', 
        fontSize: isMobile ? 10 : 12 
      }}>
        © 2024人力资源管理系统 - 用人申请模块
      </div>
    </div>
  );
};

export default RecruitmentRequestForm;
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
  const [levels, setLevels] = useState([]);
  const [levelsLoading, setLevelsLoading] = useState(false);
  const [teams, setTeams] = useState([]);
  const [teamsLoading, setTeamsLoading] = useState(false);
  const navigate = useNavigate();
  const { id } = useParams();

  useEffect(() => {
    handleResize();
    window.addEventListener('resize', handleResize);
    return () => window.removeEventListener('resize', handleResize);
  }, []);

  useEffect(() => {
    fetchLevels();
    fetchTeams();
  }, []);

  // 通用的缓存获取函数
  const fetchWithCache = async (url, cacheKey, timestampKey, setData, setLoading, errorMessage) => {
    // 先从localStorage获取缓存的数据
    const cachedData = localStorage.getItem(cacheKey);
    const cachedTimestamp = localStorage.getItem(timestampKey);
    const now = Date.now();
    const cacheExpiry = 24 * 60 * 60 * 1000; // 24小时缓存
    
    if (cachedData && cachedTimestamp && (now - parseInt(cachedTimestamp)) < cacheExpiry) {
      try {
        const parsedData = JSON.parse(cachedData);
        setData(parsedData);
        setLoading(false);
        return;
      } catch (error) {
        console.error(`解析缓存的${errorMessage}数据失败:`, error);
      }
    }
    
    setLoading(true);
    try {
      const response = await api.get(url);
      if (response.data.returnCode === 'SUC0000') {
        const data = response.data.body || [];
        if (data.length > 0) {
          setData(data);
          // 缓存到localStorage
          localStorage.setItem(cacheKey, JSON.stringify(data));
          localStorage.setItem(timestampKey, now.toString());
        } else {
          message.warning(`未获取到${errorMessage}数据，使用默认值`);
          // 使用空数组作为fallback
          setData([]);
        }
      } else {
        message.error(`获取${errorMessage}失败：` + response.data.errorMsg);
        // 使用空数组作为fallback
        setData([]);
      }
    } catch (error) {
      message.error(`获取${errorMessage}失败，请稍后重试`);
      console.error(`Fetch ${errorMessage} error:`, error);
      // 使用空数组作为fallback
      setData([]);
    } finally {
      setLoading(false);
    }
  };

  const fetchTeams = async () => {
    await fetchWithCache(
      '/api/sys/params/active/type/TEAM',
      'cachedTeams',
      'teamsTimestamp',
      setTeams,
      setTeamsLoading,
      '所属团队'
    );
  };

  const fetchLevels = async () => {
    await fetchWithCache(
      '/api/sys/params/active/type/LEVEL',
      'cachedLevels',
      'levelsTimestamp',
      setLevels,
      setLevelsLoading,
      '建议级别'
    );
  };

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
      console.error('Load error:', error);
      if (error.code === 'ECONNABORTED') {
        message.error('请求超时，请检查网络连接后重试');
      } else if (error.response) {
        // 服务器返回错误
        const errorMsg = error.response.data?.errorMsg || '加载数据失败';
        message.error(errorMsg);
      } else if (error.request) {
        // 请求已发送但没有收到响应
        message.error('服务器无响应，请稍后重试');
      } else {
        // 其他错误
        message.error('加载数据失败，请稍后重试');
      }
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
      console.error('Submit error:', error);
      if (error.code === 'ECONNABORTED') {
        message.error('请求超时，请检查网络连接后重试');
      } else if (error.response) {
        // 服务器返回错误
        const errorMsg = error.response.data?.errorMsg || `${isEditMode ? '更新' : '提交'}失败`;
        message.error(errorMsg);
      } else if (error.request) {
        // 请求已发送但没有收到响应
        message.error('服务器无响应，请稍后重试');
      } else {
        // 其他错误
        message.error(`${isEditMode ? '更新' : '提交'}失败，请稍后重试`);
      }
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
      console.error('Save draft error:', error);
      if (error.code === 'ECONNABORTED') {
        message.error('请求超时，请检查网络连接后重试');
      } else if (error.response) {
        // 服务器返回错误
        const errorMsg = error.response.data?.errorMsg || '草稿保存失败';
        message.error(errorMsg);
      } else if (error.request) {
        // 请求已发送但没有收到响应
        message.error('服务器无响应，请稍后重试');
      } else {
        // 其他错误
        message.error('草稿保存失败，请稍后重试');
      }
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleBackToList = () => {
    navigate('/recruitment-request');
  };

  const formItemStyle = {
    marginBottom: isMobile ? 4 : 8
  };

  const cardStyle = {
    marginBottom: isMobile ? 8 : 12
  };

  const buttonStyle = {
    fontSize: isMobile ? 11 : 12,
    padding: isMobile ? '3px 8px' : '4px 12px'
  };

  return (
    <div className="interview-scheduling">
      
      <Form
        form={form}
        layout="vertical"
        onFinish={onFinish}
      >
        <Card className="form-card" bordered={true} style={{ borderTop: 'none', borderLeft: 'none', borderRight: 'none' }}>
          <div className="form-card-title">基本信息</div>
          <div style={{ display: 'flex', gap: isMobile ? 8 : 16, flexDirection: isMobile ? 'column' : 'row' }}>
            <Form.Item
              name="requestTitle"
              label="岗位标题"
              rules={[{ required: true, message: '请输入岗位标题' }]}
              style={formItemStyle}
            >
              <Input placeholder="请输入岗位标题" size={isMobile ? 'small' : 'middle'} />
            </Form.Item>
          </div>
          
          <div style={{ display: 'flex', gap: isMobile ? 8 : 16, flexDirection: isMobile ? 'column' : 'row' }}>
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
          
          <div style={{ display: 'flex', gap: isMobile ? 8 : 16, flexDirection: isMobile ? 'column' : 'row' }}>
            <Form.Item
              name="team"
              label="所属团队"
              rules={[{ required: true, message: '请选择所属团队' }]}
              style={{ flex: 1, ...formItemStyle }}
            >
              <Select 
                placeholder="请选择所属团队" 
                size={isMobile ? 'small' : 'middle'}
                loading={teamsLoading}
              >
                {teams.map((team) => (
                  <Option key={team.paramValue} value={team.paramValue}>
                    {team.paramName}
                  </Option>
                ))}
              </Select>
            </Form.Item>
            
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
          </div>
        </Card>
        
        <Card className="form-card" bordered={true} style={{ borderTop: 'none', borderLeft: 'none', borderRight: 'none' }}>
          <div className="form-card-title">招聘需求</div>
          <div style={{ display: 'flex', gap: isMobile ? 8 : 16, flexDirection: isMobile ? 'column' : 'row' }}>
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
          
          <div style={{ display: 'flex', gap: isMobile ? 8 : 16, flexDirection: isMobile ? 'column' : 'row' }}>
            <Form.Item
              name="proposedLevel"
              label="建议级别"
              rules={[{ required: true, message: '请选择建议级别' }]}
              style={{ flex: 1, ...formItemStyle }}
            >
              <Select 
                placeholder="请选择建议级别" 
                size={isMobile ? 'small' : 'middle'}
                loading={levelsLoading}
              >
                {levels.map((level) => (
                  <Option key={level.paramValue} value={level.paramValue}>
                    {level.paramName}
                  </Option>
                ))}
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
            name="skillRequirement"
            label="技能要求描述"
            rules={[{ required: true, message: '请详细描述技能要求' }]}
            style={formItemStyle}
          >
            <Input.TextArea 
              rows={isMobile ? 3 : 4} 
              placeholder="请详细描述技能要求..."
              size={isMobile ? 'small' : 'middle'}
            />
          </Form.Item>
          
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
          
          <p style={{ color: '#999', fontSize: isMobile ? 10 : 12 }}>建议包含：主要工作内容、工作目标等</p>
        </Card>
        
        <div style={{ 
          display: 'flex', 
          justifyContent: 'flex-end', 
          gap: isMobile ? 4 : 6, 
          marginTop: isMobile ? 8 : 12,
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
        marginTop: isMobile ? 6 : 10, 
        color: '#999', 
        fontSize: isMobile ? 9 : 10 
      }}>
        © 2024人力资源管理系统 - 用人申请模块
      </div>
    </div>
  );
};

export default RecruitmentRequestForm;
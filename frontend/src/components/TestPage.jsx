import React from 'react';

const TestPage = () => {
  return (
    <div style={{
      display: 'flex',
      justifyContent: 'center',
      alignItems: 'center',
      minHeight: '100vh',
      background: '#f0f2f5'
    }}>
      <div style={{
        textAlign: 'center',
        padding: '40px',
        background: '#fff',
        borderRadius: '8px',
        boxShadow: '0 4px 12px rgba(0,0,0,0.15)'
      }}>
        <h1>测试页面</h1>
        <p>如果您看到此页面，说明前端开发服务器运行正常。</p>
        <p>现在尝试访问登录页面：<a href="/login">/login</a></p>
      </div>
    </div>
  );
};

export default TestPage;
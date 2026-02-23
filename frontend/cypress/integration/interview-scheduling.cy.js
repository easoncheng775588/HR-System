/// <reference types="cypress" />

describe('面试安排模块测试', () => {
  beforeEach(() => {
    // 先登录
    cy.visit('http://localhost:5173');
    cy.get('input[name="username"]').type('admin');
    cy.get('input[name="password"]').type('123321');
    cy.get('button[type="submit"]').click();
    
    // 导航到面试安排页面
    cy.contains('面试安排').click();
  });

  it('测试安排面试', () => {
    // 点击安排面试按钮（日历图标）
    cy.get('button').contains('安排面试').first().click();
    
    // 验证安排面试模态框显示
    cy.contains('安排面试');
    
    // 填写面试信息
    cy.get('select[name="interviewerId"]').select('1'); // 假设面试官ID为1
    cy.get('input[name="interviewDate"]').type('2026-02-24');
    cy.get('input[name="interviewTime"]').type('10:00');
    
    // 保存面试安排
    cy.get('button').contains('确定').click();
    
    // 验证安排成功
    cy.contains('面试安排成功');
  });

  it('测试录入面试结果', () => {
    // 点击面试记录按钮（文件图标）
    cy.get('button').contains('面试记录').first().click();
    
    // 验证面试记录模态框显示
    cy.contains('面试记录');
    
    // 点击录入面试结果按钮
    cy.get('button').contains('录入面试结果').click();
    
    // 验证录入结果模态框显示
    cy.contains('录入面试结果');
    
    // 填写面试结果
    cy.get('select[name="interviewResult"]').select('通过');
    cy.get('textarea[name="interviewComment"]').type('表现优秀，推荐通过');
    
    // 保存面试结果
    cy.get('button').contains('确定').click();
    
    // 验证录入成功
    cy.contains('面试结果录入成功');
  });

  it('测试查看面试记录', () => {
    // 点击面试记录按钮（文件图标）
    cy.get('button').contains('面试记录').first().click();
    
    // 验证面试记录模态框显示
    cy.contains('面试记录');
    // 验证包含面试信息
    cy.contains('面试环节');
    cy.contains('面试官');
    cy.contains('面试时间');
    
    // 关闭模态框
    cy.get('button').contains('知道了').click();
  });

  it('测试面试时间边界值', () => {
    // 点击安排面试按钮
    cy.get('button').contains('安排面试').first().click();
    
    // 尝试安排当天的面试
    const today = new Date().toISOString().split('T')[0];
    cy.get('input[name="interviewDate"]').type(today);
    cy.get('input[name="interviewTime"]').type('14:00');
    
    // 保存面试安排
    cy.get('button').contains('确定').click();
    
    // 验证安排成功
    cy.contains('面试安排成功');
  });

  it('测试权限控制 - 非外包招聘岗不能安排面试', () => {
    // 先登出
    cy.get('button').contains('登出').click();
    
    // 以非外包招聘岗身份登录
    cy.get('input[name="username"]').type('user');
    cy.get('input[name="password"]').type('123456');
    cy.get('button[type="submit"]').click();
    
    // 导航到面试安排页面
    cy.contains('面试安排').click();
    
    // 验证没有安排面试按钮
    cy.get('button').contains('安排面试').should('not.exist');
  });
});

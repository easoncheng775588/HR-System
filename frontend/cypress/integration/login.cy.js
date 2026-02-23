/// <reference types="cypress" />

describe('登录认证测试', () => {
  beforeEach(() => {
    // 访问登录页面
    cy.visit('http://localhost:5173');
  });

  it('测试登录成功', () => {
    // 输入用户名和密码
    cy.get('input[name="username"]').type('admin');
    cy.get('input[name="password"]').type('123321');
    
    // 点击登录按钮
    cy.get('button[type="submit"]').click();
    
    // 验证登录成功后跳转到首页
    cy.url().should('include', '/dashboard');
    // 验证首页包含欢迎信息
    cy.contains('欢迎');
  });

  it('测试登录失败 - 用户名错误', () => {
    // 输入错误的用户名
    cy.get('input[name="username"]').type('wronguser');
    cy.get('input[name="password"]').type('123321');
    
    // 点击登录按钮
    cy.get('button[type="submit"]').click();
    
    // 验证登录失败提示
    cy.contains('登录失败');
    // 验证仍然在登录页面
    cy.url().should('not.include', '/dashboard');
  });

  it('测试登录失败 - 密码错误', () => {
    // 输入错误的密码
    cy.get('input[name="username"]').type('admin');
    cy.get('input[name="password"]').type('wrongpassword');
    
    // 点击登录按钮
    cy.get('button[type="submit"]').click();
    
    // 验证登录失败提示
    cy.contains('登录失败');
    // 验证仍然在登录页面
    cy.url().should('not.include', '/dashboard');
  });

  it('测试登录失败 - 缺少必填字段', () => {
    // 不输入用户名
    cy.get('input[name="password"]').type('123321');
    cy.get('button[type="submit"]').click();
    cy.contains('请输入用户名');
    
    // 清空并测试缺少密码
    cy.get('input[name="password"]').clear();
    cy.get('input[name="username"]').type('admin');
    cy.get('button[type="submit"]').click();
    cy.contains('请输入密码');
  });

  it('测试登出功能', () => {
    // 先登录
    cy.get('input[name="username"]').type('admin');
    cy.get('input[name="password"]').type('123321');
    cy.get('button[type="submit"]').click();
    
    // 验证登录成功
    cy.url().should('include', '/dashboard');
    
    // 点击登出按钮
    cy.get('button').contains('登出').click();
    
    // 验证登出后跳转到登录页面
    cy.url().should('not.include', '/dashboard');
    cy.contains('登录');
  });
});

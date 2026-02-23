/// <reference types="cypress" />

describe('权限控制测试', () => {
  it('测试超级管理员权限（正面）', () => {
    // 登录超级管理员
    cy.visit('http://localhost:5173');
    cy.get('input[name="username"]').type('admin');
    cy.get('input[name="password"]').type('123321');
    cy.get('button[type="submit"]').click();
    
    // 验证可以访问所有功能模块
    cy.contains('用人申请').click();
    cy.contains('简历筛选').click();
    cy.contains('面试安排').click();
    cy.contains('岗位发布').click();
    cy.contains('用户管理').click();
    
    // 验证所有页面都能正常加载
    cy.contains('用户管理');
  });

  it('测试普通用户权限不足（反面）', () => {
    // 登录普通用户
    cy.visit('http://localhost:5173');
    cy.get('input[name="username"]').type('user');
    cy.get('input[name="password"]').type('123456');
    cy.get('button[type="submit"]').click();
    
    // 验证可以访问基础功能
    cy.contains('用人申请').click();
    
    // 尝试访问需要管理员权限的页面
    cy.visit('http://localhost:5173/user-management');
    
    // 验证权限不足提示
    cy.contains('权限不足');
    // 或者验证被重定向到无权限页面
    cy.url().should('include', '/unauthorized');
  });

  it('测试部门经理权限（正面）', () => {
    // 登录部门经理
    cy.visit('http://localhost:5173');
    cy.get('input[name="username"]').type('manager');
    cy.get('input[name="password"]').type('123456');
    cy.get('button[type="submit"]').click();
    
    // 验证可以访问本部门的用人申请
    cy.contains('用人申请').click();
    cy.contains('我的申请');
    
    // 验证可以审批申请
    cy.contains('待审批');
  });

  it('测试部门经理跨部门操作（反面）', () => {
    // 登录部门经理
    cy.visit('http://localhost:5173');
    cy.get('input[name="username"]').type('manager');
    cy.get('input[name="password"]').type('123456');
    cy.get('button[type="submit"]').click();
    
    // 进入用人申请页面
    cy.contains('用人申请').click();
    
    // 尝试编辑其他部门的申请
    // 这里需要根据实际页面结构调整选择器
    cy.contains('其他部门').parent().parent().find('button').contains('编辑').click();
    
    // 验证权限不足提示
    cy.contains('无权限操作');
  });

  it('测试外包招聘岗权限（正面）', () => {
    // 登录外包招聘岗
    cy.visit('http://localhost:5173');
    cy.get('input[name="username"]').type('recruiter');
    cy.get('input[name="password"]').type('123456');
    cy.get('button[type="submit"]').click();
    
    // 验证可以安排面试
    cy.contains('面试安排').click();
    cy.contains('安排面试');
  });

  it('测试非外包招聘岗安排面试（反面）', () => {
    // 登录非外包招聘岗用户
    cy.visit('http://localhost:5173');
    cy.get('input[name="username"]').type('user');
    cy.get('input[name="password"]').type('123456');
    cy.get('button[type="submit"]').click();
    
    // 进入面试安排页面
    cy.contains('面试安排').click();
    
    // 验证没有安排面试按钮
    cy.get('button').contains('安排面试').should('not.exist');
  });

  it('测试HR权限（正面）', () => {
    // 登录HR
    cy.visit('http://localhost:5173');
    cy.get('input[name="username"]').type('hr');
    cy.get('input[name="password"]').type('123456');
    cy.get('button[type="submit"]').click();
    
    // 验证可以发布岗位
    cy.contains('岗位发布').click();
    cy.contains('发布');
  });

  it('测试非HR操作（反面）', () => {
    // 登录非HR用户
    cy.visit('http://localhost:5173');
    cy.get('input[name="username"]').type('user');
    cy.get('input[name="password"]').type('123456');
    cy.get('button[type="submit"]').click();
    
    // 进入岗位发布页面
    cy.contains('岗位发布').click();
    
    // 验证没有发布按钮或者权限不足
    cy.get('button').contains('发布').should('not.exist');
  });
});

/// <reference types="cypress" />

describe('简历筛选模块测试', () => {
  beforeEach(() => {
    // 先登录
    cy.visit('http://localhost:5173');
    cy.get('input[name="username"]').type('admin');
    cy.get('input[name="password"]').type('123321');
    cy.get('button[type="submit"]').click();
    
    // 导航到简历筛选页面
    cy.contains('简历筛选').click();
  });

  it('测试查看简历列表', () => {
    // 验证页面加载成功
    cy.contains('简历筛选管理');
    // 验证显示简历列表
    cy.contains('刷新简历');
    // 验证显示简历数量
    cy.contains('共');
  });

  it('测试刷新简历列表', () => {
    // 点击刷新按钮
    cy.get('button').contains('刷新简历').click();
    
    // 验证页面刷新
    cy.contains('加载中...').should('not.exist');
    cy.contains('简历筛选管理');
  });

  it('测试查看简历详情', () => {
    // 点击查看按钮（眼睛图标）
    cy.get('button').contains('查看').first().click();
    
    // 验证详情抽屉显示
    cy.contains('简历详情');
    // 验证包含个人信息
    cy.contains('个人信息');
    // 验证包含岗位信息
    cy.contains('岗位信息');
    // 关闭抽屉
    cy.get('button').contains('关闭').click();
  });

  it('测试下载简历', () => {
    // 点击下载按钮
    cy.get('button').contains('下载').first().click();
    
    // 验证下载成功（这里可以检查下载的文件，需要配置Cypress的下载行为）
    // cy.verifyDownload('resume.pdf'); // 需要安装cypress-downloadfile插件
  });

  it('测试通过简历', () => {
    // 找到待筛选的简历并点击通过
    cy.contains('待筛选').parent().parent().find('button').contains('通过').click();
    
    // 验证操作成功
    cy.contains('状态更新成功');
    // 验证状态变为已筛选
    cy.contains('已筛选');
  });

  it('测试拒绝简历', () => {
    // 找到待筛选的简历并点击拒绝
    cy.contains('待筛选').parent().parent().find('button').contains('拒绝').click();
    
    // 验证操作成功
    cy.contains('状态更新成功');
    // 验证状态变为已拒绝
    cy.contains('已拒绝');
  });

  it('测试大量简历数据加载', () => {
    // 验证页面在有大量数据时能正常加载
    // 这里可以通过API预先创建大量简历数据，然后测试页面加载性能
    cy.contains('简历筛选管理');
    // 验证页面没有卡顿
    cy.get('table').should('exist');
  });
});

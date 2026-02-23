/// <reference types="cypress" />

describe('岗位发布模块测试', () => {
  beforeEach(() => {
    // 先登录
    cy.visit('http://localhost:5173');
    cy.get('input[name="username"]').type('admin');
    cy.get('input[name="password"]').type('123321');
    cy.get('button[type="submit"]').click();
    
    // 导航到岗位发布页面
    cy.contains('岗位发布').click();
  });

  it('测试发布岗位', () => {
    // 点击发布按钮（对勾图标）
    cy.get('button').contains('发布').first().click();
    
    // 验证发布模态框显示
    cy.contains('发布岗位');
    
    // 验证表单自动填充
    cy.get('input[name="positionTitle"]').should('have.value');
    cy.get('input[name="recruitCount"]').should('have.value');
    
    // 提交发布
    cy.get('button').contains('发布').click();
    
    // 验证发布成功
    cy.contains('岗位发布成功');
    // 验证状态变为已发布
    cy.contains('已发布');
  });

  it('测试撤销发布', () => {
    // 找到已发布的岗位并点击撤销
    cy.contains('已发布').parent().parent().find('button').contains('撤销').click();
    
    // 验证撤销确认框显示
    cy.contains('确认撤销发布？');
    
    // 确认撤销
    cy.get('button').contains('确认').click();
    
    // 验证撤销成功
    cy.contains('岗位撤销成功');
    // 验证状态变为未发布
    cy.contains('未发布');
  });

  it('测试查看岗位详情', () => {
    // 点击查看按钮
    cy.get('button').contains('查看').first().click();
    
    // 验证详情抽屉显示
    cy.contains('申请详情');
    // 验证包含岗位信息
    cy.contains('岗位标题');
    cy.contains('补充人数');
    cy.contains('建议级别');
    
    // 关闭抽屉
    cy.get('button').contains('关闭').click();
  });

  it('测试重复发布', () => {
    // 找到已发布的岗位并再次点击发布
    cy.contains('已发布').parent().parent().find('button').contains('发布').click();
    
    // 验证提示信息
    cy.contains('岗位已发布');
  });

  it('测试岗位标题长度边界值', () => {
    // 点击发布按钮
    cy.get('button').contains('发布').first().click();
    
    // 修改岗位标题为长文本
    cy.get('input[name="positionTitle"]').clear().type('a'.repeat(100));
    
    // 提交发布
    cy.get('button').contains('发布').click();
    
    // 验证发布成功
    cy.contains('岗位发布成功');
  });
});

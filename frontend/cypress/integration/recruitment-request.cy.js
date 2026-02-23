/// <reference types="cypress" />

describe('用人申请模块测试', () => {
  beforeEach(() => {
    // 先登录
    cy.visit('http://localhost:5173');
    cy.get('input[name="username"]').type('admin');
    cy.get('input[name="password"]').type('123321');
    cy.get('button[type="submit"]').click();
    
    // 导航到用人申请列表页面
    cy.contains('用人申请').click();
  });

  it('测试创建用人申请', () => {
    // 点击新增按钮
    cy.get('button').contains('新增用人申请').click();
    
    // 填写表单
    cy.get('input[name="requestTitle"]').type('自动化测试岗位');
    cy.get('select[name="team"]').select('零售');
    cy.get('input[name="supplementCount"]').type('2');
    cy.get('select[name="proposedLevel"]').select('高级');
    cy.get('input[name="experienceYears"]').type('3-5年');
    cy.get('select[name="technicalPlatform"]').select('Java');
    cy.get('textarea[name="skillRequirement"]').type('熟悉Java开发');
    cy.get('textarea[name="positionResponsibility"]').type('负责系统开发和维护');
    
    // 提交申请
    cy.get('button').contains('提交申请').click();
    
    // 验证提交成功
    cy.contains('申请提交成功');
    
    // 验证新申请出现在列表中
    cy.contains('自动化测试岗位');
  });

  it('测试保存草稿', () => {
    // 点击新增按钮
    cy.get('button').contains('新增用人申请').click();
    
    // 填写部分表单
    cy.get('input[name="requestTitle"]').type('测试草稿');
    cy.get('select[name="team"]').select('批发');
    
    // 保存草稿
    cy.get('button').contains('保存草稿').click();
    
    // 验证保存成功
    cy.contains('草稿保存成功');
    
    // 验证草稿出现在列表中
    cy.contains('测试草稿');
  });

  it('测试编辑用人申请', () => {
    // 找到一个待审批的申请并点击编辑
    cy.contains('待审批').parent().parent().find('button').contains('编辑').click();
    
    // 修改表单
    cy.get('input[name="requestTitle"]').clear().type('修改后的测试岗位');
    
    // 保存修改
    cy.get('button').contains('提交申请').click();
    
    // 验证修改成功
    cy.contains('申请提交成功');
    cy.contains('修改后的测试岗位');
  });

  it('测试查看申请详情', () => {
    // 找到一个申请并点击查看
    cy.get('button').contains('查看').first().click();
    
    // 验证详情页面显示
    cy.contains('申请详情');
    // 验证包含岗位标题
    cy.contains('岗位标题');
  });

  it('测试缺少必填字段', () => {
    // 点击新增按钮
    cy.get('button').contains('新增用人申请').click();
    
    // 直接提交（不填写任何字段）
    cy.get('button').contains('提交申请').click();
    
    // 验证提示信息
    cy.contains('请输入岗位标题');
    cy.contains('请选择所属团队');
    cy.contains('请输入补充人数');
  });
});

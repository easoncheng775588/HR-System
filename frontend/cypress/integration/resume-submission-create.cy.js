/// <reference types="cypress" />

describe('Resume Submission Create Modal', () => {
  it('opens create resume modal and shows required fields', () => {
    cy.intercept('POST', '**/api/auth/login', {
      statusCode: 200,
      body: {
        returnCode: 'SUC0000',
        body: {
          token: 'Bearer mock-token',
          user: { userId: '1001', username: 'admin', realName: '管理员', position: '管理员' },
          permissions: [],
        },
      },
    });
    cy.intercept('GET', '**/api/resume/list', { statusCode: 200, body: { returnCode: 'SUC0000', body: [] } });
    cy.intercept('GET', '**/api/resume/requirement-options', {
      statusCode: 200,
      body: { returnCode: 'SUC0000', body: [{ value: 1, label: '需求A' }] },
    });
    cy.intercept('GET', '**/api/sys/params/active/type/*', { statusCode: 200, body: { returnCode: 'SUC0000', body: [] } });
    cy.intercept('GET', '**/api/recruitment-request/approval/pending', { statusCode: 200, body: { returnCode: 'SUC0000', body: [] } });
    cy.intercept('GET', '**/api/interview/pending/count', { statusCode: 200, body: { returnCode: 'SUC0000', body: 0 } });
    cy.intercept('GET', '**/api/message/user/**', { statusCode: 200, body: { returnCode: 'SUC0000', body: [] } });
    cy.intercept('GET', '**/api/message/unread-count/**', { statusCode: 200, body: { returnCode: 'SUC0000', body: { unreadCount: 0 } } });

    cy.visit('/login');
    cy.get('input[name="username"]').type('admin');
    cy.get('input[name="password"]').type('admin123');
    cy.get('button[type="submit"]').click();
    cy.url().should('include', '/dashboard');

    cy.contains('.ant-menu-item', '简历提交').click();
    cy.url().should('include', '/resume-submission');
    cy.contains('button', '新增简历').click();

    cy.contains('.ant-modal-title', '新增简历').should('exist');
    cy.contains('label', '关联需求').should('exist');
    cy.contains('label', '候选人').should('exist');
    cy.contains('label', '性别').should('exist');
    cy.contains('label', '可参加面试时间').should('exist');
    cy.contains('label', '附件').should('exist');
    cy.contains('label', '备注').should('exist');
  });
});


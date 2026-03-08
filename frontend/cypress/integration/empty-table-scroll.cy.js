/// <reference types="cypress" />

describe('Empty Table Scrollbars', () => {
  const loginAndEnterApp = () => {
    cy.intercept('POST', '**/api/auth/login', {
      statusCode: 200,
      body: {
        returnCode: 'SUC0000',
        body: {
          token: 'Bearer mock-token',
          user: {
            userId: '1001',
            username: 'admin',
            realName: '管理员',
            position: '管理员',
            department: '技术管理团队',
          },
          permissions: [],
        },
      },
    }).as('login');

    cy.intercept('GET', '**/api/recruitment-request/approval/pending', {
      statusCode: 200,
      body: { returnCode: 'SUC0000', body: [] },
    });
    cy.intercept('GET', '**/api/interview/pending/count', {
      statusCode: 200,
      body: { returnCode: 'SUC0000', body: 0 },
    });
    cy.intercept('GET', '**/api/message/user/**', {
      statusCode: 200,
      body: { returnCode: 'SUC0000', body: [] },
    });
    cy.intercept('GET', '**/api/message/unread-count/**', {
      statusCode: 200,
      body: { returnCode: 'SUC0000', body: { unreadCount: 0 } },
    });

    cy.visit('/login');
    cy.get('input[name="username"]').type('admin');
    cy.get('input[name="password"]').type('admin123');
    cy.get('button[type="submit"]').click();
    cy.wait('@login');
    cy.url().should('include', '/dashboard');
  };

  const verifyNoInnerVerticalScrollbar = () => {
    cy.get('.table-empty-state .ant-table-placeholder').should('exist');
    cy.get('.table-empty-state .ant-table-content').should('have.length.at.least', 1).each(($el) => {
      const el = $el[0];
      expect(el.scrollHeight, 'scrollHeight').to.equal(el.clientHeight);
      expect(el.scrollWidth, 'scrollWidth').to.equal(el.clientWidth);
      expect(getComputedStyle(el).overflowY, 'overflowY').to.equal('hidden');
      expect(getComputedStyle(el).overflowX, 'overflowX').to.equal('hidden');
    });
  };

  it('removes inner vertical scrollbar in empty state on target pages', () => {
    loginAndEnterApp();

    cy.intercept('GET', '**/api/recruitment-request/approval/status/3RDAPPROVED', {
      statusCode: 200,
      body: { returnCode: 'SUC0000', body: [] },
    });
    cy.intercept('GET', '**/api/resume/list', {
      statusCode: 200,
      body: { returnCode: 'SUC0000', body: [] },
    });
    cy.intercept('GET', '**/api/org-units/active', {
      statusCode: 200,
      body: { returnCode: 'SUC0000', body: [] },
    });
    cy.intercept('GET', '**/api/staffings', {
      statusCode: 200,
      body: { returnCode: 'SUC0000', body: [] },
    });

    cy.contains('.ant-menu-item', '岗位发布').click();
    cy.url().should('include', '/position-publishing');
    verifyNoInnerVerticalScrollbar();

    cy.contains('.ant-menu-item', '简历提交').click();
    cy.url().should('include', '/resume-submission');
    verifyNoInnerVerticalScrollbar();

    cy.contains('.ant-menu-item', '简历筛选').click();
    cy.url().should('include', '/resume-screening');
    verifyNoInnerVerticalScrollbar();

    cy.contains('.ant-menu-item', '编制管理').click();
    cy.url().should('include', '/staffing-management');
    verifyNoInnerVerticalScrollbar();
  });
});

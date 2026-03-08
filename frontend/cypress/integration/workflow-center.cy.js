/// <reference types="cypress" />

describe('Workflow Center', () => {
  it('can open workflow center and see three tabs', () => {
    cy.visit('http://localhost:5173/login');
    cy.get('input[name="username"]').clear().type('admin');
    cy.get('input[name="password"]').clear().type('123321');
    cy.get('button[type="submit"]').click();

    cy.contains('流程中心', { timeout: 10000 }).click();
    cy.contains('我的待办').should('be.visible');
    cy.contains('我发起的流程').should('be.visible');
    cy.contains('我处理的流程').should('be.visible');
  });
});

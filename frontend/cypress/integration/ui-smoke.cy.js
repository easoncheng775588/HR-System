/// <reference types="cypress" />

describe('UI Smoke', () => {
  it('loads login page and core fields', () => {
    cy.visit('http://localhost:5173/login');
    cy.contains('外包招聘管理系统');
    cy.get('input[name="username"]').should('exist');
    cy.get('input[name="password"]').should('exist');
    cy.get('button[type="submit"]').should('exist').and('be.visible');
  });
});

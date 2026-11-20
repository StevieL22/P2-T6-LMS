/// <reference types="cypress" />
describe('LMS Frontend Setup', () => {

  it('loads the LMS application', () => {

    cy.visit('http://localhost:5173');

    cy.get('#root').should('be.visible');

  });

});
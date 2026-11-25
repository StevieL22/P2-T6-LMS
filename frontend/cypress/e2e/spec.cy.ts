/// <reference types="cypress" />

describe("LMS Frontend Setup", () => {
  it("loads the LMS application", () => {
    cy.visit("/");

    cy.get("h1")
      .should("be.visible")
      .and("contain.text", "Team 6 Learning Management System");
  });
});
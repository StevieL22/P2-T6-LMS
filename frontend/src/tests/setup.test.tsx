import { describe, test, expect } from "@jest/globals";
import "@testing-library/jest-dom/jest-globals";
import { render, screen } from '@testing-library/react';
import '@testing-library/jest-dom';

// Test that React renders correctly
describe('Frontend Setup', () => {

  test('renders LMS heading', () => {

    // Arrange and Act
    render(<h1>Learning Management System</h1>);

    // Assert
    expect(
      screen.getByText('Learning Management System')
    ).toBeInTheDocument();

  });

});

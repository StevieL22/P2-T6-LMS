
module.exports = {
  testEnvironment: 'jsdom',
  roots: ['<rootDir>/src'],
  transform: {
    '^.+\\.[jt]sx?$': 'babel-jest'
  },
  setupFilesAfterEnv: [
    '@testing-library/jest-dom'
  ],
  collectCoverageFrom: [
    'src/**/*.{ts,tsx}',
    '!src/**/*.d.ts'
  ],
  coverageDirectory: 'coverage'
};

/**
 * For a detailed explanation regarding each configuration property, visit:
 * https://jestjs.io/docs/configuration
 */

/** @type {import('jest').Config} */
module.exports = {
  clearMocks: true,
  collectCoverage: true,
  collectCoverageFrom: [
    'src/**/*.vue',
  ],
  coverageReporters: [
    'html-spa',
    'text-summary',
  ],
  moduleNameMapper: {'^@/(.*)$': '<rootDir>/src/$1'},
  preset: '@vue/cli-plugin-unit-jest',
  reporters: [
    'default',
    [
      'jest-html-reporter',
      {
        outputPath: './reports/test-report.html',
        pageTitle: 'Test Report',
        includeFailureMsg: true,
        includeConsoleLog: true,
      },
    ],
  ],
  testEnvironment: 'jsdom',
  testMatch: [
    '**/tests/unit/**/*.js',
  ],
  testPathIgnorePatterns: [
    '/node_modules/'
  ],
  transform: {
    '^.+\\.js$': 'babel-jest',
    '^.+\\.vue$': '@vue/vue2-jest',
  },
  transformIgnorePatterns: [
    '/node_modules/(?!axios/).*'
  ],
  verbose: true,
};

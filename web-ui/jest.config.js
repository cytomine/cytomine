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
  moduleNameMapper: {
    '^.+\\.(css|sass|scss|png|jpg|jpeg|gif|webp|svg|ttf|woff|woff2)$': '<rootDir>/tests/unit/asset-stub.js',
    '^@/(.*)$': '<rootDir>/src/$1',
    '^@vue/test-utils$': '<rootDir>/tests/unit/vtu-compat.js',
    '^buefy$': '@ntohq/buefy-next',
    '^vee-validate$': '<rootDir>/src/utils/vee-validate-shim.js',
    '^v-tooltip$': 'floating-vue',
  },
  moduleFileExtensions: ['js', 'json', 'vue'],
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
  setupFilesAfterEnv: ['<rootDir>/tests/unit/setup.js'],
  testEnvironment: 'jsdom',
  testEnvironmentOptions: {customExportConditions: ['node', 'node-addons']},
  testMatch: [
    '**/tests/unit/**/*.test.js',
    '**/tests/unit/**/*.spec.js',
  ],
  testPathIgnorePatterns: [
    '/node_modules/'
  ],
  transform: {
    '^.+\\.js$': 'babel-jest',
    '^.+\\.vue$': '@vue/vue3-jest',
  },
  transformIgnorePatterns: [
    '/node_modules/(?!axios/|sl-vue-tree-next/|ol/|floating-vue/|@ckpack/).*'
  ],
  verbose: true,
};

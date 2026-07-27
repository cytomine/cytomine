module.exports = {
  'root': true,
  'env': {
    'node': true
  },
  'extends': [
    'eslint:recommended',
    'plugin:vitest/legacy-recommended',
    'plugin:vue/essential'
  ],
  globals: {
    suite: 'readonly',
    test: 'readonly',
    describe: 'readonly',
    it: 'readonly',
    expect: 'readonly',
    assert: 'readonly',
    vitest: 'readonly',
    vi: 'readonly',
    beforeAll: 'readonly',
    afterAll: 'readonly',
    beforeEach: 'readonly',
    afterEach: 'readonly'
  },
  rules: {
    'array-bracket-spacing': ['error', 'never'],
    'brace-style': ['error', '1tbs'],
    'camelcase': ['error', {allow: ['$_veeValidate']}],
    'curly': ['error', 'all'],
    'eqeqeq': ['error', 'smart'],
    'indent': ['error', 2, {'SwitchCase': 1, 'ignoredNodes': ['TemplateLiteral']}],
    'keyword-spacing': ['error'],
    'no-console': ['off'],
    'no-redeclare': ['error'],
    'no-undef': ['error'],
    'no-unused-vars': ['error'],
    'no-var': ['error'],
    'object-curly-spacing': ['error'],
    'quotes': ['error', 'single', {'avoidEscape': true}],
    'semi': ['error', 'always'],
    'space-before-blocks': ['error', 'always'],
    'space-before-function-paren': ['error', {
      anonymous: 'always',
      named: 'never',
      asyncArrow: 'always',
    }],
    'space-infix-ops': ['error'],
    'space-in-parens': ['error'],
    'vitest/expect-expect': 'off',
    'vitest/no-commented-out-tests': 'off',
    'vitest/no-disabled-tests': 'off',
  },
};

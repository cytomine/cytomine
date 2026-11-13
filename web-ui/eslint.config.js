import js from '@eslint/js';
import globals from 'globals';
import pluginVue from 'eslint-plugin-vue';
import vitest from '@vitest/eslint-plugin';

export default [
  {
    ignores: [
      'dist/',
      'coverage/',
      'reports/',
      'node_modules/',
      'src/locales/json/',
    ],
  },
  js.configs.recommended,
  ...pluginVue.configs['flat/essential'],
  {
    files: ['**/*.js', '**/*.vue'],
    languageOptions: {
      ecmaVersion: 2020,
      sourceType: 'module',
      globals: {
        ...globals.browser,
        ...globals.node,
        ...globals.es2020,
      },
    },
    rules: {
      'array-bracket-spacing': ['error', 'never'],
      'brace-style': ['error', '1tbs'],
      'camelcase': ['error', { allow: ['$_veeValidate'] }],
      'curly': ['error', 'all'],
      'eqeqeq': ['error', 'smart'],
      'indent': ['error', 2, { 'SwitchCase': 1, 'ignoredNodes': ['TemplateLiteral'] }],
      'keyword-spacing': ['error'],
      'no-console': ['off'],
      'no-redeclare': ['error'],
      'no-undef': ['error'],
      'no-unused-vars': ['error', { caughtErrors: 'none' }],
      'no-useless-assignment': ['off'],
      'no-var': ['error'],
      'object-curly-spacing': ['error', 'always'],
      'quotes': ['error', 'single', { 'avoidEscape': true }],
      'semi': ['error', 'always'],
      'space-before-blocks': ['error', 'always'],
      'space-before-function-paren': ['error', {
        anonymous: 'always',
        named: 'never',
        asyncArrow: 'always',
      }],
      'space-infix-ops': ['error'],
      'space-in-parens': ['error'],
      'vue/no-v-text-v-html-on-component': ['off'],
    },
  },
  {
    files: ['tests/**/*.js', 'tests/**/*.vue'],
    plugins: { vitest },
    rules: {
      ...vitest.configs.recommended.rules,
      'vitest/expect-expect': 'off',
      'vitest/no-commented-out-tests': 'off',
      'vitest/no-disabled-tests': 'off',
    },
    languageOptions: {
      globals: {
        ...vitest.environments.env.globals,
      },
    },
  },
];

import js from '@eslint/js';
import vitest from 'eslint-plugin-vitest';
import vue from 'eslint-plugin-vue';
import globals from 'globals';

export default [
  {
    // Flat config has no `--ignore-path`, so the .gitignore entries that hold
    // build output and generated files are repeated here. node_modules is
    // ignored by ESLint itself.
    ignores: [
      'dist/',
      'coverage/',
      'reports/',
      'src/locales/json/',
      'public/configuration.json'
    ]
  },

  {
    // ESLint 9 turned this on by default. Leaving it off keeps the reported set
    // identical to what the previous .eslintrc config produced; enabling it
    // surfaces 6 stale directives that are worth cleaning up separately.
    linterOptions: {
      reportUnusedDisableDirectives: 'off'
    }
  },

  js.configs.recommended,
  ...vue.configs['flat/essential'],

  {
    files: ['**/*.js', '**/*.vue'],
    languageOptions: {
      ecmaVersion: 2021,
      sourceType: 'module',
      globals: {
        ...globals.node,
        ...globals.browser
      }
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
      // ESLint 9 changed `caughtErrors` from 'none' to 'all'; keeping the old
      // default leaves the 16 unused `catch (error)` bindings for their own pass.
      'no-unused-vars': ['error', {caughtErrors: 'none'}],
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
      'space-in-parens': ['error']
    }
  },

  {
    files: ['tests/**/*.js'],
    plugins: {vitest},
    languageOptions: {
      globals: vitest.environments.env.globals
    },
    rules: {
      ...vitest.configs.recommended.rules,
      'vitest/expect-expect': 'off',
      'vitest/no-commented-out-tests': 'off',
      'vitest/no-disabled-tests': 'off'
    }
  }
];

import { defineConfig, mergeConfig } from 'vitest/config';

import viteConfig from './vite.config.js';

export default mergeConfig(
  viteConfig({ command: 'serve', mode: 'test' }),
  defineConfig({
    test: {
      environment: 'jsdom',
      globals: true,
      clearMocks: true,
      silent: 'passed-only',
      include: ['tests/unit/**/*.js'],
      reporters: ['default'],
      coverage: {
        provider: 'v8',
        enabled: true,
        reportOnFailure: true,
        reportsDirectory: './coverage',
        reporter: ['html-spa', 'text-summary'],
        include: ['src/**/*.vue']
      }
    }
  })
);

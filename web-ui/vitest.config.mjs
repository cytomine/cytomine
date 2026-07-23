import {defineConfig, mergeConfig} from 'vitest/config';

import viteConfig from './vite.config.mjs';

export default mergeConfig(
  viteConfig({command: 'serve', mode: 'test'}),
  defineConfig({
    test: {
      environment: 'jsdom',
      globals: true,
      clearMocks: true,
      include: ['tests/unit/**/*.js'],
      reporters: ['default'],
      coverage: {
        provider: 'v8',
        enabled: true,
        reportsDirectory: './coverage',
        reporter: ['html-spa', 'text-summary'],
        include: ['src/**/*.vue']
      }
    }
  })
);

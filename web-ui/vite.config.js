import { fileURLToPath, URL } from 'node:url';

import vue from '@vitejs/plugin-vue';
import { defineConfig } from 'vite';

export default defineConfig(({ command }) => ({
  plugins: [
    vue({
      template: {
        compilerOptions: {
          compatConfig: { MODE: 2 }
        }
      }
    })
  ],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url)),
      vue: '@vue/compat'
    },
    extensions: ['.mjs', '.js', '.mts', '.ts', '.jsx', '.tsx', '.vue', '.json']
  },
  esbuild: command === 'build' ? { drop: ['console'] } : undefined,
  build: {
    commonjsOptions: {
      // UMD/CJS libraries (vue-slider-component, ...) require('vue') and expect the Vue constructor, not ESM namespace
      requireReturnsDefault: id => id.includes('node_modules/@vue/compat/') ? 'preferred' : 'auto'
    }
  },
  server: {
    host: true,
    port: Number(process.env.PORT) || 8080,
    strictPort: true,
    hmr: {
      path: '/dev-ws'
    }
  }
}));

import {fileURLToPath, URL} from 'node:url';

import vue2 from '@vitejs/plugin-vue2';
import {defineConfig} from 'vite';

export default defineConfig(({command}) => ({
  plugins: [vue2()],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    }
  },
  // Replaces babel-plugin-transform-remove-console (production only)
  esbuild: command === 'build' ? {drop: ['console']} : undefined,
  build: {
    commonjsOptions: {
      // UMD/CJS libraries (vue-slider-component, vue-draggable-resizable, ...) do
      // require('vue') and expect the Vue constructor, not the ESM namespace
      requireReturnsDefault: id => id.includes('node_modules/vue/') ? 'preferred' : 'auto'
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

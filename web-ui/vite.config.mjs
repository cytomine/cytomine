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
  server: {
    host: true,
    port: 8080,
    hmr: {
      protocol: 'ws',
      host: '127.0.0.1',
      clientPort: 80,
      path: '/dev-ws'
    }
  }
}));

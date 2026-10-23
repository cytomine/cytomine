import { fileURLToPath, URL } from 'node:url';

import vue from '@vitejs/plugin-vue';
import { defineConfig } from 'vite';

export default defineConfig(({ command }) => ({
  plugins: [
    vue(),
  ],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    },
    extensions: ['.mjs', '.js', '.mts', '.ts', '.jsx', '.tsx', '.vue', '.json']
  },
  // Replaces babel-plugin-transform-remove-console (production only)
  esbuild: command === 'build' ? { drop: ['console'] } : undefined,
  server: {
    host: true,
    port: Number(process.env.PORT) || 8080,
    strictPort: true,
    hmr: {
      path: '/dev-ws'
    }
  }
}));

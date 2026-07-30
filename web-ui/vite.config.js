import { fileURLToPath, URL } from 'node:url';

import vue from '@vitejs/plugin-vue';
import { defineConfig } from 'vite';

export default defineConfig(({ command }) => ({
  plugins: [
    vue({
      template: {
        compilerOptions: {
          // MODE: 2 keeps Vue 2 runtime behavior by default across the board while
          // running on @vue/compat, so 188 SFCs keep working during the staged migration.
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
  // Replaces babel-plugin-transform-remove-console (production only)
  esbuild: command === 'build' ? { drop: ['console'] } : undefined,
  build: {
    commonjsOptions: {
      // UMD/CJS libraries (vue-slider-component, vue-draggable-resizable, ...) do
      // require('vue') and expect the Vue constructor, not the ESM namespace
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

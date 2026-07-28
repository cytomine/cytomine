import fs from 'node:fs';
import {fileURLToPath, URL} from 'node:url';

import {defineConfig, mergeConfig} from 'vitest/config';

import viteConfig from './vite.config.js';

const pkg = JSON.parse(
  fs.readFileSync(fileURLToPath(new URL('./package.json', import.meta.url)), 'utf8')
);

// Node condition resolution picks the CommonJS build of most Vue libraries, and
// the require('vue') inside it bypasses the `vue` -> `@vue/compat` alias set in
// vite.config.js: the library then runs against a second, plain Vue 3 runtime
// while the components under test run on the compat one, which blows up as soon
// as the two exchange component instances. Point every Vue-consuming dependency
// that ships an ESM build at that build instead, so Vite rewrites its `vue`
// import and a single compat runtime is used everywhere.
const BUILD_TOOLING = ['@vitejs/plugin-vue', 'eslint-plugin-vue', 'vue-eslint-parser'];

function vueLibraryAliases() {
  const aliases = {};
  const names = [...Object.keys(pkg.dependencies), ...Object.keys(pkg.devDependencies)];

  for (const name of names) {
    if (name === 'vue' || name === '@vue/compat' || BUILD_TOOLING.includes(name)) {
      continue;
    }

    // Read from disk rather than require(): an "exports" map that does not
    // expose ./package.json would otherwise hide the package from us.
    const manifest = fileURLToPath(new URL(`./node_modules/${name}/package.json`, import.meta.url));
    if (!fs.existsSync(manifest)) {
      continue;
    }
    const meta = JSON.parse(fs.readFileSync(manifest, 'utf8'));

    const dependsOnVue = ['dependencies', 'peerDependencies', 'devDependencies']
      .some(field => meta[field] && meta[field].vue);
    if (!dependsOnVue) {
      continue;
    }

    const esm = meta.module || meta.exports?.['.']?.import || meta.exports?.['.']?.module;
    if (typeof esm === 'string') {
      aliases[name] = fileURLToPath(new URL(`./node_modules/${name}/${esm}`, import.meta.url));
    }
  }

  return aliases;
}

const vueLibraries = vueLibraryAliases();

export default mergeConfig(
  viteConfig({command: 'serve', mode: 'test'}),
  defineConfig({
    resolve: {
      alias: vueLibraries
    },
    test: {
      environment: 'jsdom',
      globals: true,
      clearMocks: true,
      include: ['tests/unit/**/*.js'],
      setupFiles: ['tests/setup.js'],
      reporters: ['default'],
      server: {
        deps: {
          // Aliasing alone is not enough: the packages also have to go through
          // Vite's transform pipeline for the `vue` import to be rewritten.
          inline: Object.keys(vueLibraries)
        }
      },
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

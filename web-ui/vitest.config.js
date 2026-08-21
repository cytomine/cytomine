import fs from 'node:fs';
import path from 'node:path';
import { fileURLToPath, URL } from 'node:url';

import { defineConfig, mergeConfig } from 'vitest/config';

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

// A Vite alias matches the bare name *and* every subpath under it, but points
// them all at the one file named here, so a package that is imported per
// component cannot be aliased this way. `src/viewer-ol/` imports
// `vue3-openlayers/map/OlMap` and friends rather than the barrel, to keep the
// jspdf/proj4/ol-ext components it never renders out of the bundle; its
// "exports" map already resolves those to ESM, so it only needs inlining.
const SUBPATH_IMPORTED = ['vue3-openlayers'];

// `vue-demi` exists to forward every export of whichever Vue major is installed,
// which it does with `export * from 'vue'`. Aliasing it the usual way produces a
// module with no exports at all: the alias lands on @vue/compat's CommonJS build
// here, and a star re-export cannot see through CJS interop. Point it straight at
// the compat runtime instead — being Vue is the whole of its job.
const VUE_FORWARDERS = { 'vue-demi': '@vue/compat' };

const projectRoot = fileURLToPath(new URL('.', import.meta.url));

// Read from disk rather than require(): an "exports" map that does not expose
// ./package.json would otherwise hide the package from us. Walking up from the
// importer's own directory is what finds a nested install, which is how npm
// lays out a transitive dependency whose version conflicts with a hoisted one.
function findManifest(name, fromDir) {
  for (let dir = fromDir; ; dir = path.dirname(dir)) {
    const manifest = path.join(dir, 'node_modules', name, 'package.json');
    if (fs.existsSync(manifest)) {
      return manifest;
    }
    if (path.dirname(dir) === dir) {
      return null;
    }
  }
}

function vueLibraryAliases() {
  const aliases = { ...VUE_FORWARDERS };
  const seen = new Set(['vue', '@vue/compat', ...BUILD_TOOLING, ...Object.keys(VUE_FORWARDERS)]);
  const queue = [...Object.keys(pkg.dependencies), ...Object.keys(pkg.devDependencies)]
    .map(name => ({ name, from: projectRoot }));

  while (queue.length > 0) {
    const { name, from } = queue.shift();
    if (seen.has(name)) {
      continue;
    }
    seen.add(name);

    const manifest = findManifest(name, from);
    if (!manifest) {
      continue;
    }
    const meta = JSON.parse(fs.readFileSync(manifest, 'utf8'));

    const dependsOnVue = ['dependencies', 'peerDependencies', 'devDependencies']
      .some(field => meta[field] && meta[field].vue);
    if (!dependsOnVue || SUBPATH_IMPORTED.includes(name)) {
      continue;
    }

    const esm = meta.module || meta.exports?.['.']?.import || meta.exports?.['.']?.module;
    if (typeof esm === 'string') {
      aliases[name] = path.resolve(path.dirname(manifest), esm);
    }

    // Aliasing a package only moves the split one level down if its own
    // dependencies reach Vue as well, so follow those too. `@tanstack/vue-form`
    // is the case in point: it touches Vue through `@tanstack/vue-store`, which
    // in turn imports it through `vue-demi`.
    for (const dependency of Object.keys(meta.dependencies || {})) {
      queue.push({ name: dependency, from: path.dirname(manifest) });
    }
  }

  return aliases;
}

const vueLibraries = vueLibraryAliases();

export default mergeConfig(
  viteConfig({ command: 'serve', mode: 'test' }),
  defineConfig({
    resolve: {
      alias: vueLibraries
    },
    test: {
      environment: 'jsdom',
      globals: true,
      clearMocks: true,
      silent: 'passed-only',
      include: ['tests/unit/**/*.js'],
      setupFiles: ['tests/setup.js'],
      reporters: ['default'],
      server: {
        deps: {
          // Aliasing alone is not enough: the packages also have to go through
          // Vite's transform pipeline for the `vue` import to be rewritten.
          inline: [...Object.keys(vueLibraries), ...SUBPATH_IMPORTED]
        }
      },
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

/*
* Copyright (c) 2009-2022. Authors: see NOTICE file.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

/**
 * Compatibility adapter exposing the @vue/test-utils v1 mounting options on
 * top of test-utils v2, so the existing test suites keep working (mapped in
 * jest.config.js via moduleNameMapper). It translates:
 * - propsData -> props
 * - mocks / stubs / provide / directives -> global.*
 * - localVue (from createLocalVue) -> global.plugins / components / directives
 * - store / router options -> global.plugins
 * - computed / methods overrides -> global.mixins (best effort)
 * - wrapper.destroy() -> wrapper.unmount()
 */

// direct path to bypass the moduleNameMapper redirection of '@vue/test-utils'
const vtu = require('../../node_modules/@vue/test-utils');

function createLocalVue() {
  const registry = {plugins: [], components: {}, directives: {}, mixins: []};
  return {
    __isLocalVue: true,
    __registry: registry,
    use(plugin, ...options) {
      registry.plugins.push(options.length ? [plugin, ...options] : plugin);
      return this;
    },
    component(name, def) {
      registry.components[name] = def;
      return this;
    },
    directive(name, def) {
      registry.directives[name] = def;
      return this;
    },
    mixin(mixin) {
      registry.mixins.push(mixin);
      return this;
    },
    filter() {
      // template filters no longer exist in Vue 3; components were migrated off them
      return this;
    },
    prototype: {}
  };
}

function adaptOptions(options = {}) {
  const {
    propsData, props,
    mocks, stubs, provide, directives,
    localVue, store, router,
    computed, methods,
    attachToDocument,
    ...rest
  } = options;

  const g = {...(rest.global || {})};
  const merge = (key, value) => {
    if (value === undefined) {
      return;
    }
    if (Array.isArray(value)) {
      g[key] = [...(g[key] || []), ...value];
    } else {
      g[key] = {...(g[key] || {}), ...value};
    }
  };

  merge('mocks', mocks);
  merge('stubs', stubs);
  merge('provide', provide);
  merge('directives', directives);

  if (localVue && localVue.__isLocalVue) {
    merge('plugins', localVue.__registry.plugins);
    merge('components', localVue.__registry.components);
    merge('directives', localVue.__registry.directives);
    merge('mixins', localVue.__registry.mixins);
  }
  if (store) {
    if (typeof store.install === 'function') {
      merge('plugins', [store]); // real Vuex store
    } else {
      merge('mocks', {$store: store}); // plain mock object
    }
  }
  if (router) {
    merge('plugins', [router]);
  }
  const adapted = {...rest, global: g};
  if (props || propsData) {
    adapted.props = props || propsData;
  }
  if (attachToDocument) {
    adapted.attachTo = document.body;
  }
  // VTU1 allowed overriding computed/methods via mounting options; a mixin
  // cannot emulate that (component options win), so merge into a component copy
  adapted.__overrides = (computed || methods) ? {computed, methods} : null;
  return adapted;
}

function applyOverrides(component, overrides) {
  if (!overrides) {
    return component;
  }
  const copy = {...component};
  if (overrides.computed) {
    copy.computed = {...(component.computed || {}), ...overrides.computed};
  }
  if (overrides.methods) {
    copy.methods = {...(component.methods || {}), ...overrides.methods};
  }
  return copy;
}

function wrapWrapper(wrapper) {
  if (wrapper && !wrapper.destroy) {
    wrapper.destroy = (...args) => wrapper.unmount(...args);
  }
  if (wrapper && wrapper.vm) {
    // VTU1 setData could also hit writable computeds / instance properties;
    // VTU2 merges into $data only and throws for unknown keys
    const originalSetData = wrapper.setData.bind(wrapper);
    const isPlain = value => value === null || typeof value !== 'object'
      || Array.isArray(value) || value.constructor === Object;
    wrapper.setData = async data => {
      const dataKeys = {};
      for (const [key, value] of Object.entries(data)) {
        if (wrapper.vm.$data && key in wrapper.vm.$data && isPlain(value)) {
          dataKeys[key] = value; // VTU2 setData deep-merges: fine for plain values
        } else {
          wrapper.vm[key] = value; // computed setters / class instances (File, ...)
        }
      }
      if (Object.keys(dataKeys).length) {
        await originalSetData(dataKeys);
      } else {
        await wrapper.vm.$nextTick();
      }
    };
  }
  return wrapper;
}

function adaptedMount(mountFn) {
  return (component, options) => {
    const adapted = adaptOptions(options);
    const overrides = adapted.__overrides;
    delete adapted.__overrides;
    return wrapWrapper(mountFn(applyOverrides(component, overrides), adapted));
  };
}

module.exports = {
  ...vtu,
  createLocalVue,
  mount: adaptedMount(vtu.mount),
  shallowMount: adaptedMount(vtu.shallowMount)
};

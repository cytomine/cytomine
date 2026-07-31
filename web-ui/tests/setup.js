import { config } from '@vue/test-utils';
import { configureCompat } from 'vue';

import optOutBuefyFromVue2Compat from '@/utils/buefy-compat.js';

// Mirror the runtime compat configuration applied in src/main.js. Without it the
// tests would run against Vue 3 semantics while the app runs in Vue 2 mode.
configureCompat({ MODE: 2 });

// Vue Test Utils v2 stopped rendering the default slot of stubbed components.
// The suite was written against v1, where it was rendered, and many assertions
// read content projected into a stubbed child.
config.global.renderStubDefaultSlot = true;

// Same opt-out src/main.js applies, so Buefy components behave in the tests the
// way they do in the app.
optOutBuefyFromVue2Compat();

// Since ol 6, `ol/Map` observes its target with a ResizeObserver, which jsdom
// does not implement. Nothing measures anything in a test, so an inert stub is
// enough to let the viewer components mount.
if (typeof globalThis.ResizeObserver === 'undefined') {
  globalThis.ResizeObserver = class ResizeObserver {
    observe() {}
    unobserve() {}
    disconnect() {}
  };
}

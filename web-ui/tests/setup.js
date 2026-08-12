import { config } from '@vue/test-utils';

// Vue Test Utils v2 stopped rendering the default slot of stubbed components.
// The suite was written against v1, where it was rendered, and many assertions
// read content projected into a stubbed child.
config.global.renderStubDefaultSlot = true;

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

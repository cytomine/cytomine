import { config } from '@vue/test-utils';

// Vue Test Utils v2 no longer renders the default slot of stubbed components by
// default (VTU v1 did). Many component tests stub child components and assert on
// their slot content, so restore the v1 behaviour globally.
config.global.renderStubDefaultSlot = true;

// jsdom does not implement ResizeObserver, which OpenLayers (via vue3-openlayers)
// relies on. Provide a no-op polyfill.
if (typeof globalThis.ResizeObserver === 'undefined') {
  globalThis.ResizeObserver = class ResizeObserver {
    observe() {}
    unobserve() {}
    disconnect() {}
  };
}

import * as vueColor from 'vue-color';

// This is a temporary fix (see src/utils/buefy-compat.js for the full rationale).

/**
 * vue-color 3 is a plain Vue 3 library, but `configureCompat({MODE: 2})` in
 * `src/main.js` is global: @vue/compat applies its Vue 2 behaviours to every
 * component in the tree, vue-color's pickers included, and several break it.
 * Per-component `compatConfig: {MODE: 3}` opts a component out of all of them,
 * which is what vue-color wants, while the app's own components keep MODE 2.
 *
 * Concretely, mounting `<SketchPicker>` under MODE 2 crashed:
 *
 * - RENDER_FUNCTION treats the picker's Vue 3 render function as a legacy Vue 2
 *   one and converts it, mangling the output.
 * - ATTR_FALSE_VALUE / attribute coercion then tries to set an attribute to an
 *   object, throwing "Cannot convert object to primitive value" in setAttribute.
 *
 * This whole file goes away with @vue/compat itself, once MODE 2 can be turned
 * off globally.
 */
export default function optOutVueColorFromVue2Compat() {
  for (const exported of Object.values(vueColor)) {
    // Skip the exported `tinycolor` helper (a function), keep the components.
    if (exported && typeof exported === 'object') {
      exported.compatConfig = { ...exported.compatConfig, MODE: 3 };
    }
  }
}

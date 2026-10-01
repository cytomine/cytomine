/**
 * Replaces vuelayers' global `IdentityMap`, scoped to one map.
 *
 * vuelayers let a component name a source or a feature collection with an
 * `ident` prop and let any other component resolve it by that string — that is
 * how `SelectInteraction` handed its selected-feature collection to the
 * edit interactions (`select-target-<index>`). `vue3-openlayers` only passes ol
 * objects down through `provide`/`inject`, which does not reach a *sibling*
 * component, so the viewer keeps a small registry of its own.
 *
 * Unlike vuelayers' version this one is per-viewer rather than global, so two
 * open images cannot collide, and the keys stay `select-target-<index>` only
 * because the store and the templates already speak that name.
 */
import { markRaw, reactive } from 'vue';

export const VIEWER_CONTEXT = 'cytomineViewerOl';

export function createViewerContext() {
  // Reactive so that a component rendering before the target is registered
  // re-renders once it appears; `markRaw` so that Vue never deep-proxies an ol
  // object (see the note in `CytomineImage.vue`).
  const targets = reactive({});

  return {
    targets,

    register(name, target) {
      targets[name] = markRaw(target);
    },

    unregister(name) {
      delete targets[name];
    },

    resolve(name) {
      return targets[name] || null;
    }
  };
}

/**
 * `inject` option for a component that needs the registry.
 */
export const injectViewerContext = {
  viewerContext: { from: VIEWER_CONTEXT, default: null }
};

/**
 * vee-validate 2 installs itself with `new Vue()`, which has no equivalent in
 * Vue 3, so the plugin cannot be installed in a test app. Until the app moves to
 * a Vue 3 compatible validation library, tests mount components that use
 * `v-validate` with the pieces below instead of the real plugin.
 */
export const veeValidateDirectives = {
  validate: {}
};

export function veeValidateMocks({ valid = true, errors = {} } = {}) {
  return {
    errors: {
      has: field => field in errors,
      first: field => errors[field],
      any: () => Object.keys(errors).length > 0,
      all: () => Object.values(errors),
    },
    $validator: {
      validate: () => Promise.resolve(valid),
      validateAll: () => Promise.resolve(valid),
    },
  };
}

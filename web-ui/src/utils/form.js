import i18n from '@/lang.js';

/**
 * The validation rules the app used to get from vee-validate, reimplemented as
 * `@tanstack/vue-form` field validators. TanStack Form ships no rule DSL of its
 * own: a validator is any function taking `{value, fieldApi}` and returning an
 * error message, or nothing when the value is valid.
 *
 * The messages still come from the `validations.messages.*` keys the four
 * locales already carry. vee-validate interpolated them with
 * `[fieldName, ...ruleParams]`, which is why `min` reads `{1}` and not `{0}`;
 * that argument order is kept so the translations need no change.
 */
function message(rule, field, params = []) {
  return i18n.global.t(`validations.messages.${rule}`, [field, ...params]);
}

/**
 * vee-validate skipped every rule but `required` on an empty value, so that a
 * field could be optional yet still constrained once filled in. Each rule below
 * reproduces that by passing empty values.
 */
function isEmpty(value) {
  if (Array.isArray(value)) {
    return value.length === 0;
  }
  if (value === null || value === undefined) {
    return true;
  }
  return String(value).trim() === '';
}

const emailPattern = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
const decimalPattern = /^[-+]?\d*(\.\d+)?$/;

export function required({ value, fieldApi }) {
  return isEmpty(value) ? message('required', fieldApi.name) : undefined;
}

export function email({ value, fieldApi }) {
  return isEmpty(value) || emailPattern.test(String(value).trim())
    ? undefined
    : message('email', fieldApi.name);
}

export function decimal({ value, fieldApi }) {
  return isEmpty(value) || decimalPattern.test(String(value).trim())
    ? undefined
    : message('decimal', fieldApi.name);
}

export function positive({ value, fieldApi }) {
  return isEmpty(value) || Number(value) > 0
    ? undefined
    : message('positive', fieldApi.name);
}

export function min(length) {
  return ({ value, fieldApi }) => (isEmpty(value) || String(value).length >= length
    ? undefined
    : message('min', fieldApi.name, [length]));
}

/**
 * Combines rules into the single function a TanStack validator slot takes,
 * reporting the first failure only — vee-validate's `fastExit` behaviour, and
 * what the `b-field` message slot can display anyway.
 */
export function rules(...validators) {
  return params => {
    for (const validator of validators) {
      const error = validator(params);
      if (error) {
        return error;
      }
    }
    return undefined;
  };
}

/**
 * Drop-in for `this.$validator.validateAll()`: runs every field's validators
 * with the `submit` cause and reports whether the form may be saved.
 */
export async function validateForm(form) {
  await form.validateAllFields('submit');
  return form.state.isFieldsValid;
}

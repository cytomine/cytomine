import i18n from '@/lang.js';

function message(rule, field, params = []) {
  return i18n.global.t(`validations.messages.${rule}`, [field, ...params]);
}

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

export async function validateForm(form) {
  await form.validateAllFields('submit');
  return form.state.isFieldsValid;
}

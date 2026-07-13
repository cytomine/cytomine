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

import {reactive} from 'vue';

/**
 * Minimal compatibility layer for the vee-validate 2 API surface used in this
 * codebase (vee-validate 2 does not support Vue 3): the `v-validate`
 * directive, the per-component `errors` bag (has/first/any) and
 * `$validator.validateAll()/reset()`.
 *
 * Field values are read from the `modelValue` prop of the vnode carrying the
 * directive (all validated fields in the codebase use v-model), so validation
 * works for native wrappers (b-input, b-select) and custom components alike.
 */

const messages = {
  required: field => `The ${field} field is required.`,
  decimal: field => `The ${field} field must be numeric.`,
  positive: field => `The ${field} field must be a positive number.`,
  email: field => `The ${field} field must be a valid email.`,
  min: (field, [length]) => `The ${field} field must be at least ${length} characters.`
};

const rules = {
  required: value => {
    if (Array.isArray(value)) {
      return value.length > 0;
    }
    return value !== null && value !== undefined && String(value).trim() !== '';
  },
  decimal: value => {
    if (value === null || value === undefined || value === '') {
      return true;
    }
    return /^-?\d*([.,]\d+)?$/.test(String(value));
  },
  positive: value => {
    if (value === null || value === undefined || value === '') {
      return true;
    }
    return Number(String(value).replace(',', '.')) > 0;
  },
  email: value => {
    if (value === null || value === undefined || value === '') {
      return true;
    }
    return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(String(value));
  },
  min: (value, [length]) => {
    if (value === null || value === undefined || value === '') {
      return true;
    }
    return String(value).length >= Number(length);
  }
};

class ErrorBag {
  constructor() {
    this.items = reactive([]);
  }

  has(field) {
    return this.items.some(item => item.field === field);
  }

  first(field) {
    let item = this.items.find(item => item.field === field);
    return item ? item.msg : undefined;
  }

  any() {
    return this.items.length > 0;
  }

  add(field, msg) {
    this.remove(field);
    this.items.push({field, msg});
  }

  remove(field) {
    let index = this.items.findIndex(item => item.field === field);
    if (index !== -1) {
      this.items.splice(index, 1);
    }
  }

  clear() {
    this.items.splice(0, this.items.length);
  }
}

class Validator {
  constructor(errorBag) {
    this.errors = errorBag;
    this.fields = new Map(); // field name => {rules: String, getValue: Function}
  }

  attach(name, ruleString, getValue) {
    this.fields.set(name, {rules: ruleString, getValue});
  }

  detach(name) {
    this.fields.delete(name);
    this.errors.remove(name);
  }

  validate(name) {
    let field = this.fields.get(name);
    if (!field || !field.rules) {
      this.errors.remove(name);
      return true;
    }

    let value = field.getValue();
    for (let rule of field.rules.split('|')) {
      if (!rule) {
        continue;
      }
      let [ruleName, params] = rule.split(':');
      let ruleFn = rules[ruleName];
      if (!ruleFn) {
        console.warn(`[vee-validate-shim] unknown rule: ${ruleName}`);
        continue;
      }
      if (!ruleFn(value, params ? params.split(',') : [])) {
        let message = messages[ruleName] || (f => `The ${f} field is invalid.`);
        this.errors.add(name, message(name, params ? params.split(',') : []));
        return false;
      }
    }
    this.errors.remove(name);
    return true;
  }

  async validateAll() {
    let valid = true;
    for (let name of this.fields.keys()) {
      if (!this.validate(name)) {
        valid = false;
      }
    }
    return valid;
  }

  reset() {
    this.errors.clear();
  }
}

function getFieldName(el, vnode) {
  return (vnode.props && vnode.props.name) || el.getAttribute('name') || el.dataset.vvName;
}

const validateDirective = {
  mounted(el, binding, vnode) {
    let vm = binding.instance;
    if (!vm.$validator) {
      return;
    }
    let name = getFieldName(el, vnode);
    if (!name) {
      console.warn('[vee-validate-shim] v-validate used without a name attribute', el);
      return;
    }
    el.__vvField = {name, value: vnode.props ? vnode.props.modelValue : undefined};
    vm.$validator.attach(name, binding.value, () => el.__vvField.value);
  },
  updated(el, binding, vnode) {
    let vm = binding.instance;
    if (!vm.$validator || !el.__vvField) {
      return;
    }
    let field = el.__vvField;
    let newValue = vnode.props ? vnode.props.modelValue : undefined;
    let changed = newValue !== field.value;
    field.value = newValue;
    // rules may be dynamic (e.g. UserModal) => keep them up to date
    vm.$validator.attach(field.name, binding.value, () => el.__vvField.value);
    // revalidate live once a field has an error, so it clears when fixed
    if (changed && vm.errors.has(field.name)) {
      vm.$validator.validate(field.name);
    }
  },
  unmounted(el, binding) {
    let vm = binding.instance;
    if (vm && vm.$validator && el.__vvField) {
      vm.$validator.detach(el.__vvField.name);
    }
  }
};

export default {
  install(app) {
    app.directive('validate', validateDirective);
    app.mixin({
      beforeCreate() {
        let errorBag = new ErrorBag();
        this.errors = errorBag;
        this.$validator = new Validator(errorBag);
      }
    });
  }
};

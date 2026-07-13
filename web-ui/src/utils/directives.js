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
 * Vue 3 replacements for v-click-outside-x and vue-shortkey (both Vue 2 only),
 * limited to the API surface used in this codebase.
 */

export const clickOutside = {
  beforeMount(el, binding) {
    el.__clickOutsideHandler = event => {
      if (el !== event.target && !el.contains(event.target)) {
        binding.value(event);
      }
    };
    document.addEventListener('click', el.__clickOutsideHandler, true);
  },
  updated(el, binding) {
    el.__clickOutsideBinding = binding.value;
  },
  unmounted(el) {
    document.removeEventListener('click', el.__clickOutsideHandler, true);
    delete el.__clickOutsideHandler;
  }
};

// selectors on which shortcuts must not fire (same list as previous vue-shortkey config)
const SHORTKEY_PREVENTED = [
  'input[type=text]',
  'input[type=password]',
  'input[type=search]',
  'input[type=email]',
  'textarea',
  '.ql-editor'
];

const KEY_ALIASES = {
  ' ': 'space',
  'escape': 'esc',
  'delete': 'del'
};

function comboFromEvent(event) {
  let keys = [];
  if (event.ctrlKey) {
    keys.push('ctrl');
  }
  if (event.metaKey) {
    keys.push('meta');
  }
  if (event.shiftKey) {
    keys.push('shift');
  }
  if (event.altKey) {
    keys.push('alt');
  }
  let key = event.key.toLowerCase();
  if (!['control', 'meta', 'shift', 'alt'].includes(key)) {
    keys.push(KEY_ALIASES[key] || key);
  }
  return keys;
}

function sameCombo(combo, expected) {
  if (!Array.isArray(expected) || expected.length !== combo.length) {
    return false;
  }
  let normalized = expected.map(k => k.toLowerCase());
  return combo.every(k => normalized.includes(k));
}

export const shortkey = {
  beforeMount(el, binding) {
    el.__shortkeyValue = binding.value;
    el.__shortkeyHandler = event => {
      if (event.repeat) {
        return;
      }
      if (SHORTKEY_PREVENTED.some(selector => event.target.matches && event.target.matches(selector))) {
        return;
      }
      let value = el.__shortkeyValue;
      if (!value) {
        return;
      }
      let combo = comboFromEvent(event);
      let mappings = Array.isArray(value) ? {'': value} : value;
      for (let srcKey in mappings) {
        if (sameCombo(combo, mappings[srcKey])) {
          event.preventDefault();
          event.stopPropagation();
          let customEvent = new CustomEvent('shortkey', {bubbles: false});
          customEvent.srcKey = srcKey;
          el.dispatchEvent(customEvent);
          return;
        }
      }
    };
    document.addEventListener('keydown', el.__shortkeyHandler);
  },
  updated(el, binding) {
    el.__shortkeyValue = binding.value;
  },
  unmounted(el) {
    document.removeEventListener('keydown', el.__shortkeyHandler);
    delete el.__shortkeyHandler;
    delete el.__shortkeyValue;
  }
};

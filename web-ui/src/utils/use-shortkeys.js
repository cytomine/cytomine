import { toValue } from 'vue';
import { onKeyStroke } from '@vueuse/core';

const MODIFIERS = ['ctrl', 'alt', 'shift', 'meta'];
const SPECIAL_KEYS = { delete: 'del', escape: 'esc', ' ': 'space' };

function keyName(event) {
  const key = event.key.toLowerCase();
  return SPECIAL_KEYS[key] || key;
}

function activeModifiers(event) {
  return MODIFIERS.filter(mod => event[`${mod}Key`]);
}

function isTypingTarget(target) {
  if (!target) {
    return false;
  }
  if (target.tagName === 'TEXTAREA') {
    return true;
  }
  if (target.tagName === 'INPUT') {
    const type = (target.getAttribute('type') || 'text').toLowerCase();
    return ['text', 'password', 'search', 'email'].includes(type);
  }
  return !!(target.closest && target.closest('.ql-editor'));
}

function matches(combo, key, mods) {
  const comboKey = combo.find(k => !MODIFIERS.includes(k));
  const comboMods = combo.filter(k => MODIFIERS.includes(k));
  return comboKey === key
    && comboMods.length === mods.length
    && comboMods.every(mod => mods.includes(mod));
}

export default function useShortkeys(mapping, onTrigger) {
  onKeyStroke((event) => {
    if (isTypingTarget(event.target)) {
      return;
    }
    const map = toValue(mapping);
    const key = keyName(event);
    const mods = activeModifiers(event);
    for (const srcKey in map) {
      const combo = map[srcKey];
      if (combo && combo.length && matches(combo, key, mods)) {
        event.preventDefault();
        onTrigger(srcKey, event);
        return;
      }
    }
  }, { dedupe: true, target: window });
}

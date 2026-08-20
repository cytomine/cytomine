import { onKeyStroke } from '@vueuse/core';

const DEFAULT_PREVENT = Object.freeze([
  'input[type=text]',
  'input[type=password]',
  'input[type=search]',
  'input[type=email]',
  'textarea',
  '.ql-editor',
]);

const MODIFIERS = Object.freeze(['ctrl', 'shift', 'alt', 'meta']);

const KEY_ALIASES = Object.freeze({
  esc: 'escape',
  del: 'delete',
  space: ' ',
});

function normalizeKey(key) {
  const lower = String(key).toLowerCase();
  return KEY_ALIASES[lower] || lower;
}

function comboMatches(event, combo) {
  if (event.ctrlKey !== combo.includes('ctrl')) {
    return false;
  }
  if (event.shiftKey !== combo.includes('shift')) {
    return false;
  }
  if (event.altKey !== combo.includes('alt')) {
    return false;
  }
  if (event.metaKey !== combo.includes('meta')) {
    return false;
  }

  const mainKeys = combo.filter(token => !MODIFIERS.includes(token));
  const eventKey = normalizeKey(event.key);
  return mainKeys.length > 0 && mainKeys.every(token => normalizeKey(token) === eventKey);
}

function shouldPrevent(target, selectors) {
  return !!target
    && typeof target.matches === 'function'
    && selectors.some(selector => target.matches(selector));
}

export function useShortkeys(mapping, handler, options = {}) {
  const { once = true, prevent = DEFAULT_PREVENT } = options;
  const resolveMapping = () => (typeof mapping === 'function' ? mapping() : mapping);

  return onKeyStroke(event => {
    if (once && event.repeat) {
      return; // ignore OS key auto-repeat
    }
    if (shouldPrevent(event.target, prevent)) {
      return; // typing in an input/editor
    }

    const map = resolveMapping() || {};
    for (const srcKey of Object.keys(map)) {
      if (comboMatches(event, map[srcKey])) {
        event.preventDefault();
        handler(srcKey, event);
        break;
      }
    }
  });
}

export default useShortkeys;

const STORE = '__clickOutside__';

function isOutside(el, target) {
  return el !== target && !el.contains(target);
}

function eventName(binding) {
  return binding.arg || 'click';
}

export default {
  mounted(el, binding) {
    const event = eventName(binding);
    const capture = !!binding.modifiers.capture;
    const store = el[STORE] || (el[STORE] = {});

    const entry = { handler: binding.value, capture };
    entry.listener = (e) => {
      if (typeof entry.handler === 'function' && isOutside(el, e.target)) {
        entry.handler(e);
      }
    };

    store[event] = entry;
    document.addEventListener(event, entry.listener, capture);
  },

  updated(el, binding) {
    const entry = el[STORE] && el[STORE][eventName(binding)];
    if (entry) {
      entry.handler = binding.value;
    }
  },

  unmounted(el, binding) {
    const event = eventName(binding);
    const entry = el[STORE] && el[STORE][event];
    if (entry) {
      document.removeEventListener(event, entry.listener, entry.capture);
      delete el[STORE][event];
      if (Object.keys(el[STORE]).length === 0) {
        delete el[STORE];
      }
    }
  }
};

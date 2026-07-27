const wrappedListeners = new WeakMap();

class EventBus extends EventTarget {
  on(type, listener) {
    let byType = wrappedListeners.get(listener);
    if (!byType) {
      byType = new Map();
      wrappedListeners.set(listener, byType);
    }

    const wrapped = (event) => listener(...event.detail);
    byType.set(type, wrapped);
    this.addEventListener(type, wrapped);
  }

  off(type, listener) {
    const byType = wrappedListeners.get(listener);
    const wrapped = byType && byType.get(type);
    if (wrapped) {
      this.removeEventListener(type, wrapped);
      byType.delete(type);
    }
  }

  emit(type, ...args) {
    this.dispatchEvent(new CustomEvent(type, {detail: args}));
  }
}

export default new EventBus();

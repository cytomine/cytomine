export default {
  namespaced: true,

  state() {
    return {
      stores: [],
    };
  },

  mutations: {
    set(state, stores) {
      state.stores = stores;
    },

    add(state, store) {
      state.stores.push(store);
    },

    delete(state, store) {
      state.stores = state.stores.filter(s => s.id !== store.id);
    },

    reset(state) {
      Object.assign(state, {stores: []});
    }
  },

  getters: {
    stores: (state) => state.stores,
  },
};

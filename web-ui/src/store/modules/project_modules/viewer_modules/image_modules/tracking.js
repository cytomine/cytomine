export default {
  state() {
    return {
      broadcast: false,
      trackedUser: null
    };
  },

  mutations: {
    setBroadcast(state, value) {
      state.broadcast = value;
    },

    setTrackedUser(state, idUser) {
      state.trackedUser = idUser;
    },
  }
};

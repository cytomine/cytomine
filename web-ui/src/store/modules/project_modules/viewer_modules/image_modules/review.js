export default {
  state() {
    return {
      reviewMode: false
    };
  },

  mutations: {
    setReviewMode(state, value) {
      state.reviewMode = value;
    }
  },

  actions: {
    setImageInstance({commit}) { // when image is changed, reset reviewMode value
      commit('setReviewMode', false);
    },
  }
};

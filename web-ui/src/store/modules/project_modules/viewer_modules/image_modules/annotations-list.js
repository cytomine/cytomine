import Vue from 'vue';

export default {
  state() {
    return {
      open: false,
      displayType: 'TERM', // TERM, TRACK
      currentPages: {}, // mapping of type {idProp: currentPage}
      selectedTermsIds: [],
      selectedTracksIds: []
    };
  },

  mutations: {
    setShowAnnotationsList(state, value) {
      state.open = value;
    },
    setDisplayType(state, value) {
      state.displayType = value;
    },
    setCurrentPage(state, {prop, page}) {
      Vue.set(state.currentPages, prop, page);
    },
    setSelectedTermsIds(state, termsIds) {
      state.selectedTermsIds = termsIds;
    },
    setSelectedTracksIds(state, tracksIds) {
      state.selectedTracksIds = tracksIds;
    }
  }
};

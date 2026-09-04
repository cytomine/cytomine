import Vue from 'vue';

export default {
  namespaced: true,

  state() {
    return {
      previewSize: null,
      categorization: null,
      perPage: 25,
      outlineColor: null,
      regroup: null,

      annotationType: null,
      filters: {
        members: null,
        reviewers: null,
        images: null,
        termsIds: null,
        tracksIds: null,
        tags: null,
        imageGroups: null,
      },
      fromDate: null,
      toDate: null,


      currentPages: {} // mapping of type {idProp: currentPage}
    };
  },

  mutations: {
    setPreviewSize(state, size) {
      state.previewSize = size;
    },

    setCategorization(state, categorization) {
      state.categorization = categorization;
    },

    setPerPage(state, perPage) {
      state.perPage = perPage;
    },

    setOutlineColor(state, color) {
      state.outlineColor = color;
    },

    setAnnotationType(state, type) {
      state.annotationType = type;
    },

    setFilter(state, { filterName, propValue }) {
      state.filters[filterName] = propValue;
    },

    setFromDate(state, date) {
      state.fromDate = date;
    },

    setToDate(state, date) {
      state.toDate = date;
    },

    setRegroup(state, regroup) {
      state.regroup = regroup;
    },

    resetPagesAndFilters(state) {
      for (let key in state.filters) {
        state.filters[key] = null;
      }
      state.fromDate = null;
      state.toDate = null;
      state.regroup = false;
      state.currentPages = {};
    },

    setCurrentPage(state, { prop, page }) {
      Vue.set(state.currentPages, prop, page);
    }
  }
};

function getDefaultState() {
  return {
    searchString: '',
    selectedOntology: null
  };
}

export default {
  namespaced: true,

  state: getDefaultState(),

  mutations: {
    setSelectedOntology(state, idOntology) {
      state.selectedOntology = idOntology;
    },

    setSearchString(state, searchString) {
      state.searchString = searchString;
    },

    resetState(state) {
      Object.assign(state, getDefaultState());
    }
  }
};

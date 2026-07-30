function getDefaultState() {
  return {
    searchString: '',

    filtersOpened: false,
    filters: {
      selectedOntologies: null,
      selectedRoles: null,
      selectedTags: null,
      boundsMembers: null,
      boundsImages: null,
      boundsUserAnnotations: null,
      boundsReviewedAnnotations: null
    },

    currentPage: 1,
    perPage: 10,
    sortField: 'lastActivity',
    sortOrder: 'desc',
    openedDetails: []
  };
}

export default {
  namespaced: true,

  state: getDefaultState(),

  mutations: {
    resetState(state) {
      Object.assign(state, getDefaultState());
    },

    setSearchString(state, searchString) {
      state.searchString = searchString;
    },

    setFiltersOpened(state, value) {
      state.filtersOpened = value;
    },

    setFilter(state, { filterName, propValue }) {
      state.filters[filterName] = propValue;
    },

    setCurrentPage(state, page) {
      state.currentPage = page;
    },

    setPerPage(state, perPage) {
      state.perPage = perPage;
    },

    setSortField(state, field) {
      state.sortField = field;
    },

    setSortOrder(state, order) {
      state.sortOrder = order;
    },

    setOpenedDetails(state, value) {
      state.openedDetails = value;
    }
  },

  getters: {
    nbActiveFilters: state => {
      return Object.values(state.filters).filter(val => val).length; // count the number of not null values
    },

    nbEmptyFilters: state => {
      return Object.values(state.filters).filter(val => val && val.length === 0).length;
    }
  }
};

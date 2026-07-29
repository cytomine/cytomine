function getDefaultState() {
  return {
    searchString: '',

    filtersOpened: false,
    filters: {
      boundsJobs: null,
      boundsSuccessJobs: null,
      onlyLastReleases: true,
      onlyExecutables: true,
    },

    currentPage: 1,
    perPage: 10,
    sort: {
      field: 'name',
      order: 'asc'
    },
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

    setFilter(state, {filterName, propValue}) {
      state.filters[filterName] = propValue;
    },

    setCurrentPage(state, page) {
      state.currentPage = page;
    },

    setPerPage(state, perPage) {
      state.perPage = perPage;
    },

    setSort(state, sort) {
      state.sort = sort;
    },

    setOpenedDetails(state, value) {
      state.openedDetails = value;
    }
  },

  getters: {
    nbActiveFilters: state => {
      return Object.values(state.filters).filter(val => val).length; // count the number of not null values
    }
  }
};

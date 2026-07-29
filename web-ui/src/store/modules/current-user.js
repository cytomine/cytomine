import {Cytomine, MyAccount, User} from '@/api';
import {updateToken} from '@/utils/token-utils';

function getDefaultState() {
  return {
    user: null,
    account: null,
    expandedSidebar: true,
    increment: 0,
    shortTermToken: null
  };
}

export default {
  namespaced: true,

  state: getDefaultState(),

  mutations: {
    setUser(state, user) {
      state.user = user ? user.clone() : null;
    },
    setAccount(state, account) {
      state.account = account ? account.clone() : null;
    },
    setShortTermToken(state, value) {
      state.shortTermToken = value;
    },
    setAdminByNow(state, value) {
      state.user.adminByNow = value;
    },
    setExpandedSidebar(state, val) {
      state.expandedSidebar = val;
    },
    resetState(state) {
      Object.assign(state, getDefaultState());
    },
  },

  actions: {
    async fetchUser({commit}) {
      const [user, account] = await Promise.all([
        User.fetchCurrent(), MyAccount.fetch()
      ]);

      if (user.id) { // fetchCurrent() redirects to home page if user not authenticated => check that id is set
        commit('setUser', user);
      } else {
        commit('setUser', null);
      }

      if (account) {
        commit('setAccount', account);
      } else {
        commit('setAccount', null);
      }
    },

    async updateAccount({dispatch}, account) {
      // Need to be sequential because the token needs to be refreshed to send updated claims to core.
      await account.update();
      await updateToken(-1);
      await dispatch('fetchUser');
    },

    async openAdminSession({commit}) {
      await Cytomine.instance.openAdminSession();
      commit('setAdminByNow', true);
    },
    async closeAdminSession({dispatch}) {
      await Cytomine.instance.closeAdminSession();
      await dispatch('fetchUser');
    },
  },

  getters: {
    currentShortTermToken: (state, _, rootState) => {
      let currentUser = rootState.currentUser || {};
      return currentUser.shortTermToken;
    },
  }

};

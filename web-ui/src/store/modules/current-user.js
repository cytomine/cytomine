import axios from 'axios';
import { Cytomine, MyAccount, User } from '@/api';
import constants from '@/utils/constants';
import { getKeycloak } from '@/keycloak';
import { KeycloakRole } from '@/constants/UserRole';
import { tokenHasResourceRole, updateToken } from '@/utils/token-utils';

const ACTING_AS_STORAGE_KEY = 'cytomine-acting-as';
const ACTING_TOKEN_MIN_VALIDITY_MS = 30 * 1000;

function decodeJwtExpMs(token) {
  try {
    const payload = JSON.parse(atob(token.split('.')[1]));
    return payload.exp ? payload.exp * 1000 : 0;
  } catch (error) {
    return 0;
  }
}

function loadActingFromSession() {
  try {
    const raw = window.sessionStorage.getItem(ACTING_AS_STORAGE_KEY);
    return raw ? JSON.parse(raw) : null;
  } catch (error) {
    return null;
  }
}

function persistActing(actingAs) {
  try {
    if (actingAs) {
      window.sessionStorage.setItem(ACTING_AS_STORAGE_KEY, JSON.stringify(actingAs));
    } else {
      window.sessionStorage.removeItem(ACTING_AS_STORAGE_KEY);
    }
  } catch (error) {
    void error;
  }
}

async function exchangeForActingToken(commit, userId) {
  await updateToken(-1);
  const adminToken = await updateToken();
  const authorization = { Authorization: `Bearer ${adminToken}` };

  const { data } = await axios.post(
    `${constants.CORE_URL}/user/${userId}/connect-as.json`,
    {},
    { headers: authorization }
  );

  const actingAs = {
    token: data.accessToken,
    expiresAt: decodeJwtExpMs(data.accessToken),
    userId: data.userId,
    username: data.username,
    actorUsername: data.actorUsername
  };
  persistActing(actingAs);
  commit('setActingAs', actingAs);
  return data.accessToken;
}

function getDefaultState() {
  return {
    user: null,
    account: null,
    expandedSidebar: true,
    increment: 0,
    shortTermToken: null,
    actingAs: loadActingFromSession()
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
    setActingAs(state, actingAs) {
      state.actingAs = actingAs;
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
    async fetchUser({ commit }) {
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

    async updateAccount({ dispatch }, account) {
      // Need to be sequential because the token needs to be refreshed to send updated claims to core.
      await account.update();
      await updateToken(-1);
      await dispatch('fetchUser');
    },

    async openAdminSession({ commit }) {
      await Cytomine.instance.openAdminSession();
      commit('setAdminByNow', true);
    },
    async closeAdminSession({ dispatch }) {
      await Cytomine.instance.closeAdminSession();
      await dispatch('fetchUser');
    },

    async connectAs({ commit }, userId) {
      return exchangeForActingToken(commit, userId);
    },

    async ensureActingToken({ state, commit }) {
      if (!state.actingAs) {
        return null;
      }
      if (state.actingAs.expiresAt - ACTING_TOKEN_MIN_VALIDITY_MS > Date.now()) {
        return state.actingAs.token;
      }
      try {
        return await exchangeForActingToken(commit, state.actingAs.userId);
      } catch (error) {
        persistActing(null);
        commit('setActingAs', null);
        throw error;
      }
    },

    async exitActingAs({ state, commit }) {
      try {
        await updateToken(-1);
        const adminToken = await updateToken();
        await axios.post(
          `${constants.CORE_URL}/user/${state.actingAs.userId}/connect-as/exit.json`,
          {},
          { headers: { Authorization: `Bearer ${adminToken}` } }
        );
      } catch (error) {
        console.log(error);
      }
      persistActing(null);
      commit('setActingAs', null);
    },
  },

  getters: {
    isActing: state => state.actingAs !== null,
    actingUsername: state => (state.actingAs ? state.actingAs.username : null),
    actingActorUsername: state => (state.actingAs ? state.actingAs.actorUsername : null),
    isEffectiveAdmin: state => {
      if (state.actingAs) {
        return tokenHasResourceRole(state.actingAs.token, KeycloakRole.ADMIN);
      }
      return getKeycloak().hasResourceRole(KeycloakRole.ADMIN);
    },
    currentShortTermToken: (state, _, rootState) => {
      let currentUser = rootState.currentUser || {};
      return currentUser.shortTermToken;
    },
  }

};

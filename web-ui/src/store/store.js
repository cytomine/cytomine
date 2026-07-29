import Vue from 'vue';
import Vuex from 'vuex';

import appStores from './modules/app-stores.js';
import currentUser from './modules/current-user.js';
import currentProject from './modules/current-project.js';
import ontologies from './modules/ontologies.js';
import listProjects from './modules/list-projects.js';

Vue.use(Vuex);
let store = new Vuex.Store({
  actions: {
    logout({ state, commit }) {
      commit('appStores/reset');
      commit('currentUser/resetState');
      commit('currentProject/resetState');
      commit('ontologies/resetState');
      commit('listProjects/resetState');
      for (let key in state.projects) {
        this.unregisterModule(['projects', key]);
      }
    }
  },
  modules: {
    appStores,
    currentUser,
    currentProject,
    ontologies,
    listProjects,
    projects: {
      namespaced: true
    }
  },
  strict: process.env.NODE_ENV !== 'production'
});

export default store;

export function getModuleNamespace(state) { // to update if https://github.com/vuejs/vuex/issues/1244 is implemented
  let pathes = Object.keys(store._modulesNamespaceMap);
  let moduleNamespace = pathes.find(path => store._modulesNamespaceMap[path].context.state === state);
  if (typeof moduleNamespace === 'string') {
    return moduleNamespace.slice(0, -1).split('/');
  }
}

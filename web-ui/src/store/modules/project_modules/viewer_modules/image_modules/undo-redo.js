import {Cytomine} from '@/api';

export default {
  state() {
    return {
      actions: [],
      undoneActions: []
    };
  },

  mutations: {
    resetActions(state) {
      state.actions = [];
      state.undoneActions = [];
    },

    addAction(state, {annot, type}) {
      let action = {annot, type, command: Cytomine.instance.lastCommand};
      state.actions.push(action);
      state.undoneActions = [];
    },

    undoAction(state, opposedAction) {
      state.actions.pop();
      state.undoneActions.push(opposedAction);
    },

    redoAction(state, opposedAction) {
      state.undoneActions.pop();
      state.actions.push(opposedAction);
    }
  },

  actions: {
    setImageInstance({commit}) {
      commit('resetActions');
    }
  }
};

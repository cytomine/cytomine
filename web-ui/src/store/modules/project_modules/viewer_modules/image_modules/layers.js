export default {
  state() {
    return {
      selectedLayers: null
    };
  },

  mutations: {
    addLayer(state, layer) {
      if (!state.selectedLayers) {
        state.selectedLayers = [];
      }
      state.selectedLayers.push({...layer});
    },

    removeLayer(state, indexLayer) {
      state.selectedLayers.splice(indexLayer, 1);
    },

    toggleLayerVisibility(state, indexLayer) {
      let layer = state.selectedLayers[indexLayer];
      layer.visible = !layer.visible;
    },

    toggleLayerDrawOn(state, indexLayer) {
      let layer = state.selectedLayers[indexLayer];
      layer.drawOn = !layer.drawOn;
    },

    filterSelectedLayers(state, idLayers) {
      if (!state.selectedLayers) {
        return;
      }
      state.selectedLayers = state.selectedLayers.filter(layer => idLayers.includes(layer.id));
    }
  },

  actions: {
    async addLayer({commit}, layer) {
      commit('addLayer', layer);
    },

    toggleLayerVisibility({state, commit}, indexLayer) {
      commit('toggleLayerVisibility', indexLayer);
      let layer = state.selectedLayers[indexLayer];
      if (!layer.visible) {
        commit('removeLayerFromSelectedFeatures', {layer});
      }
    },

    removeLayer({state, commit}, indexLayer) {
      let layer = state.selectedLayers[indexLayer];
      commit('removeLayer', indexLayer);
      commit('removeLayerFromSelectedFeatures', {layer});
    }
  },

  getters: {
    selectedLayers: state => state.selectedLayers // expose in getter function because properties module need access to this value
  }
};

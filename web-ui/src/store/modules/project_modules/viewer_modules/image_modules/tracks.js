import {TrackCollection} from '@/api';

export default {
  state() {
    return {
      idImage: 0,
      tracks: [],
    };
  },

  mutations: {
    setIdImage(state, id) {
      state.idImage = id;
    },

    setTracks(state, keys) {
      state.tracks = keys;
    },
  },

  actions: {
    async initialize({commit}, {image}) {
      commit('setIdImage', image.id);

      let [tracks] = await Promise.all([
        fetchTracks(image.id)
      ]);

      commit('setTracks', tracks);
      commit('setWrappedTracks', tracks);
    },

    async setImageInstance({commit, dispatch}, {image}) {
      commit('setIdImage', image.id);
      await dispatch('refreshTracks');
    },

    async refreshTracks({state, commit}) {
      let tracks = await fetchTracks(state.idImage);
      commit('filterTracksNewAnnots', tracks || []);
      commit('setTracks', tracks);
      commit('setWrappedTracks', tracks);
    },

    async refreshData({dispatch}) {
      await dispatch('refreshTracks');
    }
  },

  getters: {
    tracks: state => state.tracks
  }
};

async function fetchTracks(idImage) {
  let data = (await TrackCollection.fetchAll({filterKey: 'imageinstance', filterValue: idImage})).array;
  return data;
}

import listImages from './project_modules/list-images.js';
import listImageGroups from './project_modules/list-image-groups.js';
import listAnnotations from './project_modules/list-annotations.js';

export default {
  namespaced: true,

  state() {
    return {
      id: null,
      name: null
    };
  },

  mutations: {
    setProject(state, project) {
      state.id = project.id;
      state.name = project.name;
    },
  },

  modules: {
    viewers: {
      namespaced: true
    },
    listImages,
    listImageGroups,
    listAnnotations
  }
};

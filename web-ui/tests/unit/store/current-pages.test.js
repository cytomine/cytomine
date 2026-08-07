import { computed } from 'vue';
import { createStore } from 'vuex';

import listAnnotations from '@/store/modules/project_modules/list-annotations.js';
import annotationsList
  from '@/store/modules/project_modules/viewer_modules/image_modules/annotations-list.js';

describe.each([
  ['project_modules/list-annotations', listAnnotations],
  ['viewer_modules/image_modules/annotations-list', annotationsList],
])('%s currentPages', (_name, module) => {

  function buildStore() {
    return createStore({ modules: { subject: { ...module, namespaced: true } } });
  }

  it('should start with no pages recorded', () => {
    expect(buildStore().state.subject.currentPages).toEqual({});
  });

  it('should record a page under a key that did not exist', () => {
    const store = buildStore();

    store.commit('subject/setCurrentPage', { prop: 'term-42', page: 3 });

    expect(store.state.subject.currentPages).toEqual({ 'term-42': 3 });
  });

  it('should overwrite a page already recorded under that key', () => {
    const store = buildStore();

    store.commit('subject/setCurrentPage', { prop: 'term-42', page: 3 });
    store.commit('subject/setCurrentPage', { prop: 'term-42', page: 5 });

    expect(store.state.subject.currentPages).toEqual({ 'term-42': 5 });
  });

  it('should keep pages for other keys', () => {
    const store = buildStore();

    store.commit('subject/setCurrentPage', { prop: 'term-42', page: 3 });
    store.commit('subject/setCurrentPage', { prop: 'track-7', page: 2 });

    expect(store.state.subject.currentPages).toEqual({ 'term-42': 3, 'track-7': 2 });
  });

  it('should notify a watcher that read the key before it existed', () => {
    const store = buildStore();
    const page = computed(() => store.state.subject.currentPages['term-42']);

    expect(page.value).toBeUndefined();
    store.commit('subject/setCurrentPage', { prop: 'term-42', page: 4 });

    expect(page.value).toBe(4);
  });
});

import {createLocalVue, mount} from '@vue/test-utils';
import Buefy from 'buefy';

import AnnotationMultiSelect from '@/components/appengine/forms/fields/array/AnnotationMultiSelect';
import GeometryArrayField from '@/components/appengine/forms/fields/array/GeometryArrayField';

jest.mock('@/api', () => ({
  AnnotationCollection: jest.fn().mockImplementation(() => ({
    fetchAll: jest.fn().mockResolvedValue({array: []}),
  })),
}));

describe('GeometryArrayField.vue', () => {
  let localVue;

  beforeEach(() => {
    localVue = createLocalVue();
    localVue.use(Buefy);
  });

  const createWrapper = () => mount(GeometryArrayField, {
    localVue,
    computed: {
      project: () => ({id: 42}),
    },
    mocks: {
      $store: {
        getters: {
          'currentProject/currentViewer': {images: [{imageInstance: {id: 1}}]},
        },
      },
    },
    stubs: {
      AnnotationMultiSelect: true,
    },
  });

  it('should render an AnnotationMultiSelect', () => {
    const wrapper = createWrapper();
    expect(wrapper.findComponent(AnnotationMultiSelect).exists()).toBe(true);
  });

  it('should wrap the emitted ids in {ids, type: annotation} and re-emit as input', async () => {
    const wrapper = createWrapper();
    const ids = [1, 2, 3];

    wrapper.findComponent(AnnotationMultiSelect).vm.$emit('input', ids);
    await wrapper.vm.$nextTick();

    expect(wrapper.emitted('input')).toBeTruthy();
    expect(wrapper.emitted('input')[0]).toEqual([{ids, type: 'annotation'}]);
  });
});

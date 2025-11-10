import {createLocalVue, mount} from '@vue/test-utils';
import Buefy from 'buefy';

import AnnotationMultiSelect from '@/components/appengine/forms/fields/array/AnnotationMultiSelect';
import SelectableAnnotation from '@/components/annotations/SelectableAnnotation';
import {flushPromises} from '../../../../../../utils';

const mockedAnnotations = [
  {id: 1, name: 'Annotation 1'},
  {id: 2, name: 'Annotation 2'},
];

jest.mock('@/api', () => ({
  AnnotationCollection: jest.fn().mockImplementation(() => ({
    fetchAll: jest.fn().mockResolvedValue({
      array: mockedAnnotations,
    }),
  })),
}));

describe('AnnotationMultiSelect.vue', () => {
  const localVue = createLocalVue();
  localVue.use(Buefy);

  const mockImages = [{imageInstance: {id: 1}}];

  const createWrapper = () => {
    return mount(AnnotationMultiSelect, {
      localVue,
      computed: {
        project: () => ({
          id: 42,
        }),
      },
      mocks: {
        $store: {
          getters: {
            'currentProject/currentViewer': {images: mockImages},
          },
        },
      },
      stubs: {
        AnnotationPreview: true,
      },
    });
  };

  it('should render the loading when the data is fetched', () => {
    const wrapper = createWrapper();

    expect(wrapper.vm.loading).toBe(true);
    expect(wrapper.findComponent(SelectableAnnotation).exists()).toBe(false);
  });

  it('should render the data when the annotations are fetched', async () => {
    const wrapper = createWrapper();
    await flushPromises();

    expect(wrapper.vm.loading).toBe(false);
    const components = wrapper.findAllComponents(SelectableAnnotation);
    expect(components.exists()).toBe(true);
    expect(components.length).toBe(mockedAnnotations.length);
  });

  it('should emit an input event when selecting annotations', async () => {
    const wrapper = createWrapper();

    const selectedAnnotationIds = [42, 1337];
    await wrapper.setData({selectedAnnotationIds: selectedAnnotationIds});

    expect(wrapper.emitted().input).toBeTruthy();
    expect(wrapper.emitted().input[0]).toEqual([selectedAnnotationIds]);
  });
});

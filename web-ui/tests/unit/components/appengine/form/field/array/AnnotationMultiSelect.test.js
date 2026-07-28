import {mount} from '@vue/test-utils';
import Buefy from 'buefy';

import AnnotationMultiSelect from '@/components/appengine/forms/fields/array/AnnotationMultiSelect';
import SelectableAnnotation from '@/components/annotations/SelectableAnnotation';
import {flushPromises} from '../../../../../../utils';

const mockedAnnotations = [
  {id: 1, name: 'Annotation 1'},
  {id: 2, name: 'Annotation 2'},
];

vi.mock('@/api', () => ({
  AnnotationCollection: vi.fn().mockImplementation(function () {
    return {
      fetchAll: vi.fn().mockResolvedValue({
        array: mockedAnnotations,
      }),
    };
  }),
}));

describe('AnnotationMultiSelect.vue', () => {

  const mockImages = [{imageInstance: {id: 1}}];

  const createWrapper = () => {
    return mount(AnnotationMultiSelect, {
      global: {
        plugins: [Buefy],
        mocks: {
          // The `computed` mounting option is gone in Vue Test Utils v2, so the
          // store the `get()` helper reads from has to be mocked instead.
          $store: {
            state: {
              currentProject: {project: {id: 42}},
            },
            getters: {
              'currentProject/currentViewer': {images: mockImages},
            },
          },
        },
        stubs: {
          AnnotationPreview: true,
        }
      }
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
    expect(components.length).toBeGreaterThan(0);
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

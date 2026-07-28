import {shallowMount} from '@vue/test-utils';

import AnnotationSelection from '@/components/annotations/AnnotationSelection';
import CytomineModal from '@/components/utils/CytomineModal';
import {flushPromises} from '../../../utils';

vi.mock('@/api', () => ({
  AnnotationCollection: vi.fn().mockImplementation(function () {
    return {
      fetchPage: vi.fn().mockResolvedValue({
        array: [
          {id: 1, name: 'Annotation 1'},
          {id: 2, name: 'Annotation 2'},
        ]
      }),
    };
  }),
}));

describe('AnnotationSelection.vue', () => {
  const mockAnnotations = [
    {id: 1, name: 'Annotation 1'},
    {id: 2, name: 'Annotation 2'},
  ];
  const mockImages = [{
    imageInstance: {id: 1},
    layers: {selectedLayers: [{id: 101, name: 'Mock Layer 1'}, {id: 102, name: 'Mock Layer 2'}]},
  }];

  const createWrapper = () => {
    return shallowMount(AnnotationSelection, {
      props: {
        active: true,
      },
      data() {
        return {
          loading: false,
          selectedAnnotation: null,
        };
      },
      global: {
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
          $t: (message) => message,
        },
        stubs: {
          AnnotationPreview: true,
          'cytomine-modal': true,
          'b-loading': true,
          'b-pagination': true,
          SelectableAnnotation: true,
        }
      }
    });
  };

  it('should be rendered correctly', () => {
    const wrapper = createWrapper();

    expect(wrapper.exists()).toBe(true);
    expect(wrapper.findComponent(CytomineModal).exists()).toBe(true);
    expect(wrapper.find('.annotation-content').exists()).toBe(true);
  });

  it('should render the loading when the data is fetched', async () => {
    const wrapper = createWrapper();
    await flushPromises();

    wrapper.vm.loading = true;
    await wrapper.vm.$nextTick();

    expect(wrapper.exists()).toBe(true);
    expect(wrapper.findComponent(CytomineModal).exists()).toBe(true);
    expect(wrapper.find('.annotation-content').exists()).toBe(false);
  });

  it('Selecting an annotation should emit the select-annotation event', async () => {
    const wrapper = createWrapper();

    wrapper.vm.selectedAnnotation = mockAnnotations[0];
    await wrapper.vm.selectAnnotation();

    expect(wrapper.emitted('select-annotation')).toBeTruthy();
    expect(wrapper.emitted('select-annotation')[0]).toEqual([mockAnnotations[0]]);
  });

  it('Clicking on cancel annotation should reset selectedAnnotation', async () => {
    const wrapper = createWrapper();

    wrapper.vm.selectedAnnotation = mockAnnotations[0];

    expect(wrapper.vm.selectedAnnotation).toEqual(mockAnnotations[0]);

    wrapper.vm.cancelAnnotation();
    await wrapper.vm.$nextTick();

    expect(wrapper.vm.selectedAnnotation).toBe(null);
    expect(wrapper.emitted('update:active')).toEqual([[false]]);
  });
});

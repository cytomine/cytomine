import {shallowMount} from '@vue/test-utils';

import AnnotationSelection from '@/components/annotations/AnnotationSelection';
import CytomineModal from '@/components/utils/CytomineModal';

jest.mock('@/api', () => ({
  AnnotationCollection: jest.fn().mockImplementation(() => ({
    fetchPage: jest.fn().mockResolvedValue({
      array: [
        {id: 1, name: 'Annotation 1'},
        {id: 2, name: 'Annotation 2'},
      ]
    }),
  })),
}));

describe('AnnotationSelection.vue', () => {
  const mockAnnotations = [
    {id: 1, name: 'Annotation 1'},
    {id: 2, name: 'Annotation 2'},
  ];
  const mockImages = [{imageInstance: {id: 1}}];
  const mockedTerms = [{id: 1, name: 'Term 1'}];

  const createWrapper = (options = {}) => {
    return shallowMount(AnnotationSelection, {
      propsData: {
        active: true,
      },
      computed: {
        project: () => ({
          id: 42,
        }),
        layers: () => [
          {id: 101, name: 'Mock Layer 1'},
          {id: 102, name: 'Mock Layer 2'}
        ]
      },
      mocks: {
        $eventBus: {
          $on: jest.fn(),
          $off: jest.fn(),
          $emit: jest.fn(),
        },
        $store: {
          getters: {
            'currentProject/currentViewer': {images: mockImages},
            'currentProject/terms': options.terms || [],
          },
        },
        $t: (message) => message,
      },
      data() {
        return {
          loading: false,
          selectedAnnotation: null,
        };
      },
      stubs: {
        AnnotationPreview: true,
        'cytomine-modal': true,
        'b-loading': true,
        'b-pagination': true,
        SelectableAnnotation: true,
      },
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

    await wrapper.setData({loading: true});

    expect(wrapper.exists()).toBe(true);
    expect(wrapper.findComponent(CytomineModal).exists()).toBe(true);
    expect(wrapper.find('.annotation-content').exists()).toBe(false);
  });

  it('Selecting an annotation should emit the select-annotation event', async () => {
    const wrapper = createWrapper();

    wrapper.setData({selectedAnnotation: mockAnnotations[0]});
    await wrapper.vm.selectAnnotation();

    expect(wrapper.emitted('select-annotation')).toBeTruthy();
    expect(wrapper.emitted('select-annotation')[0]).toEqual([mockAnnotations[0]]);
  });

  it('Clicking on cancel annotation should reset selectedAnnotation', async () => {
    const wrapper = createWrapper();

    wrapper.setData({selectedAnnotation: mockAnnotations[0]});

    expect(wrapper.vm.selectedAnnotation).toBe(mockAnnotations[0]);

    wrapper.vm.cancelAnnotation();
    await wrapper.vm.$nextTick();

    expect(wrapper.vm.selectedAnnotation).toBe(null);
    expect(wrapper.emitted('update:active')).toEqual([[false]]);
  });

  describe('terms', () => {
    it('should load an empty array when no term is provided', () => {
      const wrapper = createWrapper({terms: []});

      expect(wrapper.vm.terms).toEqual([]);
    });

    it('should load the terms data correctly', async () => {
      const wrapper = createWrapper({terms: mockedTerms});

      expect(wrapper.vm.terms).toEqual(mockedTerms);
    });
  });
});

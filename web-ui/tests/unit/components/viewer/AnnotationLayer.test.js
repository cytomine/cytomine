import { shallowMount } from '@vue/test-utils';

import AnnotationLayer from '@/components/viewer/AnnotationLayer.vue';

vi.mock('ol/format/WKT', () => {
  const WKT = vi.fn().mockImplementation(function () {
    return {
      readFeature: vi.fn(),
      readGeometry: vi.fn(),
    };
  });
  return { __esModule: true, default: WKT };
});

describe('AnnotationLayer.vue', () => {
  const addFeature = vi.fn();

  const createWrapper = () => shallowMount(
    AnnotationLayer,
    {
      props: {
        index: '0',
        layer: { id: 1, visible: true },
      },
      global: {
        mocks: {
          $store: {
            getters: {
              'currentProject/currentViewer': {
                images: {
                  0: {
                    imageInstance: { id: 10, width: 1000, height: 1000 },
                    activeSlices: [],
                    selectedFeatures: {
                      annotsToSelect: [],
                      selectedFeatures: [],
                    },
                    draw: {
                      ongoingEdit: false,
                    },
                    style: {
                      terms: [],
                      wrappedTracks: [],
                    },
                    properties: {
                      selectedPropertyKey: null,
                      selectedPropertyColor: null,
                    },
                    review: {},
                  }
                },
              },
              'currentProject/imageModule': vi.fn(() => 'mock-module/'),
            },
          },
        },
        stubs: {
          'vl-layer-vector': true,
          // `$refs` is shallow-readonly in Vue 3, so the source the component
          // calls into has to be a real stub rather than something assigned
          // onto `$refs` after mounting.
          'vl-source-vector': {
            name: 'vl-source-vector',
            template: '<div></div>',
            methods: { addFeature },
          },
          'vl-style-func': true,
        }
      }
    },
  );

  describe('addAnnotationHandler', () => {
    beforeEach(() => {
      addFeature.mockClear();
    });

    it('should add feature when annotation belongs to layer', () => {
      const wrapper = createWrapper();
      const feature = { id: 1 };
      wrapper.vm.annotBelongsToLayer = vi.fn().mockReturnValue(true);
      wrapper.vm.createFeature = vi.fn().mockReturnValue(feature);

      wrapper.vm.addAnnotationHandler({ id: 1 });

      expect(wrapper.vm.createFeature).toHaveBeenCalled();
      expect(addFeature).toHaveBeenCalledWith(feature);
    });

    it('should not add feature when annotation does not belong to layer', () => {
      const wrapper = createWrapper();
      wrapper.vm.annotBelongsToLayer = vi.fn().mockReturnValue(false);

      wrapper.vm.addAnnotationHandler({ id: 1 });

      expect(addFeature).not.toHaveBeenCalled();
    });
  });
});

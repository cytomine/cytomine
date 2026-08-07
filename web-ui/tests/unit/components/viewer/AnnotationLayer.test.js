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
  const refresh = vi.fn();
  const setStyle = vi.fn();

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
            commit: vi.fn(),
            dispatch: vi.fn(),
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
              'mock-module/genStyleFunction': vi.fn(),
            },
          },
        },
        stubs: {
          'ol-vector-layer': {
            name: 'ol-vector-layer',
            template: '<div><slot /></div>',
            data: () => ({ vectorLayer: { setStyle } }),
          },
          'ol-source-vector': {
            name: 'ol-source-vector',
            template: '<div></div>',
            data: () => ({ source: { addFeature, refresh } }),
          },
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

  describe('clearFeatures', () => {
    it('should refresh the source so that the loader runs again', () => {
      refresh.mockClear();
      const wrapper = createWrapper();

      wrapper.vm.clearFeatures();

      expect(refresh).toHaveBeenCalled();
    });
  });

  describe('styling', () => {
    it('should set the style on the ol layer, which a prop would not reach', () => {
      setStyle.mockClear();

      createWrapper();

      expect(setStyle).toHaveBeenCalledWith(expect.any(Function));
    });
  });
});

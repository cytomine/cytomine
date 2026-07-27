import {shallowMount} from '@vue/test-utils';

import AnnotationLayer from '@/components/viewer/AnnotationLayer.vue';

vi.mock('ol/format/WKT', () => {
  const WKT = vi.fn().mockImplementation(function () {
    return {
      readFeature: vi.fn(),
      readGeometry: vi.fn(),
    };
  });
  return {__esModule: true, default: WKT};
});

describe('AnnotationLayer.vue', () => {
  const createWrapper = () => shallowMount(
    AnnotationLayer,
    {
      propsData: {
        index: '0',
        layer: {id: 1, visible: true},
      },
      mocks: {
        $eventBus: {
          $on: vi.fn(),
          $off: vi.fn(),
        },
        $store: {
          getters: {
            'currentProject/currentViewer': {
              images: {
                0: {
                  imageInstance: {id: 10, width: 1000, height: 1000},
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
        'vl-source-vector': true,
        'vl-style-func': true,
      },
    },
  );

  describe('addAnnotationHandler', () => {
    it('should add feature when annotation belongs to layer', () => {
      const wrapper = createWrapper();
      const feature = {id: 1};
      wrapper.vm.$refs.olSource = {addFeature: vi.fn()};
      wrapper.vm.annotBelongsToLayer = vi.fn().mockReturnValue(true);
      wrapper.vm.createFeature = vi.fn().mockReturnValue(feature);

      wrapper.vm.addAnnotationHandler({id: 1});

      expect(wrapper.vm.createFeature).toHaveBeenCalled();
      expect(
        wrapper.vm.$refs.olSource.addFeature
      ).toHaveBeenCalledWith(feature);
    });

    it('should not add feature when annotation does not belong to layer', () => {
      const wrapper = createWrapper();
      wrapper.vm.$refs.olSource = {addFeature: vi.fn()};

      wrapper.vm.annotBelongsToLayer = vi.fn().mockReturnValue(false);
      wrapper.vm.addAnnotationHandler({id: 1});

      expect(wrapper.vm.$refs.olSource.addFeature).not.toHaveBeenCalled();
    });
  });
});

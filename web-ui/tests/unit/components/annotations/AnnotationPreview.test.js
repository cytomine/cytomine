import {shallowMount} from '@vue/test-utils';

import AnnotationPreview from '@/components/annotations/AnnotationPreview.vue';

vi.mock('@/api', () => ({
  Cytomine: {
    instance: {
      api: {
        get: vi.fn(() => Promise.resolve({
          data: new Blob(['test image data'], {type: 'image/jpeg'})
        })),
      },
    },
  }
}));

const mockBlobUrl = 'blob:mock-url-12345';

describe('AnnotationPreview.vue', () => {
  const createWrapper = (options) => shallowMount(AnnotationPreview, {
    mocks: {
      $eventBus: mockEventBus,
    },
    stubs: {
      'v-popover': true,
    },
    ...options,
  });

  let mockEventBus;

  beforeEach(() => {
    global.URL.createObjectURL = vi.fn(() => mockBlobUrl);
    global.URL.revokeObjectURL = vi.fn();

    mockEventBus = {
      $on: vi.fn(),
      $off: vi.fn(),
      $emit: vi.fn()
    };
  });

  afterEach(() => {
    vi.clearAllMocks();
  });

  describe('annotation thumbnail', () => {
    it('should fetch and display the annotation thumbnail', async () => {
      const wrapper = createWrapper({
        propsData: {
          annot: {
            annotationCropURL: vi.fn(() => 'http://cytomine.org/crop.jpg'),
          },
        }
      });

      await wrapper.vm.$nextTick();

      expect(global.URL.createObjectURL).toHaveBeenCalled();
      expect(wrapper.vm.imageDataUrl).toBe(mockBlobUrl);
    });

    it('should revoke old URL when fetching new thumbnail', async () => {
      const wrapper = createWrapper({
        propsData: {
          annot: {
            annotationCropURL: vi.fn(() => 'http://cytomine.org/crop.jpg'),
          },
        }
      });

      await wrapper.vm.fetchThumbnail();

      expect(global.URL.revokeObjectURL).toHaveBeenCalledWith(mockBlobUrl);
      expect(global.URL.createObjectURL).toHaveBeenCalledTimes(2);
    });
  });

  describe('preview button', () => {
    it('should display preview button is showDetails is true', () => {
      const wrapper = createWrapper();

      const buttonWrapper = wrapper.findComponent({ref: 'previewButton'});
      expect(buttonWrapper.exists()).toBe(true);
    });

    it('should not display preview button is showDetails is false', () => {
      const wrapper = createWrapper({
        propsData: {
          showDetails: false,
        }
      });

      const buttonWrapper = wrapper.findComponent({ref: 'previewButton'});
      expect(buttonWrapper.exists()).toBe(false);
    });
  });
});

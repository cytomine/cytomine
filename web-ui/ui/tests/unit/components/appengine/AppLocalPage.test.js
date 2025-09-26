import {shallowMount, createLocalVue} from '@vue/test-utils';

import AppLocalPage from '@/components/appengine/AppLocalPage.vue';
import AppCard from '@/components/appengine/AppCard.vue';
import UploadAppButton from '@/components/appengine/UploadAppButton.vue';
import Task from '@/utils/appengine/task';
import {flushPromises} from '../../../utils';

jest.mock('cytomine-client', () => ({
  Cytomine: {
    instance: {
      api: {
        get: jest.fn(),
      },
    },
  },
}));

jest.mock('@/utils/appengine/task');

const BLoading = {
  name: 'BLoading',
  template: '<div class="loading" v-if="active"><slot></slot></div>',
  props: ['active', 'isFullPage']
};

const localVue = createLocalVue();

describe('AppLocalPage.vue', () => {
  const createWrapper = (options = {}) => shallowMount(
    AppLocalPage,
    {
      localVue,
      components: {
        AppCard,
        UploadAppButton,
        'b-loading': BLoading,
      },
      mocks: {
        $t: (key) => key,
      },
      ...options,
    },
  );

  const mockApplications = [
    {id: 1, name: 'App 1', version: '1.0.0'},
    {id: 2, name: 'App 2', version: '2.0.0'},
    {id: 3, name: 'App 3', version: '1.5.0'},
  ];

  beforeEach(() => {
    jest.clearAllMocks();

    jest.spyOn(console, 'error').mockImplementation(() => {});
  });

  afterEach(() => {
    console.error.mockRestore();
  });

  describe('Component initialisation', () => {
    it('should render loading state initially', () => {
      Task.fetchAll.mockImplementation(() => new Promise(() => {}));

      const wrapper = createWrapper();

      expect(wrapper.findComponent(BLoading).exists()).toBe(true);
      expect(wrapper.findComponent(BLoading).props('active')).toBe(true);
      expect(wrapper.findComponent(BLoading).props('isFullPage')).toBe(false);
    });

    it('should initialises with correct data', () => {
      const wrapper = createWrapper();

      expect(wrapper.vm.applications).toEqual([]);
      expect(wrapper.vm.loading).toBe(true);
    });
  });

  describe('Data loading', () => {
    it('should fetch apps on created and updates loading state', async () => {
      Task.fetchAll.mockResolvedValue(mockApplications);

      const wrapper = createWrapper();
      await flushPromises();

      expect(Task.fetchAll).toHaveBeenCalledTimes(1);
      expect(wrapper.vm.applications).toEqual(mockApplications);
      expect(wrapper.vm.loading).toBe(false);
    });

    it('should handle fetch error gracefully', async () => {
      const fetchError = new Error('Failed to fetch applications');
      Task.fetchAll.mockRejectedValue(fetchError);

      const wrapper = createWrapper();
      await flushPromises();

      expect(Task.fetchAll).toHaveBeenCalledTimes(1);
      expect(wrapper.vm.loading).toBe(false);
    });
  });

  describe('Content rendering', () => {
    beforeEach(() => {
      Task.fetchAll.mockResolvedValue(mockApplications);
    });

    it('should not render loading spinner', async () => {
      const wrapper = createWrapper();
      await flushPromises();

      expect(wrapper.find('.panel').exists()).toBe(true);
      expect(wrapper.findComponent(BLoading).exists()).toBe(true);
      expect(wrapper.findComponent(BLoading).props('active')).toBe(false);
    });

    it('should render UploadAppButton in panel heading', async () => {
      const wrapper = createWrapper();
      await flushPromises();

      const uploadButton = wrapper.findComponent(UploadAppButton);

      expect(uploadButton.exists()).toBe(true);
      expect(uploadButton.props('btnFunc')).toBe('upload');
    });

    it('should render the correct number of AppCard components', async () => {
      const wrapper = createWrapper();
      await flushPromises();

      const appCards = wrapper.findAllComponents(AppCard);

      expect(appCards).toHaveLength(mockApplications.length);
    });

    it('should pass correct props to AppCard components', async () => {
      const wrapper = createWrapper();
      await flushPromises();

      const appCards = wrapper.findAllComponents(AppCard);

      appCards.wrappers.forEach((cardWrapper, index) => {
        expect(cardWrapper.props('app')).toEqual(mockApplications[index]);
      });
    });
  });

  describe('Upload handling', () => {
    beforeEach(() => {
      Task.fetchAll.mockResolvedValue(mockApplications);
    });

    it('should handle task upload success event', async () => {
      const updatedApplications = [
        ...mockApplications,
        {id: 4, name: 'New App', version: '1.0.0'}
      ];

      Task.fetchAll.mockResolvedValueOnce(mockApplications)
        .mockResolvedValueOnce(updatedApplications);

      const wrapper = createWrapper();
      await flushPromises();

      const uploadButton = wrapper.findComponent(UploadAppButton);
      uploadButton.vm.$emit('taskUploadSuccess');
      await flushPromises();

      expect(Task.fetchAll).toHaveBeenCalledTimes(2);
      expect(wrapper.vm.applications).toEqual(updatedApplications);
    });

    it('should handle upload fetch error gracefully', async () => {
      Task.fetchAll.mockResolvedValueOnce(mockApplications)
        .mockRejectedValueOnce(new Error('Fetch failed'));

      const wrapper = createWrapper();
      await flushPromises();

      const uploadButton = wrapper.findComponent(UploadAppButton);
      uploadButton.vm.$emit('taskUploadSuccess');
      await flushPromises();

      expect(Task.fetchAll).toHaveBeenCalledTimes(2);
      expect(console.error).toHaveBeenCalledWith(
        'Error fetching tasks after upload:',
        expect.any(Error)
      );
    });
  });

  describe('Edge cases', () => {
    it('should handle empty applications array', async () => {
      Task.fetchAll.mockResolvedValue([]);

      const wrapper = createWrapper();
      await flushPromises();

      const appCards = wrapper.findAllComponents(AppCard);

      expect(appCards).toHaveLength(0);
      expect(wrapper.vm.applications).toEqual([]);
    });

    it('should handle null applications response', async () => {
      Task.fetchAll.mockResolvedValue(null);

      const wrapper = createWrapper();
      await flushPromises();

      expect(wrapper.vm.applications).toBe(null);
    });

    it('should maintain loading state during async operations', async () => {
      let resolvePromise;
      const promise = new Promise((resolve) => {
        resolvePromise = resolve;
      });
      Task.fetchAll.mockReturnValue(promise);

      const wrapper = createWrapper();

      expect(wrapper.vm.loading).toBe(true);
      expect(wrapper.findComponent(BLoading).props('active')).toBe(true);

      resolvePromise(mockApplications);
      await flushPromises();

      expect(wrapper.vm.loading).toBe(false);
    });
  });
});

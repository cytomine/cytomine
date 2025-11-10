import {shallowMount} from '@vue/test-utils';

import AppLocalPage from '@/components/appengine/AppLocalPage.vue';
import Task from '@/utils/appengine/task';
import {flushPromises} from '../../../utils';

jest.mock('@/utils/appengine/task');

const BLoading = {
  name: 'BLoading',
  template: '<div class="loading" v-if="active"><slot></slot></div>',
  props: ['active', 'isFullPage']
};

describe('AppLocalPage.vue', () => {
  const mockApplications = [
    {id: 1, name: 'App 1', version: '1.0.0'},
    {id: 2, name: 'App 2', version: '2.0.0'},
    {id: 3, name: 'App 3', version: '1.5.0'},
  ];

  const createWrapper = (options = {}) => shallowMount(
    AppLocalPage,
    {
      mocks: {
        $t: (key) => key,
      },
      stubs: {
        'b-icon': true,
        'b-loading': BLoading,
        'b-message': true,
      },
      ...options,
    },
  );

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

  it('should not render loading spinner', async () => {
    Task.fetchAll.mockResolvedValue(mockApplications);

    const wrapper = createWrapper();
    await flushPromises();

    expect(wrapper.vm.loading).toBe(false);
    expect(wrapper.findComponent(BLoading).exists()).toBe(true);
    expect(wrapper.findComponent(BLoading).props('active')).toBe(false);
  });

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

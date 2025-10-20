import {createLocalVue, shallowMount} from '@vue/test-utils';
import Buefy from 'buefy';
import Vuex from 'vuex';

import AppStorePage from '@/components/appengine/AppStorePage';
import {Cytomine} from '@/api';
import {flushPromises} from '../../../utils';

jest.mock('@/api', () => ({
  Cytomine: {
    instance: {
      api: {
        get: jest.fn(),
      },
    },
  },
}));

describe('AppStorePage.vue', () => {
  const localVue = createLocalVue();
  localVue.use(Buefy);
  localVue.use(Vuex);

  const mockStores = [
    {host: 'http://test.cytomine.org', name: 'Test Store'},
    {host: 'http://store.cytomine.org', name: 'Cytomine Store'},
  ];

  const createWrapper = (options = {}) => shallowMount(
    AppStorePage,
    {
      localVue,
      mocks: {
        $store: {
          getters: {
            'appStores/stores': mockStores,
          },
        },
        $t: (key) => key,
      },
      ...options,
    },
  );

  it('should fetch tasks from stores on created hook', async () => {
    const tasks = [{
      namespace: 'test-app',
      version: '1.0.0',
      imageUrl: 'https://example.com/image.png',
      name: 'Test App',
      date: '2025-04-11',
      description: 'This is a test app description.'
    }];
    Cytomine.instance.api.get.mockResolvedValue({data: tasks});

    const wrapper = createWrapper();
    await flushPromises();

    const expectedParams = wrapper.vm.stores.map(store => ({
      params: {host: encodeURIComponent(store.host)}
    }));

    const expectedApplications = [];
    wrapper.vm.stores.forEach((store) => {
      tasks.forEach(task => expectedApplications.push({...task, host: store.host}));
    });

    Cytomine.instance.api.get.mock.calls.forEach((call, i) => {
      expect(call[0]).toBe('/stores/tasks');
      expect(call[1]).toEqual(expectedParams[i]);
    });
    expect(wrapper.vm.applications).toEqual(expectedApplications);
  });
});

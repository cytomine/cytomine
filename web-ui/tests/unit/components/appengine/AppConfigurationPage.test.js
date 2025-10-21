import {shallowMount, createLocalVue} from '@vue/test-utils';
import Buefy from 'buefy';
import Vuex from 'vuex';

import AppConfigurationPage from '@/components/appengine/AppConfigurationPage.vue';
import AppStoreAddModal from '@/components/appengine/AppStoreAddModal.vue';
import store from '@/store/store';
import {Cytomine} from '@/api';
import {flushPromises} from '../../../utils';

const localVue = createLocalVue();
localVue.use(Buefy);
localVue.use(Vuex);

const mockNotify = jest.fn();
const mockDialog = {confirm: jest.fn()};

jest.mock('@/api', () => ({
  Cytomine: {
    instance: {
      api: {
        delete: jest.fn(),
        post: jest.fn(),
        put: jest.fn(),
      },
    },
  },
}));

describe('AppConfigurationPage.vue', () => {
  const createWrapper = () => {
    return shallowMount(AppConfigurationPage, {
      localVue,
      store,
      mocks: {
        $notify: mockNotify,
        $buefy: {dialog: mockDialog},
        $t: (key) => key,
      },
      stubs: {
        AppStoreAddModal,
      },
    });
  };

  beforeEach(() => {
    jest.clearAllMocks();
  });

  it('should add a store correctly', async () => {
    const newStore = {id: 2, name: 'Store2', host: 'http://host2.com', default: false};
    Cytomine.instance.api.post.mockResolvedValue({data: newStore});

    const wrapper = createWrapper();
    await wrapper.vm.handleAdd(newStore);

    expect(Cytomine.instance.api.post).toHaveBeenCalledWith('/stores', newStore);
    expect(wrapper.vm.stores).toContainEqual(newStore);
  });

  it('should delete a store correctly', async () => {
    const store = {id: 3, name: 'Store3', host: 'http://host3.com', default: false};
    const wrapper = createWrapper();

    Cytomine.instance.api.delete.mockResolvedValue({});
    mockDialog.confirm.mockImplementation(({onConfirm}) => onConfirm());

    await wrapper.vm.handleDelete(store);
    await flushPromises();

    expect(Cytomine.instance.api.delete).toHaveBeenCalledWith(`/stores/${store.id}`);
    expect(wrapper.vm.stores).not.toContainEqual(store);
    expect(mockNotify).toHaveBeenCalledWith({
      type: 'success',
      text: 'notify-success-app-store-deletion',
    });
  });

  it('should open the add modal', () => {
    const wrapper = createWrapper();
    expect(wrapper.vm.showModal).toBe(false);

    wrapper.setData({showModal: true});
    expect(wrapper.vm.showModal).toBe(true);
  });
});

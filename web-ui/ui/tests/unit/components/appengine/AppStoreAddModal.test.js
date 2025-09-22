import {shallowMount, createLocalVue} from '@vue/test-utils';
import Buefy from 'buefy';

import AppStoreAddModal from '@/components/appengine/AppStoreAddModal.vue';
import CytomineModal from '@/components/utils/CytomineModal.vue';

const localVue = createLocalVue();
localVue.use(Buefy);

describe('AppStoreAddModal.vue', () => {
  const createWrapper = (options = {}) => {
    return shallowMount(AppStoreAddModal, {
      localVue,
      propsData: {
        active: true,
      },
      mocks: {
        $notify: jest.fn(),
        $t: (key) => key,
      },
      stubs: {
        CytomineModal,
      },
      ...options,
    });
  };

  it('should initialise with an empty store', () => {
    const wrapper = createWrapper();
    expect(wrapper.vm.store).toEqual({name: '', host: '', default: false});
  });

  it('should validate form correctly', () => {
    const wrapper = createWrapper();

    expect(wrapper.vm.isFormValid).toBeFalsy();

    wrapper.setData({store: {name: 'Store', host: '', default: false}});
    expect(wrapper.vm.isFormValid).toBeFalsy();

    wrapper.setData({store: {name: 'Store', host: 'http://host.com', default: false}});
    expect(wrapper.vm.isFormValid).toBeTruthy();
  });

  it('should emit add-store and resets form on add', async () => {
    const wrapper = createWrapper();
    const notifyMock = wrapper.vm.$notify;
    wrapper.setData({store: {name: 'Store1', host: 'http://host.com', default: true}});

    await wrapper.vm.add();

    expect(wrapper.emitted()['add-store'][0]).toEqual([
      {name: 'Store1', host: 'http://host.com', default: true},
    ]);
    expect(wrapper.emitted()['update:active'][0]).toEqual([false]);
    expect(wrapper.vm.store).toEqual({name: '', host: '', default: false});
    expect(notifyMock).toHaveBeenCalledWith({
      type: 'success',
      text: 'notify-success-app-store-addition',
    });
  });

  it('should reset form and closes modal on cancel', async () => {
    const wrapper = createWrapper();
    wrapper.setData({store: {name: 'Store1', host: 'http://host.com', default: true}});
    await wrapper.vm.cancel();

    expect(wrapper.vm.store).toEqual({name: '', host: '', default: false});
    expect(wrapper.emitted()['update:active'][0]).toEqual([false]);
  });

  it('should disable add button if form is invalid', () => {
    const wrapper = createWrapper();
    expect(wrapper.vm.isFormValid).toBeFalsy();
    expect(wrapper.html()).toContain('false');

    wrapper.setData({store: {name: 'Store1', host: 'http://host.com', default: false}});
    expect(wrapper.vm.isFormValid).toBeTruthy();
  });
});

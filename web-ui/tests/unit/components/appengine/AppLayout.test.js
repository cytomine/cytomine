import {shallowMount, createLocalVue} from '@vue/test-utils';
import VueRouter from 'vue-router';

import AppLayout from '@/components/appengine/AppLayout.vue';
import AppSidebar from '@/components/appengine/AppSidebar.vue';
import store from '@/store/store';
import {Cytomine} from '@/api';
import {flushPromises} from '../../../utils';

jest.mock('@/utils/constants.js', () => ({
  APPENGINE_ENABLED: true,
}));

jest.mock('@/api', () => ({
  Cytomine: {
    instance: {
      api: {
        get: jest.fn().mockResolvedValue({data: []}),
      },
    },
  },
}));

const BMessage = {
  name: 'BMessage',
  template: '<div><slot name="header"></slot></div>',
  props: ['title', 'type'],
};

const localVue = createLocalVue();
localVue.use(VueRouter);

describe('AppLayout.vue', () => {
  const createWrapper = (options = {}) => {
    return shallowMount(AppLayout, {
      localVue,
      store,
      components: {
        AppSidebar,
        'b-message': BMessage,
      },
      mocks: {
        $t: (key) => key,
      },
      ...options,
    });
  };

  describe('When app engine is enabled', () => {
    it('should render the layout', () => {
      const wrapper = createWrapper();

      expect(wrapper.find('.app-container').exists()).toBe(true);
      expect(wrapper.find('.app-content').exists()).toBe(true);
      expect(wrapper.find('router-view-stub').exists()).toBe(true);
      expect(wrapper.findComponent(AppSidebar).exists()).toBe(true);
    });

    it('should not render the disabled message', () => {
      const wrapper = createWrapper();

      expect(wrapper.findComponent(BMessage).exists()).toBe(false);
    });

    it('should fetch stores on created hook', async () => {
      const storesData = [{id: 1, name: 'Store1', host: 'http://example.com', default: true}];
      Cytomine.instance.api.get.mockResolvedValue({data: storesData});

      createWrapper();
      await flushPromises();

      expect(Cytomine.instance.api.get).toHaveBeenCalledWith('/stores');
    });
  });

  describe('When app engine is disabled', () => {
    const createDisabledWrapper = () => createWrapper({
      data() {
        return {
          appEngineEnabled: false,
        };
      },
    });

    it('should render the disabled message', () => {
      const wrapper = createDisabledWrapper();

      expect(wrapper.find('.app-container').exists()).toBe(false);
      expect(wrapper.find('router-view-stub').exists()).toBe(false);
      expect(wrapper.findComponent(AppSidebar).exists()).toBe(false);
      expect(wrapper.findComponent(BMessage).exists()).toBe(true);
    });

    it('should render correct message props', () => {
      const wrapper = createDisabledWrapper();

      const message = wrapper.findComponent(BMessage);
      expect(message.props('title')).toBe('appengine-not-enabled-title');
      expect(message.props('type')).toBe('is-info');
      expect(message.text()).toBe('appengine-not-enabled-description');
    });
  });
});

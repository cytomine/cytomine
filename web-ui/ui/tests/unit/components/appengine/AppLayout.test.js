import {shallowMount, createLocalVue} from '@vue/test-utils';
import VueRouter from 'vue-router';

import AppLayout from '@/components/appengine/AppLayout.vue';
import AppSidebar from '@/components/appengine/AppSidebar.vue';

jest.mock('@/utils/constants.js', () => ({
  APPENGINE_ENABLED: true,
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

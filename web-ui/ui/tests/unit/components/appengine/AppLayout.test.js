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
  let router;

  const createWrapper = (options = {}) => {
    const defaultOptions = {
      localVue,
      router,
      components: {
        AppSidebar,
        'b-message': BMessage,
      },
      mocks: {
        $t: (key) => key,
      }
    };

    return shallowMount(AppLayout, {
      ...defaultOptions,
      ...options,
    });
  };

  beforeEach(() => {
    router = new VueRouter({
      routes: [
        {path: '/', component: {template: '<div>Home</div>'}},
        {path: '/test', component: {template: '<div>Test</div>'}},
      ],
    });
  });

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
    const createDisabledWrapper = (options = {}) => {
      return createWrapper({
        data() {
          return {
            appEngineEnabled: false,
          };
        },
        ...options,
      });
    };

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

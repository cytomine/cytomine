import {createLocalVue, shallowMount} from '@vue/test-utils';
import Buefy from 'buefy';
import VueRouter from 'vue-router';

import AppCard from '@/components/appengine/AppCard';

jest.mock('@/api', () => ({
  Cytomine: {
    instance: {
      api: {
        post: jest.fn(),
      },
    },
  },
}));

describe('AppCard.vue', () => {
  const localVue = createLocalVue();
  localVue.use(Buefy);
  localVue.use(VueRouter);

  const mockApp = {
    namespace: 'my-app',
    version: '1.0.0',
    imageUrl: 'https://example.com/image.png',
    name: 'Test App',
    date: '2025-04-11',
    description: 'This is a test app description.'
  };

  const createWrapper = (options = {}) => shallowMount(
    AppCard,
    {
      localVue,
      propsData: {
        app: mockApp,
      },
      mocks: {
        $t: (key) => key,
      },
      ...options,
    },
  );

  it('should render the app information', () => {
    const wrapper = createWrapper();

    expect(wrapper.text()).toContain(mockApp.name);
    expect(wrapper.text()).toContain(mockApp.date);
    expect(wrapper.text()).toContain(mockApp.version);
    expect(wrapper.text()).toContain(mockApp.description);
  });

  it('should render the correct image URL', () => {
    const wrapper = createWrapper();

    expect(wrapper.find('img').attributes('src')).toBe(mockApp.imageUrl);
  });

  it('should use the correct router link', () => {
    const wrapper = createWrapper();
    const link = wrapper.findComponent({name: 'RouterLink'});

    const expected = {
      path: `/apps/${mockApp.namespace}/${mockApp.version}`,
      query: {host: undefined},
    };
    expect(link.props('to')).toStrictEqual(expected);
  });

  it('should fallback to placeholder image when imageUrl is not provided', () => {
    const wrapper = createWrapper({
      propsData: {
        app: {...mockApp, imageUrl: ''},
      }
    });

    const expected = 'https://bulma.io/assets/images/placeholders/1280x960.png';
    expect(wrapper.find('img').attributes('src')).toBe(expected);
  });
});

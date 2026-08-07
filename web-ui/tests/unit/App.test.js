import { mount } from '@vue/test-utils';
import { createRouter, createMemoryHistory } from 'vue-router';

import App from '@/App.vue';
import { flushPromises } from '../utils';

vi.mock('axios', () => ({
  default: { get: vi.fn().mockResolvedValue({ data: {} }) },
}));

vi.mock('ifvisible', () => ({
  default: { setIdleDuration: vi.fn(), now: () => true, on: vi.fn() },
}));

vi.mock('@/api', () => ({ Cytomine: class Cytomine {} }));

vi.mock('@/utils/token-utils', () => ({ updateToken: vi.fn().mockResolvedValue(null) }));

vi.mock('@/utils/constants.js', () => ({ default: { IDLE_DURATION: 0, PING_INTERVAL: 100000 } }));

function createPage(name) {
  return {
    name,
    template: `<div class="${name}" />`,
    created() {
      hooks.push(`${name}:created`);
    },
    activated() {
      hooks.push(`${name}:activated`);
    },
  };
}

let hooks = [];

describe('App.vue', () => {
  let router;

  async function mountApp() {
    hooks = [];
    router = createRouter({
      history: createMemoryHistory(),
      routes: [
        { path: '/storage', component: createPage('cytomine-storage') },
        { path: '/projects', component: createPage('list-projects') },
      ],
    });
    await router.push('/storage');
    await router.isReady();

    const wrapper = mount(App, {
      global: {
        plugins: [router],
        stubs: {
          notifications: { template: '<div />' },
          'cytomine-navbar': true,
        },
        mocks: {
          $t: key => key,
          $store: {
            state: { currentUser: { user: { id: 1 }, account: null } },
            commit: vi.fn(),
            dispatch: vi.fn(),
          },
        },
      },
    });
    await flushPromises();

    return wrapper;
  }

  it('should render the routed page', async () => {
    const wrapper = await mountApp();

    expect(wrapper.find('.cytomine-storage').exists()).toBe(true);
  });

  it('should keep the storage page alive across visits', async () => {
    const wrapper = await mountApp();

    await router.push('/projects');
    await flushPromises();
    await router.push('/storage');
    await flushPromises();

    expect(wrapper.find('.cytomine-storage').exists()).toBe(true);
    expect(hooks.filter(hook => hook === 'cytomine-storage:created')).toHaveLength(1);
    expect(hooks.filter(hook => hook === 'cytomine-storage:activated')).toHaveLength(2);
  });

  it('should not keep other pages alive', async () => {
    await mountApp();

    await router.push('/projects');
    await flushPromises();
    await router.push('/storage');
    await flushPromises();
    await router.push('/projects');
    await flushPromises();

    expect(hooks.filter(hook => hook === 'list-projects:created')).toHaveLength(2);
  });
});

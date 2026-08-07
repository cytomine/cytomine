import { mount } from '@vue/test-utils';
import { createRouter, createMemoryHistory } from 'vue-router';

import AppSidebar from '@/components/appengine/AppSidebar.vue';
import store from '@/store/store';
import { flushPromises } from '../../../utils';

const Blank = { template: '<div />' };

function createTestRouter() {
  return createRouter({
    history: createMemoryHistory(),
    linkActiveClass: 'is-active',
    routes: [
      { path: '/', component: Blank },
      {
        path: '/apps',
        component: Blank,
        children: [
          { path: '', component: Blank },
          { path: 'store', component: Blank },
          { path: 'configuration', component: Blank },
        ],
      },
    ],
  });
}

describe('AppSidebar.vue', () => {
  let router;

  async function createWrapper(path) {
    router = createTestRouter();
    await router.push(path);
    await router.isReady();

    return mount(AppSidebar, {
      global: {
        plugins: [store, router],
        mocks: { $t: key => key },
      },
    });
  }

  it('should render every link as a list item wrapping an anchor', async () => {
    const wrapper = await createWrapper('/apps');
    const items = wrapper.findAll('li');

    expect(items).toHaveLength(3);
    expect(items.map(item => item.find('a').attributes('href')))
      .toEqual(['/apps', '/apps/store', '/apps/configuration']);
  });

  it('should not nest the anchor inside another anchor', async () => {
    const wrapper = await createWrapper('/apps');

    expect(wrapper.findAll('a a')).toHaveLength(0);
    expect(wrapper.findAll('ul > li')).toHaveLength(3);
  });

  it('should mark the active item, and mark the list item rather than the anchor', async () => {
    const wrapper = await createWrapper('/apps/store');
    const [installed, appStore, configuration] = wrapper.findAll('li');

    expect(appStore.classes()).toContain('is-active');
    expect(appStore.find('a').classes()).not.toContain('is-active');
    expect(installed.classes()).not.toContain('is-active');
    expect(configuration.classes()).not.toContain('is-active');
  });

  it('should mark the installed-apps item active only on /apps itself', async () => {
    const onStore = await createWrapper('/apps/store');
    expect(onStore.findAll('li')[0].classes()).not.toContain('is-active');

    const onApps = await createWrapper('/apps');
    expect(onApps.findAll('li')[0].classes()).toContain('is-active');
  });

  it('should navigate when a link is clicked', async () => {
    const wrapper = await createWrapper('/apps');

    await wrapper.findAll('li')[1].find('a').trigger('click');
    await flushPromises();

    expect(router.currentRoute.value.path).toBe('/apps/store');
  });
});

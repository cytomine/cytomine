import { mount } from '@vue/test-utils';
import { createRouter, createMemoryHistory } from 'vue-router';

import ProjectSidebar from '@/components/project/ProjectSidebar.vue';
import { flushPromises } from '../../../utils';

const Blank = { template: '<div />' };

// In sidebar order. The configuration key and the path differ for the activity
// tab, which is `project-activities-tab` but lives at /activity.
const TABS = [
  { key: 'images', path: 'images' },
  { key: 'image-groups', path: 'image-groups' },
  { key: 'annotations', path: 'annotations' },
  { key: 'apps', path: 'apps' },
  { key: 'activities', path: 'activity' },
  { key: 'information', path: 'information' },
  { key: 'configuration', path: 'configuration' },
];

// The project section of src/routes.js in miniature: every tab is a child of
// /project/:idProject, which is what an active link has to be matched against.
function createTestRouter() {
  return createRouter({
    history: createMemoryHistory(),
    linkActiveClass: 'is-active',
    routes: [
      {
        path: '/project/:idProject',
        component: Blank,
        children: TABS.map(({ path }) => ({ path, component: Blank })),
      },
    ],
  });
}

describe('ProjectSidebar.vue', () => {
  let router;

  async function createWrapper(path = '/project/7/images') {
    router = createTestRouter();
    await router.push(path);
    await router.isReady();

    return mount(ProjectSidebar, {
      global: {
        plugins: [router],
        mocks: {
          $t: key => key,
          $store: {
            state: {
              currentUser: { expandedSidebar: true },
              // `project-<tab>-tab` is what isTabDisplayed() looks up.
              currentProject: {
                project: { id: 7, name: 'Project 7' },
                configUI: Object.fromEntries(TABS.map(({ key }) => [`project-${key}-tab`, true])),
              },
            },
            commit: vi.fn(),
          },
        },
      },
    });
  }

  it('should render every tab as a list item wrapping an anchor', async () => {
    const wrapper = await createWrapper();
    const items = wrapper.findAll('li');

    expect(items).toHaveLength(TABS.length);
    expect(items.map(item => item.find('a').attributes('href')))
      .toEqual(TABS.map(({ path }) => `/project/7/${path}`));
  });

  it('should not nest the anchor inside another anchor', async () => {
    const wrapper = await createWrapper();

    expect(wrapper.findAll('a a')).toHaveLength(0);
    expect(wrapper.findAll('ul > li')).toHaveLength(TABS.length);
  });

  it('should mark the list item of the current tab, not its anchor', async () => {
    const wrapper = await createWrapper('/project/7/annotations');
    const active = wrapper.findAll('li').filter(item => item.classes().includes('is-active'));

    expect(active).toHaveLength(1);
    expect(active[0].find('a').attributes('href')).toBe('/project/7/annotations');
    expect(active[0].find('a').classes()).not.toContain('is-active');
  });

  it('should navigate when a tab is clicked', async () => {
    const wrapper = await createWrapper();

    await wrapper.findAll('li')[2].find('a').trigger('click');
    await flushPromises();

    expect(router.currentRoute.value.path).toBe('/project/7/annotations');
  });

  it('should render only the tabs the project configuration enables', async () => {
    router = createTestRouter();
    await router.push('/project/7/images');

    const wrapper = mount(ProjectSidebar, {
      global: {
        plugins: [router],
        mocks: {
          $t: key => key,
          $store: {
            state: {
              currentUser: { expandedSidebar: true },
              currentProject: {
                project: { id: 7, name: 'Project 7' },
                configUI: { 'project-images-tab': true, 'project-configuration-tab': true },
              },
            },
            commit: vi.fn(),
          },
        },
      },
    });

    expect(wrapper.findAll('li').map(item => item.find('a').attributes('href')))
      .toEqual(['/project/7/images', '/project/7/configuration']);
  });
});

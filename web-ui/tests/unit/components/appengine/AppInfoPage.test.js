import {createLocalVue, shallowMount} from '@vue/test-utils';
import VueRouter from 'vue-router';

import AppInfoPage from '@/components/appengine/AppInfoPage.vue';
import Task from '@/utils/appengine/task';
import {flushPromises} from '../../../utils';

jest.mock('@/api', () => ({
  Cytomine: {
    instance: {
      api: {
        post: jest.fn(),
      },
    },
  },
}));

jest.mock('@/utils/appengine/task', () => ({
  fetchNamespaceVersion: jest.fn(),
}));

const localVue = createLocalVue();
localVue.use(VueRouter);
const router = new VueRouter();

describe('AppInfoPage.vue', () => {
  const mockTask = {
    name: 'Test App',
    authors: [{first_name: 'John', last_name: 'Doe'}],  // eslint-disable-line
    date: '2025-10-23',
    version: '1.0.0',
    imageUrl: 'https://example.com/image.png',
    description: 'App description here',
  };

  beforeEach(() => {
    Task.fetchNamespaceVersion.mockResolvedValue(mockTask);
  });

  const createWrapper = () => {
    return shallowMount(AppInfoPage, {
      localVue,
      router,
      mocks: {
        $notify: jest.fn(),
        $t: (key) => key,
      },
      stubs: {
        'b-button': {
          props: ['label', 'iconPack', 'iconLeft'],
          template: '<button>{{ label }}</button>',
        },
        'b-collapse': true,
        'b-dropdown': true,
        'b-dropdown-item': true,
        'b-icon': true,
        'b-loading': true,
      },
    });
  };

  it('should render loading overlay when data is being fetched', async () => {
    const wrapper = createWrapper();

    expect(wrapper.vm.loading).toBe(true);
    expect(wrapper.vm.task).toBe(null);
    expect(wrapper.text()).toEqual('');
  });

  it('should render app data when app is fetched', async () => {
    const wrapper = createWrapper();
    await flushPromises();

    expect(wrapper.vm.loading).toBe(false);
    expect(wrapper.vm.task).toBe(mockTask);

    const expectedAuthors = mockTask.authors
      .map(author => `- ${author.first_name} ${author.last_name}`)
      .join('');
    expect(wrapper.text()).toContain(expectedAuthors);
    expect(wrapper.text()).toContain(mockTask.name);
    expect(wrapper.text()).toContain(mockTask.date);
    expect(wrapper.text()).toContain(mockTask.version);
    expect(wrapper.text()).toContain(mockTask.description);
  });

  it('should render action buttons', async () => {
    const wrapper = createWrapper();
    await flushPromises();

    expect(wrapper.text()).toContain('go-back');
    expect(wrapper.text()).toContain('install');
    expect(wrapper.text()).toContain('button-delete');
  });

  it('should render no description when description is missing', async () => {
    Task.fetchNamespaceVersion.mockResolvedValue({
      name: 'Test App',
      authors: [{first_name: 'John', last_name: 'Doe'}],  // eslint-disable-line
      date: '2025-10-23',
      version: '1.0.0',
      imageUrl: 'https://example.com/image.png',
    });
    const wrapper = createWrapper();
    await flushPromises();

    expect(wrapper.text()).toContain('no-description');
    expect(wrapper.text()).not.toContain(mockTask.description);
  });

  it('should render unknown when date is missing', async () => {
    Task.fetchNamespaceVersion.mockResolvedValue({
      name: 'Test App',
      authors: [{first_name: 'John', last_name: 'Doe'}],  // eslint-disable-line
      version: '1.0.0',
      imageUrl: 'https://example.com/image.png',
      description: 'App description here',
    });
    const wrapper = createWrapper();
    await flushPromises();

    expect(wrapper.text()).toContain('unknown');
    expect(wrapper.text()).not.toContain(mockTask.date);
  });
});

import { shallowMount } from '@vue/test-utils';

import AppInfoPage from '@/components/appengine/AppInfoPage.vue';
import Task from '@/utils/appengine/task';
import { flushPromises } from '../../../utils';

vi.mock('@/api', () => ({
  Cytomine: {
    instance: {
      api: {
        post: vi.fn(),
      },
    },
  },
}));

vi.mock('@/utils/appengine/task', () => ({
  default: {
    fetchNamespaceVersion: vi.fn(),
  }
}));

describe('AppInfoPage.vue', () => {
  const mockTask = {
    name: 'Test App',
    authors: [{ firstName: 'John', lastName: 'Doe' }],
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
      global: {
        mocks: {
          $notify: vi.fn(),
          $t: (key) => key,
          // vue-router 3 cannot be installed on Vue 3, so the route the
          // component reads in `created` is mocked directly.
          $route: {
            params: { namespace: 'mock-namespace', version: '1.0.0' },
            query: {},
          },
          $router: { push: vi.fn() },
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
        }
      }
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
    expect(wrapper.vm.task).toEqual(mockTask);

    const expectedAuthors = mockTask.authors
      .map(author => `- ${author.firstName} ${author.lastName}`)
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
      authors: [{ firstName: 'John', lastName: 'Doe' }],
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
      authors: [{ firstName: 'John', lastName: 'Doe' }],
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

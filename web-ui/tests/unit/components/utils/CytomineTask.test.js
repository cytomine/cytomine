import {createLocalVue, shallowMount} from '@vue/test-utils';

import CytomineTask from '@/components/utils/CytomineTask';
import {Task} from '@/api';


vi.mock('@/api', () => ({
  Task: {
    fetch: vi.fn()
  }
}));

describe('CytomineTask.vue', () => {
  const localVue = createLocalVue();

  let wrapper;

  beforeEach(() => {
    vi.clearAllMocks();

    Task.fetch.mockResolvedValue({
      id: 1,
      progress: 50,
      comments: ['Initial comment']
    });

    wrapper = shallowMount(CytomineTask, {
      localVue,
      propsData: {
        task: {
          id: 1,
          progress: 50,
          comments: ['Initial comment']
        },
        refreshInterval: 1000
      }
    });
  });

  afterEach(() => {
    vi.clearAllMocks();
  });

  it('should display the correct progress value', async () => {
    const progressBar = wrapper.find('progress');

    expect(progressBar.attributes('value')).toBe('50');
    expect(progressBar.text()).toContain('50%');
  });

  it('should display the last comment', () => {
    const comment = wrapper.find('p');

    expect(comment.text()).toBe('Initial comment');
  });
});

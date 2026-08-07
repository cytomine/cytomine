import { flushPromises, mount } from '@vue/test-utils';
import Buefy from 'buefy';

import CytomineSearcher from '@/components/search/CytomineSearcher.vue';

const fetchAllLight = vi.fn(async () => [{ id: 3, instanceFilename: 'photo.tif', project: 9, projectName: 'p' }]);
const fetchAll = vi.fn(async () => ({ array: [{ id: 9, name: 'project' }] }));

vi.mock('@/api', () => ({
  ImageInstanceCollection: { fetchAllLight: (...args) => fetchAllLight(...args) },
  ProjectCollection: vi.fn(function () {
    return { fetchAll: (...args) => fetchAll(...args) };
  }),
}));

describe('CytomineSearcher.vue', () => {
  const createWrapper = () => mount(CytomineSearcher, {
    global: {
      plugins: [Buefy],
      stubs: { 'router-link': { template: '<a><slot/></a>' } },
      directives: { 'click-outside': {} },
      mocks: {
        $t: key => key,
        $store: { state: { currentUser: { user: { id: 1 } } } },
      },
    },
  });

  const open = async (wrapper) => {
    await wrapper.find('input').trigger('focus');
    await flushPromises();
    await wrapper.setData({ searchString: 'p' });
  };

  it('loads the results when the search field takes focus', async () => {
    const wrapper = createWrapper();

    await wrapper.find('input').trigger('focus');
    await flushPromises();

    expect(fetchAllLight).toHaveBeenCalled();
    expect(fetchAll).toHaveBeenCalled();
    expect(wrapper.vm.isActive).toBe(true);
  });

  it('shows the dropdown once there is a search string', async () => {
    const wrapper = createWrapper();

    await open(wrapper);

    expect(wrapper.vm.displayResults).toBe(true);
  });

  it('closes the dropdown when a project result is clicked', async () => {
    const wrapper = createWrapper();
    await open(wrapper);

    await wrapper.findAll('.search-results a')[0].trigger('click');

    expect(wrapper.vm.isActive).toBe(false);
  });

  it('closes the dropdown when an image result is clicked', async () => {
    const wrapper = createWrapper();
    await open(wrapper);

    await wrapper.findAll('.search-results a')[1].trigger('click');

    expect(wrapper.vm.isActive).toBe(false);
  });

  it('closes the dropdown when the advanced-search link is clicked', async () => {
    const wrapper = createWrapper();
    await open(wrapper);

    await wrapper.find('.control a').trigger('click');

    expect(wrapper.vm.isActive).toBe(false);
  });

  it('renders the highlighted match markup inside the result links', async () => {
    const wrapper = createWrapper();
    await open(wrapper);

    const links = wrapper.findAll('.search-results a');
    expect(links[0].find('span').html()).toContain('<strong>p</strong>roject');
    expect(links[1].find('span').html()).toContain('<strong>p</strong>hoto.tif');
  });
});

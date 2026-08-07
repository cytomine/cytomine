import { createLocalVue, mount } from '@vue/test-utils';
import Buefy from 'buefy';

import CytomineMultiselect from '@/components/form/CytomineMultiselect.vue';
import MetadataFilter from '@/components/search/MetadataFilter.vue';
import { fetchFacets, searchMetadata } from '@/utils/search';

vi.mock('@/utils/search', () => ({
  fetchFacets: vi.fn(),
  searchMetadata: vi.fn(),
}));

const facets = {
  'block.block_preparation.meaning': {
    'Paraffin wax (substance)': 47.0,
  },
  'slide.staining.stains.compound.meaning': {
    'hematoxylin stain': 47.0,
    'water soluble eosin stain': 32.0,
  },
  'specimens.anatomical_site.meaning': {
    'BONE, STERNUM': 6.0,
    'KIDNEY': 14.0,
    'LARGE INTESTINE, CECUM': 9.0,
    'LIVER': 18.0,
  },
  'specimens.biological_being.sex': {
    'Male': 47.0,
  },
  'specimens.specimen_type.meaning': {
    'Tissue specimen (specimen)': 47.0,
  },
};

const SITE = 'specimens.anatomical_site.meaning';
const SEX = 'specimens.biological_being.sex';
const TYPE = 'specimens.specimen_type.meaning';

const emptySelection = Object.fromEntries(Object.keys(facets).map((key) => [key, []]));

const results = [{ id: 1 }, { id: 2 }];

const advance = async (ms) => vi.advanceTimersByTimeAsync(ms);

const createWrapper = async (options = {}) => {
  const localVue = createLocalVue();
  localVue.use(Buefy);

  const wrapper = mount(MetadataFilter, {
    localVue,
    mocks: {
      $t: (message) => message,
      $tc: (message) => message,
    },
    ...options,
  });
  await advance(0);
  return wrapper;
};

const createSearchedWrapper = async (options = {}) => {
  const wrapper = await createWrapper(options);
  await advance(300);
  searchMetadata.mockClear();
  return wrapper;
};

describe('MetadataFilter.vue', () => {
  beforeEach(() => {
    vi.useFakeTimers();
    fetchFacets.mockResolvedValue(facets);
    searchMetadata.mockResolvedValue(results);
  });

  afterEach(async () => {
    await vi.runAllTimersAsync();
    vi.useRealTimers();
  });

  it('should fetch the facets on creation', async () => {
    const wrapper = await createWrapper();

    expect(fetchFacets).toHaveBeenCalledOnce();
    expect(wrapper.vm.facets).toEqual([
      { key: 'block.block_preparation.meaning', values: ['Paraffin wax (substance)'] },
      {
        key: 'slide.staining.stains.compound.meaning',
        values: ['hematoxylin stain', 'water soluble eosin stain'],
      },
      { key: SITE, values: ['BONE, STERNUM', 'KIDNEY', 'LARGE INTESTINE, CECUM', 'LIVER'] },
      { key: SEX, values: ['Male'] },
      { key: TYPE, values: ['Tissue specimen (specimen)'] },
    ]);
    expect(wrapper.vm.selectedFacets).toEqual(emptySelection);
  });

  it('should render nothing when there is no facet', async () => {
    fetchFacets.mockResolvedValue({});

    const wrapper = await createWrapper();

    expect(wrapper.vm.facets).toEqual([]);
    expect(wrapper.find('h1').exists()).toBe(false);
    expect(wrapper.find('input').exists()).toBe(false);
    expect(wrapper.find('.facet-filters').exists()).toBe(false);
  });

  it('should render a multiselect per facet', async () => {
    const wrapper = await createWrapper();

    const fields = wrapper.findAll('.facet-filters .field');
    expect(fields.length).toBe(5);
    expect(fields.at(0).find('.label').text()).toBe('block.block_preparation.meaning');
    expect(fields.at(2).find('.label').text()).toBe(SITE);

    const selects = wrapper.findAllComponents(CytomineMultiselect);
    expect(selects.length).toBe(5);
    expect(selects.at(0).props('options')).toEqual(['Paraffin wax (substance)']);
    expect(selects.at(0).props('multiple')).toBe(true);
    expect(selects.at(2).props('options')).toEqual([
      'BONE, STERNUM', 'KIDNEY', 'LARGE INTESTINE, CECUM', 'LIVER',
    ]);
  });

  it('should build no filter when no value is selected', async () => {
    const wrapper = await createWrapper();

    expect(wrapper.vm.filters).toEqual([]);
  });

  it('should build an equality filter when a single value is selected', async () => {
    const wrapper = await createWrapper();

    await wrapper.setData({ selectedFacets: { [SEX]: ['Male'] } });

    expect(wrapper.vm.filters).toEqual([`${SEX}:Male`]);
  });

  it('should build an unquoted filter for a single value containing separators', async () => {
    const wrapper = await createWrapper();

    await wrapper.setData({ selectedFacets: { [SITE]: ['LARGE INTESTINE, CECUM'] } });

    expect(wrapper.vm.filters).toEqual([`${SITE}:LARGE INTESTINE, CECUM`]);
  });

  it('should build a disjunction when several values are selected', async () => {
    const wrapper = await createWrapper();

    await wrapper.setData({ selectedFacets: { [SITE]: ['LIVER', 'KIDNEY', 'BONE, STERNUM'] } });

    expect(wrapper.vm.filters).toEqual([
      `(${SITE} = "LIVER" OR ${SITE} = "KIDNEY" OR ${SITE} = "BONE, STERNUM")`,
    ]);
  });

  it('should combine the filters of the selected facets', async () => {
    const wrapper = await createWrapper();

    await wrapper.setData({
      selectedFacets: {
        [SITE]: ['LIVER', 'KIDNEY'],
        [SEX]: ['Male'],
        [TYPE]: ['Tissue specimen (specimen)'],
      },
    });

    expect(wrapper.vm.filters).toEqual([
      `(${SITE} = "LIVER" OR ${SITE} = "KIDNEY")`,
      `${SEX}:Male`,
      `${TYPE}:Tissue specimen (specimen)`,
    ]);
  });

  it('should search without filter once the facets are loaded', async () => {
    const wrapper = await createWrapper();

    expect(searchMetadata).not.toHaveBeenCalled();

    await advance(300);

    expect(searchMetadata).toHaveBeenCalledWith({ query: '', filters: [], limit: 20, offset: 0 });
    expect(wrapper.vm.results).toEqual(results);
    expect(wrapper.vm.searched).toBe(true);
  });

  it('should emit the results of the search', async () => {
    const wrapper = await createSearchedWrapper();

    await wrapper.setData({ selectedFacets: { [SEX]: ['Male'] } });
    await advance(300);

    expect(wrapper.emitted('search').at(-1)).toEqual([results]);
  });

  it('should search when the selected facets change', async () => {
    const wrapper = await createSearchedWrapper();

    await wrapper.setData({ selectedFacets: { [SITE]: ['LIVER', 'KIDNEY'], [SEX]: ['Male'] } });
    await advance(300);

    expect(searchMetadata).toHaveBeenCalledWith({
      query: '',
      filters: [`(${SITE} = "LIVER" OR ${SITE} = "KIDNEY")`, `${SEX}:Male`],
      limit: 20,
      offset: 0,
    });
  });

  it('should search only once when the selected facets change repeatedly', async () => {
    const wrapper = await createSearchedWrapper();

    await wrapper.setData({ selectedFacets: { [SITE]: ['LIVER'] } });
    await advance(100);
    await wrapper.setData({ selectedFacets: { [SITE]: ['LIVER', 'KIDNEY'] } });
    await advance(300);

    expect(searchMetadata).toHaveBeenCalledExactlyOnceWith({
      query: '',
      filters: [`(${SITE} = "LIVER" OR ${SITE} = "KIDNEY")`],
      limit: 20,
      offset: 0,
    });
  });

  it('should debounce the search string typed by the user', async () => {
    const wrapper = await createSearchedWrapper();

    const input = wrapper.find('input');
    input.element.value = 'liv';
    input.trigger('input');
    await advance(400);

    expect(wrapper.vm.searchString).toBe('');

    input.element.value = 'liver';
    input.trigger('input');
    await advance(500);

    expect(wrapper.vm.searchString).toBe('liver');
    expect(searchMetadata).not.toHaveBeenCalled();

    await advance(300);

    expect(searchMetadata).toHaveBeenCalledExactlyOnceWith({
      query: 'liver',
      filters: [],
      limit: 20,
      offset: 0,
    });
  });

  it('should search when the limit changes', async () => {
    const wrapper = await createSearchedWrapper({ propsData: { limit: 5 } });

    wrapper.setProps({ limit: 10 });
    await advance(300);

    expect(searchMetadata).toHaveBeenCalledWith({ query: '', filters: [], limit: 10, offset: 0 });
  });

  it('should search when the offset changes', async () => {
    const wrapper = await createSearchedWrapper();

    wrapper.setProps({ offset: 20 });
    await advance(300);

    expect(searchMetadata).toHaveBeenCalledWith({ query: '', filters: [], limit: 20, offset: 20 });
  });

  it('should reset the search string and the selected facets on clear', async () => {
    const wrapper = await createSearchedWrapper();

    await wrapper.setData({
      searchString: 'liver',
      selectedFacets: { [SITE]: ['LIVER'], [SEX]: ['Male'] },
    });

    wrapper.find('.metadata-filter > button').trigger('click');
    await wrapper.vm.$nextTick();

    expect(wrapper.vm.searchString).toBe('');
    expect(wrapper.vm.selectedFacets).toEqual(emptySelection);
    expect(wrapper.vm.filters).toEqual([]);

    await advance(300);

    expect(searchMetadata).toHaveBeenLastCalledWith({
      query: '',
      filters: [],
      limit: 20,
      offset: 0,
    });
  });
});

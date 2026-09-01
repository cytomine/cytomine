import { createLocalVue, mount } from '@vue/test-utils';
import Buefy from 'buefy';

import CytomineMultiselect from '@/components/form/CytomineMultiselect.vue';
import MetadataFilter from '@/components/search/MetadataFilter.vue';
import { fetchFacets } from '@/utils/search';

vi.mock('@/utils/search', () => ({
  fetchFacets: vi.fn(),
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

const opt = (value, count) => ({ value, count, label: `${value} (${count})` });

const SITE = 'specimens.anatomical_site.meaning';
const SEX = 'specimens.biological_being.sex';
const TYPE = 'specimens.specimen_type.meaning';

const emptySelection = Object.fromEntries(Object.keys(facets).map((key) => [key, []]));

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

describe('MetadataFilter.vue', () => {
  beforeEach(() => {
    vi.useFakeTimers();
    fetchFacets.mockResolvedValue(facets);
  });

  afterEach(async () => {
    await vi.runAllTimersAsync();
    vi.useRealTimers();
  });

  it('should fetch the facets on creation', async () => {
    const wrapper = await createWrapper();

    expect(fetchFacets).toHaveBeenCalledOnce();
    expect(wrapper.vm.facets).toEqual([
      { key: 'block.block_preparation.meaning', values: [opt('Paraffin wax (substance)', 47)] },
      {
        key: 'slide.staining.stains.compound.meaning',
        values: [opt('hematoxylin stain', 47), opt('water soluble eosin stain', 32)],
      },
      { key: SITE, values: [opt('BONE, STERNUM', 6), opt('KIDNEY', 14),
          opt('LARGE INTESTINE, CECUM', 9), opt('LIVER', 18),] },
      { key: SEX, values: [opt('Male', 47)] },
      { key: TYPE, values: [opt('Tissue specimen (specimen)', 47)] },
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
    expect(fields.at(0).find('.label').text()).toBe('Block preparation');
    expect(fields.at(2).find('.label').text()).toBe('Anatomical site');

    const selects = wrapper.findAllComponents(CytomineMultiselect);
    expect(selects.length).toBe(5);
    expect(selects.at(0).props('options')).toEqual([opt('Paraffin wax (substance)', 47)]);
    expect(selects.at(0).props('multiple')).toBe(true);
    expect(selects.at(2).props('options')).toEqual([
      opt('BONE, STERNUM', 6), opt('KIDNEY', 14),
      opt('LARGE INTESTINE, CECUM', 9), opt('LIVER', 18),
    ]);
  });

  it('should prettify the label of an unmapped facet key', async () => {
    const wrapper = await createWrapper();

    expect(wrapper.vm.facetLabel('specimens.custom_attribute.meaning')).toBe('Custom attribute');
    expect(wrapper.vm.facetLabel('block.block_preparation.meaning')).toBe('Block preparation');
  });

  it('should build no filter when no value is selected', async () => {
    const wrapper = await createWrapper();

    expect(wrapper.vm.filters).toEqual([]);
  });

  it('should build an equality filter when a single value is selected', async () => {
    const wrapper = await createWrapper();

    await wrapper.setData({ selectedFacets: { [SEX]: [opt('Male', 47)] } });

    expect(wrapper.vm.filters).toEqual([`${SEX} = "Male"`]);
  });

  it('should quote a single value containing separators', async () => {
    const wrapper = await createWrapper();

    await wrapper.setData({ selectedFacets: { [SITE]: [opt('LARGE INTESTINE, CECUM', 9)] } });

    expect(wrapper.vm.filters).toEqual([`${SITE} = "LARGE INTESTINE, CECUM"`]);
  });

  it('should build a disjunction when several values are selected', async () => {
    const wrapper = await createWrapper();

    await wrapper.setData({ selectedFacets: { [SITE]: [opt('LIVER', 18), opt('KIDNEY', 14), opt('BONE, STERNUM', 6)] } });

    expect(wrapper.vm.filters).toEqual([
      `(${SITE} = "LIVER" OR ${SITE} = "KIDNEY" OR ${SITE} = "BONE, STERNUM")`,
    ]);
  });

  it('should combine the filters of the selected facets', async () => {
    const wrapper = await createWrapper();

    await wrapper.setData({
      selectedFacets: {
        [SITE]: [opt('LIVER', 18), opt('KIDNEY', 14)],
        [SEX]: [opt('Male', 47)],
        [TYPE]: [opt('Tissue specimen (specimen)', 47)],
      },
    });

    expect(wrapper.vm.filters).toEqual([
      `(${SITE} = "LIVER" OR ${SITE} = "KIDNEY")`,
      `${SEX} = "Male"`,
      `${TYPE} = "Tissue specimen (specimen)"`,
    ]);
  });

  it('should emit the query and filters when the selected facets change', async () => {
    const wrapper = await createWrapper();

    await wrapper.setData({ selectedFacets: { [SITE]: [opt('LIVER', 18), opt('KIDNEY', 14)], [SEX]: [opt('Male', 47)] } });
    await advance(300);

    expect(wrapper.emitted('filter-change').at(-1)).toEqual([{
      query: '',
      filters: [`(${SITE} = "LIVER" OR ${SITE} = "KIDNEY")`, `${SEX} = "Male"`],
    }]);
  });

  it('should emit only once when the selected facets change repeatedly', async () => {
    const wrapper = await createWrapper();

    await wrapper.setData({ selectedFacets: { [SITE]: [opt('LIVER', 18)] } });
    await advance(100);
    await wrapper.setData({ selectedFacets: { [SITE]: [opt('LIVER', 18), opt('KIDNEY', 14)] } });
    await advance(300);

    expect(wrapper.emitted('filter-change')).toEqual([[{
      query: '',
      filters: [`(${SITE} = "LIVER" OR ${SITE} = "KIDNEY")`],
    }]]);
  });

  it('should debounce the search string typed by the user before emitting', async () => {
    const wrapper = await createWrapper();
    await advance(300); // flush the baseline emission triggered once the facets are loaded
    const emittedCount = wrapper.emitted('filter-change').length;

    const input = wrapper.find('input');
    input.element.value = 'liv';
    input.trigger('input');
    await advance(400);

    expect(wrapper.vm.searchString).toBe('');

    input.element.value = 'liver';
    input.trigger('input');
    await advance(500);

    expect(wrapper.vm.searchString).toBe('liver');
    expect(wrapper.emitted('filter-change').length).toBe(emittedCount);

    await advance(300);

    expect(wrapper.emitted('filter-change').length).toBe(emittedCount + 1);
    expect(wrapper.emitted('filter-change').at(-1)).toEqual([{ query: 'liver', filters: [] }]);
  });

  it('should reset the search string and the selected facets on clear', async () => {
    const wrapper = await createWrapper();

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

    expect(wrapper.emitted('filter-change').at(-1)).toEqual([{ query: '', filters: [] }]);
  });
});

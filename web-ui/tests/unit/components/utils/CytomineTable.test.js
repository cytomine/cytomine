import { mount } from '@vue/test-utils';
import Buefy from 'buefy';

import CytomineTable from '@/components/utils/CytomineTable';
import { flushPromises } from '../../../utils';

/**
 * `cytomine-table` is the wrapper behind most of the app's paginated lists, so
 * its slot forwarding is what the Buefy 3 column rewrite hinges on: it now
 * hands the consumer's `<b-table-column>` elements straight to `<b-table>`
 * instead of re-emitting a row-scoped default slot of its own.
 */
describe('CytomineTable.vue', () => {
  const rows = [
    { id: 1, name: 'first' },
    { id: 2, name: 'second' },
  ];

  const collection = (array = rows) => {
    const fake = {
      max: null,
      sort: null,
      order: null,
      clone: () => ({ ...fake, clone: fake.clone, fetchPage: fake.fetchPage }),
      fetchPage: vi.fn().mockResolvedValue({ array, totalNbItems: array.length }),
    };
    return fake;
  };

  const createWrapper = async (options = {}) => {
    const wrapper = mount(CytomineTable, {
      props: { collection: collection(options.rows), detailed: false, ...options.props },
      slots: options.slots,
      global: {
        plugins: [Buefy],
        mocks: { $t: (key) => key },
      },
    });

    await flushPromises();

    return wrapper;
  };

  it('should render each forwarded column once per row', async () => {
    const wrapper = await createWrapper({
      slots: {
        default: `
          <b-table-column v-slot="{row}" label="Name" field="name">
            <span class="name">{{ row.name }}</span>
          </b-table-column>
        `,
      },
    });

    expect(wrapper.findAll('.name').map((c) => c.text())).toEqual(['first', 'second']);
    expect(wrapper.findAll('thead th').map((th) => th.text())).toEqual(['Name']);
  });

  it('should give each column the row index alongside the row', async () => {
    const wrapper = await createWrapper({
      slots: {
        default: `
          <b-table-column v-slot="{row, index}" label="Name">
            <span class="name">{{ index }}:{{ row.name }}</span>
          </b-table-column>
        `,
      },
    });

    expect(wrapper.findAll('.name').map((c) => c.text())).toEqual(['0:first', '1:second']);
  });

  it('should still forward the detail slot with its row', async () => {
    const wrapper = await createWrapper({
      props: { detailed: true, openedDetailed: [1] },
      slots: {
        default: '<b-table-column v-slot="{row}" label="Name">{{ row.name }}</b-table-column>',
        detail: '<span class="detail">detail of {{ params.row.name }}</span>',
      },
    });

    expect(wrapper.find('.detail').text()).toBe('detail of first');
  });

  it('should render the empty slot when the collection has no item', async () => {
    const wrapper = await createWrapper({
      rows: [],
      slots: {
        default: '<b-table-column v-slot="{row}" label="Name">{{ row.name }}</b-table-column>',
        empty: '<span class="none">nothing here</span>',
      },
    });

    expect(wrapper.find('.none').text()).toBe('nothing here');
  });

  it('should fall back to a default empty message', async () => {
    const wrapper = await createWrapper({
      rows: [],
      slots: {
        default: '<b-table-column v-slot="{row}" label="Name">{{ row.name }}</b-table-column>',
      },
    });

    expect(wrapper.find('.is-empty').text()).toBe('no-result');
  });
});

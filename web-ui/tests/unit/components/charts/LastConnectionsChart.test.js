import {shallowMount} from '@vue/test-utils';

import LastConnectionsChart from '@/components/charts/LastConnectionsChart.js';
import {flushPromises} from '../../../utils';

vi.mock('@/api', () => ({
  ProjectConnectionCollection: {
    fetchConnectionsFrequency: vi.fn().mockResolvedValue([]),
  },
}));

describe('LastConnectionsChart.js', () => {
  const createWrapper = (props = {}) => shallowMount(LastConnectionsChart, {
    props: {
      startDate: new Date('2024-01-01T00:00:00Z').getTime(),
      endDate: new Date('2024-01-02T00:00:00Z').getTime(),
      period: 'day',
      project: 1,
      user: 1,
      ...props,
    },
    global: {
      mocks: {
        $t: (key) => key,
        $i18n: {locale: 'en'},
      }
    }
  });

  it('should fill in zero-frequency days when there is no connection data', async () => {
    const wrapper = createWrapper();

    await flushPromises();

    const [dataset] = wrapper.vm.chartData.datasets;
    expect(dataset.data.every(value => value === 0)).toBe(true);
    expect(dataset.data.length).toBeGreaterThan(0);
  });

  it('should set categoryPercentage on the dataset', () => {
    const wrapper = createWrapper();

    const [dataset] = wrapper.vm.chartData.datasets;

    expect(dataset.categoryPercentage).toBe(0.6);
  });

  it('should set min at the scale level, not under ticks', () => {
    const wrapper = createWrapper();

    const {scales} = wrapper.vm.chartOptions;

    expect(scales.y.min).toBe(0);
    expect(scales.y.ticks).toBeUndefined();
  });
});

import { shallowMount } from '@vue/test-utils';

import ActivityOverviewChart from '@/components/charts/ActivityOverviewChart.js';
import { flushPromises } from '../../../utils';

describe('ActivityOverviewChart.js', () => {
  const mockProject = {
    fetchConnectionsEvolution: vi.fn().mockResolvedValue([{ date: 1, size: 3 }]),
    fetchImageConsultationsEvolution: vi.fn().mockResolvedValue([{ date: 1, size: 5 }]),
    fetchAnnotationActionsEvolution: vi.fn().mockResolvedValue([{ date: 1, size: 7 }]),
  };

  const createWrapper = () => shallowMount(ActivityOverviewChart, {
    propsData: {
      project: mockProject,
      startDate: 0,
      endDate: 1,
      daysRange: 1,
    },
    mocks: {
      $t: (key) => key,
      $i18n: { locale: 'en' },
    },
  });

  it('should fetch connections/consultations/selections and fill the three datasets', async () => {
    const wrapper = createWrapper();

    await flushPromises();

    const [connections, consultations, selections] = wrapper.vm.chartData.datasets;
    expect(connections.data).toEqual([3]);
    expect(consultations.data).toEqual([5]);
    expect(selections.data).toEqual([7]);
  });

  it('should set categoryPercentage on each dataset', () => {
    const wrapper = createWrapper();

    const { datasets } = wrapper.vm.chartData;

    datasets.forEach(dataset => {
      expect(dataset.categoryPercentage).toBe(0.6);
    });
  });

  it('should set min at the scale level, not under ticks', () => {
    const wrapper = createWrapper();

    const { scales } = wrapper.vm.chartOptions;

    expect(scales.y.min).toBe(0);
    expect(scales.y.ticks).toBeUndefined();
  });
});

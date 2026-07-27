import {shallowMount} from '@vue/test-utils';

import AnnotationContributorChart from '@/components/charts/AnnotationContributorChart.js';
import {flushPromises} from '../../../utils';

describe('AnnotationContributorChart.js', () => {
  const mockProject = {
    fetchStatsAnnotationCreators: vi.fn().mockResolvedValue([
      {key: 'alice', value: 3},
      {key: 'bob', value: 5},
    ]),
  };

  const createWrapper = () => shallowMount(AnnotationContributorChart, {
    propsData: {
      project: mockProject,
      startDate: 0,
      endDate: 1,
    },
  });

  it('should fetch contributor stats into labels/data and emit nbElems', async () => {
    const wrapper = createWrapper();

    await flushPromises();

    expect(wrapper.vm.chartData.labels).toEqual(['alice', 'bob']);
    expect(wrapper.vm.chartData.datasets[0].data).toEqual([3, 5]);
    expect(wrapper.emitted('nbElems')).toEqual([[2]]);
  });

  it('should set categoryPercentage on the dataset', async () => {
    const wrapper = createWrapper();

    await flushPromises();

    expect(wrapper.vm.chartData.datasets[0].categoryPercentage).toBe(0.6);
  });

  it('should render as a horizontal bar via indexAxis, not the removed HorizontalBar type', () => {
    const wrapper = createWrapper();

    const {indexAxis} = wrapper.vm.chartOptions;

    expect(indexAxis).toBe('y');
  });

  it('should pass the datalabels plugin with the anchor/align/offset/clamp config', () => {
    const wrapper = createWrapper();

    const {datalabels} = wrapper.vm.chartOptions.plugins;

    expect(datalabels).toEqual({
      anchor: 'end',
      align: 'end',
      offset: 5,
      clamp: true,
    });
  });
});

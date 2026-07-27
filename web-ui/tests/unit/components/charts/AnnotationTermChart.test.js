import {shallowMount} from '@vue/test-utils';

import AnnotationTermChart from '@/components/charts/AnnotationTermChart.js';
import {flushPromises} from '../../../utils';

describe('AnnotationTermChart.js', () => {
  const mockProject = {
    fetchStatsTerms: vi.fn().mockResolvedValue([
      {key: 'tumour', value: 3, color: '#ff0000'},
      {key: null, value: 1, color: null},
    ]),
  };

  const createWrapper = () => shallowMount(AnnotationTermChart, {
    propsData: {
      project: mockProject,
      startDate: 0,
      endDate: 1,
    },
    mocks: {
      $t: (key) => key,
    },
  });

  it('should fetch term stats into labels/data, falling back to no-term for a missing key', async () => {
    const wrapper = createWrapper();

    await flushPromises();

    expect(wrapper.vm.chartData.labels).toEqual(['tumour', 'no-term']);
    expect(wrapper.vm.chartData.datasets[0].data).toEqual([3, 1]);
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

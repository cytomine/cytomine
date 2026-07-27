import {shallowMount} from '@vue/test-utils';

import AnnotationProfileChart from '@/components/charts/AnnotationProfileChart.js';
import {flushPromises} from '../../../utils';

describe('AnnotationProfileChart.js', () => {
  const mockAnnotation = {
    centroid: {x: 12.4, y: 7.6},
    fetchProfile: vi.fn().mockResolvedValue({profile: [1, 2, 3]}),
  };

  const createWrapper = (annotation = mockAnnotation) => shallowMount(AnnotationProfileChart, {
    propsData: {
      annotation,
      bpc: 8,
    },
    mocks: {
      $t: (key) => key,
    },
  });

  it('should fetch the annotation profile into the chart data, labelled with the centroid', async () => {
    const wrapper = createWrapper();

    await flushPromises();

    const [dataset] = wrapper.vm.chartData.datasets;
    expect(dataset.data).toEqual([1, 2, 3]);
    expect(dataset.label).toBe('(12, 8)');
  });

  it('should emit error when fetching the profile fails', async () => {
    const failingAnnotation = {
      centroid: {x: 0, y: 0},
      fetchProfile: vi.fn().mockRejectedValue(new Error('boom')),
    };
    const wrapper = createWrapper(failingAnnotation);

    await flushPromises();

    expect(wrapper.emitted('error')).toEqual([[true]]);
  });

  it('should configure the zoom plugin with the v2+ nested wheel/drag schema', () => {
    const wrapper = createWrapper();

    const {zoom} = wrapper.vm.chartOptions.plugins;

    expect(zoom.pan).toEqual({enabled: true, mode: 'xy'});
    expect(zoom.zoom.wheel).toEqual({enabled: true});
    expect(zoom.zoom.drag).toEqual({enabled: false});
    expect(zoom.zoom.mode).toBe('xy');
  });

  it('should cap the y scale using beginAtZero/max at scale level', () => {
    const wrapper = createWrapper();

    const {y} = wrapper.vm.chartOptions.scales;

    expect(y.beginAtZero).toBe(true);
    expect(y.max).toBe(255);
    expect(y.ticks).toBeUndefined();
  });

  it('should delegate resetZoom to the underlying chart instance exposed by vue-chartjs', () => {
    const wrapper = createWrapper();
    const resetZoom = vi.fn();
    wrapper.vm.$refs.chartRef = {chart: {resetZoom}};

    wrapper.vm.resetZoom();

    expect(resetZoom).toHaveBeenCalled();
  });
});

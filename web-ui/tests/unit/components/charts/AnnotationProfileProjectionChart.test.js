import { shallowMount } from '@vue/test-utils';

import AnnotationProfileProjectionChart from '@/components/charts/AnnotationProfileProjectionChart.js';
import { flushPromises } from '../../../utils';

describe('AnnotationProfileProjectionChart.js', () => {
  const createWrapper = (props = {}) => shallowMount(AnnotationProfileProjectionChart, {
    props: {
      annotation: {},
      data: [
        { x: 2, y: 0, average: 20 },
        { x: 1, y: 0, average: 10 },
      ],
      spatialAxis: false,
      dimension: 'channels',
      slices: [],
      ...props,
    },
    global: {
      mocks: {
        $t: (key) => key,
      }
    }
  });

  it('should sort non-spatial data by x/y and build the average dataset', async () => {
    const wrapper = createWrapper();

    await flushPromises();

    expect(wrapper.vm.chartData.labels).toEqual(['(1, 0)', '(2, 0)']);
    expect(wrapper.vm.chartData.datasets[0].data).toEqual([10, 20]);
  });

  it('should use the channel name from slices when spatialAxis/channels dimension is used', async () => {
    const wrapper = createWrapper({
      spatialAxis: true,
      dimension: 'channels',
      data: [{ channel: 1, average: 5 }],
      slices: [{ channel: 1, channelName: 'DAPI' }],
    });

    await flushPromises();

    expect(wrapper.vm.chartData.labels).toEqual(['DAPI']);
  });

  it('should emit error when building the chart data throws', async () => {
    const wrapper = createWrapper({ data: undefined });

    await flushPromises();

    expect(wrapper.emitted('error')).toEqual([[true]]);
  });

  it('should configure the zoom plugin with the v2+ nested wheel/drag schema', () => {
    const wrapper = createWrapper();

    const { zoom } = wrapper.vm.chartOptions.plugins;

    expect(zoom.pan).toEqual({ enabled: true, mode: 'xy' });
    expect(zoom.zoom.wheel).toEqual({ enabled: true });
    expect(zoom.zoom.drag).toEqual({ enabled: false });
    expect(zoom.zoom.mode).toBe('xy');
  });

  it('should delegate resetZoom to the underlying chart instance exposed by vue-chartjs', () => {
    const wrapper = createWrapper();
    const resetZoom = vi.fn();
    // `$refs` is shallow-readonly in Vue 3; the raw refs object is not.
    wrapper.vm.$.refs.chartRef = { chart: { resetZoom } };

    wrapper.vm.resetZoom();

    expect(resetZoom).toHaveBeenCalled();
  });
});

import { shallowMount } from '@vue/test-utils';

import HistogramChart from '@/components/charts/HistogramChart.js';

describe('HistogramChart.js', () => {
  const createWrapper = (props = {}) => shallowMount(HistogramChart, {
    props: {
      logScale: false,
      color: '#fff',
      histogram: [1, 2, 3, 4, 5],
      nBins: 5,
      firstBin: 0,
      lastBin: 4,
      defaultBounds: { min: 0, max: 255 },
      imageBounds: { min: 0, max: 255 },
      currentBounds: { min: 0, max: 255 },
      gamma: 1,
      inverted: false,
      ...props,
    }
  });

  it('should draw the response line above the histogram fill', () => {
    const wrapper = createWrapper();

    const [response, histogram] = wrapper.vm.chartData.datasets;

    expect(response.label).toBe('response');
    expect(histogram.label).toBe('histogram');
    expect(response.order).toBeLessThan(histogram.order);
  });

  it('should only fill the histogram dataset, not the response line', () => {
    const wrapper = createWrapper();

    const [response, histogram] = wrapper.vm.chartData.datasets;

    expect(response.fill).toBe(false);
    expect(histogram.fill).toBe(true);
  });

  it('should use scale ids keyed directly, not xAxes/yAxes arrays', () => {
    const wrapper = createWrapper();

    const { scales } = wrapper.vm.chartOptions;

    expect(scales.x).toBeDefined();
    expect(scales.yHistogram).toBeDefined();
    expect(scales.yResponse).toBeDefined();
    expect(scales.xAxes).toBeUndefined();
    expect(scales.yAxes).toBeUndefined();
  });

  it('should round the exponential value in the tooltip label when logScale is enabled', () => {
    const pixelIntensity = 10;
    const wrapper = createWrapper({ logScale: true });
    const { label } = wrapper.vm.chartOptions.plugins.tooltip.callbacks;

    const result = label({ parsed: { y: Math.log(pixelIntensity) } });

    expect(result).toBe(pixelIntensity);
  });

  it('should return the raw parsed value in the tooltip label when logScale is disabled', () => {
    const pixelIntensity = 42;
    const wrapper = createWrapper({ logScale: false });
    const { label } = wrapper.vm.chartOptions.plugins.tooltip.callbacks;

    const result = label({ parsed: { y: pixelIntensity } });

    expect(result).toBe(pixelIntensity);
  });

  it('should filter out the response dataset from the tooltip', () => {
    const wrapper = createWrapper();
    const { filter } = wrapper.vm.chartOptions.plugins.tooltip;

    const responseFiltered = filter({ datasetIndex: 0 });
    const histogramFiltered = filter({ datasetIndex: 1 });

    expect(responseFiltered).toBe(false);
    expect(histogramFiltered).toBe(true);
  });
});

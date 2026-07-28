import {shallowMount} from '@vue/test-utils';

import NumberAnnotationsChart from '@/components/charts/NumberAnnotationsChart.js';
import {AnnotationType} from '@/api';
import {flushPromises} from '../../../utils';

describe('NumberAnnotationsChart.js', () => {
  const mockProject = {
    fetchAnnotationsEvolution: vi.fn(({annotationType}) => {
      if (annotationType === AnnotationType.USER) {
        return Promise.resolve([{date: 1, size: 4}]);
      }
      return Promise.resolve([{date: 1, size: 2}]);
    }),
  };

  const createWrapper = () => shallowMount(NumberAnnotationsChart, {
    props: {
      project: mockProject,
      term: null,
      startDate: 0,
      endDate: 1,
      daysRange: 1,
    },
    global: {
      mocks: {
        $t: (key) => key,
        $i18n: {locale: 'en'},
      }
    }
  });

  it('should fetch user and reviewed annotation counts into separate datasets', async () => {
    const wrapper = createWrapper();

    await flushPromises();

    const [userDataset, reviewedDataset] = wrapper.vm.chartData.datasets;
    expect(userDataset.data).toEqual([4]);
    expect(reviewedDataset.data).toEqual([2]);
  });

  it('should set categoryPercentage on each dataset', () => {
    const wrapper = createWrapper();

    const {datasets} = wrapper.vm.chartData;

    datasets.forEach(dataset => {
      expect(dataset.categoryPercentage).toBe(0.6);
    });
  });

  it('should set min at the scale level, not under ticks', () => {
    const wrapper = createWrapper();

    const {scales} = wrapper.vm.chartOptions;

    expect(scales.y.min).toBe(0);
    expect(scales.y.ticks).toBeUndefined();
  });
});

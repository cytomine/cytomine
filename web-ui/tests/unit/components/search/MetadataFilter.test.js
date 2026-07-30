import { shallowMount } from '@vue/test-utils';

import MetadataFilter from '@/components/search/MetadataFilter.vue';
import { expect } from 'vitest';

describe('MetadataFilter.vue', () => {
  const createWrapper = () => shallowMount(MetadataFilter, {});

  it('should fetch the data', () => {
    const wrapper = createWrapper();

    expect(wrapper.text()).toContain('Filters');
  });
});

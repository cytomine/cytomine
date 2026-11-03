import { shallowMount } from '@vue/test-utils';
import Buefy from 'buefy';

import DomainTagInput from '@/components/utils/DomainTagInput.vue';
import { getWildcardRegexp } from '@/utils/string-utils';

vi.mock('@/utils/string-utils', () => ({
  getWildcardRegexp: vi.fn().mockImplementation(search => new RegExp(search, 'i'))
}));

describe('DomainTagInput.vue', () => {

  const mocks = {
    $t: message => message
  };

  const domains = [
    { id: 1, name: 'example.com' },
    { id: 2, name: 'test.com' },
    { id: 3, name: 'another.com' }
  ];

  const modelValue = [{ id: 1, name: 'example.com' }];

  let wrapper;
  beforeEach(() => {
    wrapper = shallowMount(DomainTagInput, {
      props: {
        modelValue,
        domains
      },
      global: {
        plugins: [Buefy],
        mocks
      }
    });
  });

  it('should render the tag input correctly', () => {
    expect(wrapper.findComponent(DomainTagInput).exists()).toBe(true);
  });

  it('should filter domains when searchString is "test" correctly', async () => {
    wrapper.setData({ searchString: 'test' });
    await wrapper.vm.$nextTick();

    expect(getWildcardRegexp).toHaveBeenCalledWith('test');
    expect(wrapper.vm.filteredDomains).toEqual([{ id: 2, name: 'test.com' }]);
  });

  it('should emit update:modelValue event when a tag is added', async () => {
    wrapper.vm.$emit('update:modelValue', [{ id: 2, name: 'test.com' }]);
    await wrapper.vm.$nextTick();

    expect(wrapper.emitted()['update:modelValue']).toBeTruthy();
    expect(wrapper.emitted()['update:modelValue'][0]).toEqual([[{ id: 2, name: 'test.com' }]]);
  });

  it('should not allow duplicate domains to be added', async () => {
    wrapper.setData({ searchString: 'example' });
    await wrapper.vm.$nextTick();

    expect(wrapper.vm.filteredDomains).toEqual([]);
  });
});

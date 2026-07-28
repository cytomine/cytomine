import {mount} from '@vue/test-utils';
import Buefy from 'buefy';

import GeometryField from '@/components/appengine/forms/fields/GeometryField';

vi.mock('@/api', () => ({
  Cytomine: {
    instance: {
      api: {
        get: vi.fn(),
      },
    },
  },
}));

describe('GeometryField.vue', () => {
  let wrapper;

  const mockParameter = {
    // eslint-disable-next-line
    display_name: 'Test Parameter',
    description: 'This is a test description',
  };

  beforeEach(() => {

    wrapper = mount(GeometryField, {
      props: {
        parameter: mockParameter,
        value: null,
      },
      global: {
        plugins: [Buefy],
        mocks: {
          $t: (message) => message,
        },
        stubs: {
          AnnotationSelection: true,
        }
      }
    });
  });

  it('The component should be rendered correctly', () => {
    expect(wrapper.find('.field label').text()).toBe(mockParameter.display_name);
    expect(wrapper.find('button').text()).toBe('select');
    expect(wrapper.find('.annotation-container').exists()).toBe(false);

    const tooltips = wrapper.findAllComponents({name: 'BTooltip'});
    expect(tooltips.length).toBe(1);
    expect(tooltips.at(0).exists()).toBe(true);
    expect(tooltips.at(0).props('label')).toBe(mockParameter.description);
  });

  it('The input should not have a default value', () => {
    expect(wrapper.vm.input).toBeNull();
  });

  it('The id should be rendered when selected', async  () => {
    await wrapper.setProps({value: 42});

    expect(wrapper.vm.value).toBe(42);
    expect(wrapper.find('.annotation-container').exists()).toBe(true);
    expect(wrapper.find('.annotation-container').text()).toBe('annotation 42');
  });

  it('should emit an event when the value is changed', async () => {
    wrapper.vm.input = 42;
    await wrapper.vm.$nextTick();

    expect(wrapper.emitted().input).toBeTruthy();
    expect(wrapper.emitted().input.at(0)).toEqual([42]);
  });
});

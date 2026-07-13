import {createLocalVue, mount} from '@vue/test-utils';
import Buefy from 'buefy';

import FileField from '@/components/appengine/forms/fields/FileField';

describe('FileField.vue', () => {
  let localVue;
  let wrapper;

  const mockParameter = {
    // eslint-disable-next-line
    display_name: 'Test Parameter',
    description: 'This is a test description',
  };

  beforeEach(() => {
    localVue = createLocalVue();
    localVue.use(Buefy);

    wrapper = mount(FileField, {
      localVue,
      mocks: {
        $t: (message) => message,
      },
      propsData: {
        parameter: mockParameter,
        modelValue: null,
      },
    });
  });

  it('The component should be rendered correctly', () => {
    expect(wrapper.find('.field label').text()).toEqual(mockParameter.display_name);
    expect(wrapper.find('input[type="file"]').exists()).toBe(true);
    expect(wrapper.find('button').exists()).toBe(false);

    const tooltips = wrapper.findAllComponents({name: 'BTooltip'});
    expect(tooltips.length).toBe(1);
    expect(tooltips.at(0).exists()).toBe(true);
    expect(tooltips.at(0).props('label')).toEqual(mockParameter.description);
  });

  it('The component should render clear button when there is a file', async () => {
    const file = {name: 'filename'};
    await wrapper.setProps({modelValue: file});

    expect(wrapper.find('.field label').text()).toEqual(mockParameter.display_name);
    expect(wrapper.find('input[type="file"]').exists()).toBe(true);
    expect(wrapper.find('button').exists()).toBe(true);
  });

  it('The input should not have a default value', () => {
    expect(wrapper.vm.input).toBeNull();
  });

  it('The id should be rendered when selected', async () => {
    const file = {name: 'filename'};
    await wrapper.setProps({modelValue: file});

    expect(wrapper.vm.modelValue).toStrictEqual(file);
    expect(wrapper.find('.file-name').text()).toBe(file.name);
  });

  it('Changing the value should emit an event', async () => {
    await wrapper.setData({input: {id: 42}});

    expect(wrapper.emitted()['update:modelValue']).toBeTruthy();
    expect(wrapper.emitted()['update:modelValue'].at(0)).toEqual([{id: 42}]);
  });
});

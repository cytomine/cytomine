import {createLocalVue, mount} from '@vue/test-utils';
import Buefy from 'buefy';

import ArrayModal from '@/components/appengine/forms/fields/array/ArrayModal';
import BooleanField from '@/components/appengine/forms/fields/BooleanField';
import CytomineModal from '@/components/utils/CytomineModal';
import GeometryArrayField from '@/components/appengine/forms/fields/array/GeometryArrayField';
import ImageArrayField from '@/components/appengine/forms/fields/array/ImageArrayField';

jest.mock('@/api', () => ({
  Cytomine: {
    instance: {
      api: {
        get: jest.fn(),
      },
    },
  },
}));

jest.mock('@/utils/image-utils', () => ({
  isWebPSupported: jest.fn(() => true)
}));

describe('ArrayModal.vue', () => {
  let localVue;
  let wrapper;

  beforeEach(() => {
    localVue = createLocalVue();
    localVue.use(Buefy);

    wrapper = mount(ArrayModal, {
      localVue,
      mocks: {
        $t: msg => msg,
        $notify: jest.fn()
      },
      stubs: {
        AnnotationMultiSelect: true,
        ImageMultiSelect: true,
      },
      propsData: {
        type: {id: 'boolean'},
        active: true,
      }
    });
  });

  it('should be rendered correctly', () => {
    expect(wrapper.exists()).toBe(true);
    expect(wrapper.findComponent(CytomineModal).exists()).toBe(true);
  });

  it('should render a boolean field when clicking on add', async () => {
    expect(wrapper.findComponent(BooleanField).exists()).toBe(false);

    await wrapper.vm.add();

    expect(wrapper.findComponent(BooleanField).exists()).toBe(true);
    expect(wrapper.vm.items.length).toBe(1);
  });

  it.each([
    ['geometry', GeometryArrayField],
    ['image', ImageArrayField],
  ])('should render the correct complex field when type is %s', async (typeId, expectedComponent) => {
    await wrapper.setProps({type: {id: typeId}});

    expect(wrapper.findComponent(expectedComponent).exists()).toBe(true);
  });

  it('should correctly update items when adding or removing', async () => {
    wrapper.vm.add();
    wrapper.vm.add();
    expect(wrapper.vm.items.length).toBe(2);

    wrapper.vm.remove(0);
    expect(wrapper.vm.items.length).toBe(1);
  });

  it('should emit update:active false when cancelling', () => {
    wrapper.vm.cancel();

    expect(wrapper.emitted('update:active')).toBeTruthy();
    expect(wrapper.emitted('update:active')[0]).toEqual([false]);
  });

  it('should show an error when selecting with an empty list', () => {
    wrapper.vm.select();

    expect(wrapper.vm.$notify).toHaveBeenCalledWith({
      type: 'error',
      text: 'notify-error-empty-list'
    });
  });

  it('should show an error when selecting with not enough items', async () => {
    await wrapper.setProps({minSize: 2});
    await wrapper.setData({items: [true]});

    wrapper.vm.select();

    expect(wrapper.vm.$notify).toHaveBeenCalledWith({
      type: 'error',
      text: 'notify-error-not-enough-item'
    });
  });

  it('should emit events and reset when selecting with valid items', async () => {
    await wrapper.setData({items: [true]});

    wrapper.vm.select();

    expect(wrapper.emitted('create-inputs')).toBeTruthy();
    expect(wrapper.emitted('update:active')).toBeTruthy();
    expect(wrapper.vm.items).toEqual([]);
    expect(wrapper.vm.$notify).toHaveBeenCalledWith({
      type: 'success',
      text: 'notify-success-create-array-inputs'
    });
  });
});

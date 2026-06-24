import {shallowMount} from '@vue/test-utils';

import AnnotationMultiSelect from '@/components/appengine/forms/fields/array/AnnotationMultiSelect';
import GeometryArrayField from '@/components/appengine/forms/fields/array/GeometryArrayField';

describe('GeometryArrayField.vue', () => {
  const createWrapper = () => shallowMount(GeometryArrayField, {});

  it('should render an AnnotationMultiSelect', () => {
    const wrapper = createWrapper();
    expect(wrapper.findComponent(AnnotationMultiSelect).exists()).toBe(true);
  });

  it('should wrap the emitted ids in {ids, type: annotation} and re-emit as input', async () => {
    const wrapper = createWrapper();
    const ids = [1, 2, 3];

    wrapper.findComponent(AnnotationMultiSelect).vm.$emit('input', ids);
    await wrapper.vm.$nextTick();

    expect(wrapper.emitted('input')).toBeTruthy();
    expect(wrapper.emitted('input')[0]).toEqual([{ids, type: 'annotation'}]);
  });
});

import { mount } from '@vue/test-utils';
import Buefy from 'buefy';

import CytomineSlider from '@/components/form/CytomineSlider.vue';

vi.mock('vue-slider-component', () => ({
  __esModule: true,
  default: {
    name: 'vue-slider',
    props: ['value'],
    render() {
      const values = Array.isArray(this.value) ? [...this.value] : [this.value];
      return values.map((value, index) => this.$scopedSlots.tooltip({ value, index }));
    },
  },
}));

describe('CytomineSlider.vue', () => {
  const createWrapper = (props = {}) => mount(CytomineSlider, {
    props: { modelValue: 20, min: 0, max: 100, ...props },
    global: { plugins: [Buefy] },
  });

  const edit = async (wrapper, dot = 0) => {
    await wrapper.findAll('.vue-slider-dot-tooltip-inner')[dot].trigger('click');
    return wrapper.find('input');
  };

  const committed = (wrapper) => (wrapper.emitted('update:modelValue') ?? [])
    .filter(([payload]) => !(payload instanceof Event));

  it('commits the edited value when Enter is pressed', async () => {
    const wrapper = createWrapper();

    const input = await edit(wrapper);
    await input.setValue('42');
    await input.trigger('keyup.enter');

    expect(committed(wrapper)).toEqual([[42]]);
    expect(wrapper.vm.indexEdited).toBeNull();
  });

  it('does not commit on a key other than Enter', async () => {
    const wrapper = createWrapper();

    const input = await edit(wrapper);
    await input.setValue('42');
    await input.trigger('keyup.esc');

    expect(committed(wrapper)).toEqual([]);
    expect(wrapper.vm.indexEdited).toBe(0);
  });

  it('commits the right bound of a range slider', async () => {
    const wrapper = createWrapper({ modelValue: [10, 80] });

    const input = await edit(wrapper, 1);
    await input.setValue('90');
    await input.trigger('keyup.enter');

    expect(committed(wrapper)).toEqual([[[10, 90]]]);
  });

  it('focuses the field when edit mode opens', async () => {
    const focus = vi.spyOn(CytomineSlider.methods, 'focus').mockImplementation(() => {});
    const wrapper = createWrapper();

    expect(focus).not.toHaveBeenCalled();
    await edit(wrapper);

    expect(focus).toHaveBeenCalled();
    focus.mockRestore();
  });
});

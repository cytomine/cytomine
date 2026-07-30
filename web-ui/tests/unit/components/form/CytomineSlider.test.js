import { mount } from '@vue/test-utils';
import Buefy from 'buefy';

import CytomineSlider from '@/components/form/CytomineSlider.vue';

// `vue-slider-component` only ships a Vue 2 UMD build, which throws
// "Super expression must either be null or a function" the moment it is
// imported under the compat runtime. Stand in for it with a component that
// renders the `tooltip` scoped slot the same way, since it is that slot's
// markup — not the slider — that this file is about.
vi.mock('vue-slider-component', () => ({
  __esModule: true,
  default: {
    name: 'vue-slider',
    props: ['value'],
    render() {
      const values = Array.isArray(this.value) ? [...this.value] : [this.value];
      // compat keeps `$slots` Vue-2 shaped, so scoped slots come off
      // `$scopedSlots`.
      return values.map((value, index) => this.$scopedSlots.tooltip({ value, index }));
    },
  },
}));

// Issue 10 moved `@keyup.enter.native` off the `b-input` and onto the plain
// wrapper element. That is invisible to lint, to the build and to every other
// test, so it is asserted directly here.
describe('CytomineSlider.vue', () => {
  const createWrapper = (props = {}) => mount(CytomineSlider, {
    props: { value: 20, min: 0, max: 100, ...props },
    global: { plugins: [Buefy] },
  });

  const edit = async (wrapper, dot = 0) => {
    await wrapper.findAll('.vue-slider-dot-tooltip-inner')[dot].trigger('click');
    return wrapper.find('input');
  };

  // `emitted()` also collects the native `input` events that bubble out of the
  // text field, so keep only what the component itself emitted.
  const committed = (wrapper) => (wrapper.emitted('input') ?? [])
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
    const wrapper = createWrapper({ value: [10, 80] });

    const input = await edit(wrapper, 1);
    await input.setValue('90');
    await input.trigger('keyup.enter');

    expect(committed(wrapper)).toEqual([[[10, 90]]]);
  });
});

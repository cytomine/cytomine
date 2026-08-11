import { mount } from '@vue/test-utils';
import Buefy from 'buefy';

import CytomineSlider from '@/components/form/CytomineSlider.vue';

describe('CytomineSlider.vue', () => {
  const createWrapper = (props = {}) => mount(CytomineSlider, {
    props: { modelValue: 20, min: 0, max: 100, ...props },
    global: { plugins: [Buefy] },
  });

  const edit = async (wrapper, dot = 0) => {
    await wrapper.findAll('.cytomine-slider-tooltip')[dot].trigger('click');
    return wrapper.find('input');
  };

  // `emitted()` also collects the native `input` events that bubble out of the
  // text field, but those land on the `input` channel; the component commits on
  // `update:modelValue`, so read that. Filter out any stray Event payloads.
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

  // Issue 17: `@hook:mounted` → `@vue:mounted` on the edit `b-input`. Both fire
  // under compat, so the regression this guards against is the hook silently
  // going dead once compat is dropped — assert `focus()` still runs when the
  // field mounts (i.e. on entering edit mode).
  it('focuses the field when edit mode opens', async () => {
    const focus = vi.spyOn(CytomineSlider.methods, 'focus').mockImplementation(() => {});
    const wrapper = createWrapper();

    expect(focus).not.toHaveBeenCalled();
    await edit(wrapper);

    expect(focus).toHaveBeenCalled();
    focus.mockRestore();
  });
});

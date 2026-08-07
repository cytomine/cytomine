import { mount } from '@vue/test-utils';
import Buefy from 'buefy';

import ImageControlsShiftButtons from '@/components/viewer/ImageControlsShiftButtons.vue';

// Issue 10 moved `@keyup.enter.native` off the `b-input` and onto the plain
// `.step-selector` wrapper. Nothing else in the suite types into the step
// selector, and the change is invisible to lint and to the build.
describe('ImageControlsShiftButtons.vue', () => {
  const commit = vi.fn();

  // `v-popover` comes from the globally installed v-tooltip plugin and keeps
  // its `popover` slot out of the document; render both slots eagerly instead.
  const VPopover = {
    name: 'v-popover',
    render() {
      return [this.$scopedSlots.default(), this.$scopedSlots.popover()];
    },
  };

  const createWrapper = () => mount(ImageControlsShiftButtons, {
    props: { index: '0', forward: true, current: 0, size: 10, dimension: 'slice' },
    global: {
      plugins: [Buefy],
      stubs: { 'v-popover': VPopover },
      directives: { 'click-outside': {} },
      mocks: {
        $t: key => key,
        $store: {
          commit,
          getters: {
            'currentProject/imageModule': () => 'mock-module/',
            'currentProject/currentViewer': {
              images: { 0: { controls: { step: { slice: 5 } } } },
            },
          },
        },
      },
    },
  });

  it('commits the edited step when Enter is pressed', async () => {
    const wrapper = createWrapper();

    const input = wrapper.find('.step-selector input');
    await input.setValue('7');
    await input.trigger('keyup.enter');

    expect(commit).toHaveBeenCalledWith('mock-module/setStep', { dimension: 'slice', value: 7 });
    expect(wrapper.vm.opened).toBe(false);
  });

  it('does not commit on a key other than Enter', async () => {
    const wrapper = createWrapper();

    const input = wrapper.find('.step-selector input');
    await input.setValue('7');
    await input.trigger('keyup.esc');

    expect(commit).not.toHaveBeenCalled();
  });

  // Issue 17: `@hook:mounted` → `@vue:mounted` on the step-selector `b-input`.
  // Both fire under compat, so this guards against the hook silently going dead
  // once compat is dropped — the field mounts eagerly here, so `focus()` runs
  // on mount.
  it('focuses the step field on mount', () => {
    const focus = vi.spyOn(ImageControlsShiftButtons.methods, 'focus').mockImplementation(() => {});

    createWrapper();

    expect(focus).toHaveBeenCalled();
    focus.mockRestore();
  });
});

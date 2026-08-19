import { mount } from '@vue/test-utils';
import Buefy from 'buefy';

import ImageControlsShiftButtons from '@/components/viewer/ImageControlsShiftButtons.vue';

describe('ImageControlsShiftButtons.vue', () => {
  const commit = vi.fn();

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

  it('focuses the step field on mount', () => {
    const focus = vi.spyOn(ImageControlsShiftButtons.methods, 'focus').mockImplementation(() => {});

    createWrapper();

    expect(focus).toHaveBeenCalled();
    focus.mockRestore();
  });
});

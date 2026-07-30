import { mount } from '@vue/test-utils';
import Buefy from 'buefy';

import LinkPanel from '@/components/viewer/panels/LinkPanel.vue';

// Issue 10 moved `@change.native` off the `b-checkbox` and onto the enclosing
// `<td>`. It has to stay a native DOM event: `handleCheckboxChange` resets the
// input through `event.target.checked`, and `b-checkbox` emits no `change`.
describe('LinkPanel.vue', () => {
  const commit = vi.fn();
  const confirm = vi.fn();

  // Two unlinked views besides the current one, so the "solo images" rows —
  // the ones carrying the checkbox — render.
  const createWrapper = ({ trackedUser = null } = {}) => mount(LinkPanel, {
    props: { index: '0' },
    global: {
      plugins: [Buefy],
      mocks: {
        $t: key => key,
        $buefy: { dialog: { confirm } },
        $store: {
          commit,
          getters: {
            'currentProject/currentViewerModule': 'viewer/',
            'currentProject/imageModule': () => 'image/',
            'currentProject/currentViewer': {
              linkMode: 'ABSOLUTE',
              links: [],
              images: {
                0: { imageInstance: { id: 10 }, tracking: { trackedUser } },
                1: { imageInstance: { id: 11 }, tracking: { trackedUser: null } },
              },
            },
          },
        },
      },
    },
  });

  it('links the view when the checkbox is ticked', async () => {
    const wrapper = createWrapper();

    await wrapper.find('td input[type="checkbox"]').setValue(true);

    expect(commit).toHaveBeenCalledWith('viewer/createLinkGroup', ['0', '1']);
  });

  it('passes the native change event, so the checkbox can be reset on cancel', async () => {
    const wrapper = createWrapper({ trackedUser: 5 });

    const checkbox = wrapper.find('td input[type="checkbox"]');
    await checkbox.setValue(true);

    expect(commit).not.toHaveBeenCalled();
    expect(confirm).toHaveBeenCalledTimes(1);

    confirm.mock.calls[0][0].onCancel();
    expect(checkbox.element.checked).toBe(false);
  });
});

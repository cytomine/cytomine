import { shallowMount } from '@vue/test-utils';
import Buefy from 'buefy';

import CytomineModal from '@/components/utils/CytomineModal';
import RenameModal from '@/components/utils/RenameModal';
import { veeValidateDirectives, veeValidateMocks } from '../../../vee-validate';

describe('RenameModal.vue', () => {

  const mocks = {
    $t: message => message,
    ...veeValidateMocks(),
  };

  it('should render the modal with the correct title', () => {
    const wrapper = shallowMount(RenameModal, {
      props: {
        active: true,
        title: 'Rename',
        currentName: 'Old Name'
      },
      global: {
        plugins: [Buefy],
        directives: veeValidateDirectives,
        mocks
      }
    });

    expect(wrapper.props().active).toBe(true);
    expect(wrapper.props().title).toBe('Rename');
    expect(wrapper.props().currentName).toBe('Old Name');

    expect(wrapper.findComponent(CytomineModal).props('title')).toBe('Rename');
  });

  it('should emit "close" and "update:active" events when close method is called', async () => {
    const wrapper = shallowMount(CytomineModal, {
      props: {
        active: true
      },
      global: {
        plugins: [Buefy]
      }
    });

    wrapper.vm.close();

    expect(wrapper.emitted().close).toBeTruthy();
    expect(wrapper.emitted()['update:active'][0]).toEqual([false]);
  });
});

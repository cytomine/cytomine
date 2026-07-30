import { mount, shallowMount } from '@vue/test-utils';
import Buefy from 'buefy';

import CytomineModal from '@/components/utils/CytomineModal';
import RenameModal from '@/components/utils/RenameModal';
import { flushPromises } from '../../../utils';

describe('RenameModal.vue', () => {

  const mocks = {
    $t: message => message,
  };

  // The form fields render through `@tanstack/vue-form`'s renderless `Field`,
  // which only passes its slot props when it is really rendered, so these mount
  // fully rather than shallow. `b-modal` still swallows its content under Vue 3
  // (issue 3, `active` → `modelValue`), so the modal itself is stubbed by one
  // that renders both slots inline.
  const cytomineModalStub = {
    name: 'cytomine-modal',
    props: ['active', 'title'],
    template: '<div><slot /><slot name="footer" /></div>'
  };

  function mountModal(props = {}) {
    return mount(RenameModal, {
      props: { active: true, title: 'Rename', currentName: 'Old Name', ...props },
      global: {
        plugins: [Buefy],
        stubs: { 'cytomine-modal': cytomineModalStub },
        mocks
      }
    });
  }

  it('should render the modal with the correct title', () => {
    const wrapper = mountModal();

    expect(wrapper.props().active).toBe(true);
    expect(wrapper.props().title).toBe('Rename');
    expect(wrapper.props().currentName).toBe('Old Name');

    expect(wrapper.findComponent(CytomineModal).props('title')).toBe('Rename');
  });

  it('should seed the field with the current name', () => {
    const wrapper = mountModal();

    expect(wrapper.find('input').element.value).toBe('Old Name');
    expect(wrapper.vm.form.state.values.name).toBe('Old Name');
  });

  it('should re-seed the field every time the modal is reopened', async () => {
    const wrapper = mountModal({ active: false });

    await wrapper.find('input').setValue('Typed then abandoned');
    await wrapper.setProps({ active: true, currentName: 'Another Name' });

    expect(wrapper.vm.form.state.values.name).toBe('Another Name');
  });

  it('should emit "rename" with the new name when the name is valid', async () => {
    const wrapper = mountModal();

    await wrapper.find('input').setValue('New Name');
    await wrapper.find('form').trigger('submit');
    await flushPromises();

    expect(wrapper.emitted().rename[0]).toEqual(['New Name']);
    expect(wrapper.emitted()['update:active'][0]).toEqual([false]);
  });

  it('should not emit "rename" when the name is empty, and should show the error', async () => {
    const wrapper = mountModal();

    await wrapper.find('input').setValue('');
    await wrapper.find('form').trigger('submit');
    await flushPromises();

    expect(wrapper.emitted().rename).toBeUndefined();
    expect(wrapper.text()).toContain('This field is required');
  });

  it('should disable the save button while the name is invalid', async () => {
    const wrapper = mountModal();
    const saveButton = wrapper.find('button.is-link');

    expect(saveButton.attributes('disabled')).toBeUndefined();

    await wrapper.find('input').setValue('');
    expect(saveButton.attributes('disabled')).toBeDefined();

    await wrapper.find('input').setValue('New Name');
    expect(saveButton.attributes('disabled')).toBeUndefined();
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

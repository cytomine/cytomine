import { mount } from '@vue/test-utils';
import Buefy from 'buefy';

import CalibrationModal from '@/components/image/CalibrationModal';
import { flushPromises } from '../../../utils';

describe('CalibrationModal.vue', () => {

  const cytomineModalStub = {
    name: 'cytomine-modal',
    props: ['active', 'title'],
    template: '<div><slot /><slot name="footer" /></div>'
  };

  const save = vi.fn().mockResolvedValue(undefined);

  function image({ depth = 1, duration = 1 } = {}) {
    const source = {
      depth,
      duration,
      instanceFilename: 'slide.svs',
      physicalSizeX: 0.5,
      physicalSizeZ: 2,
      fps: 25,
      save
    };
    source.clone = () => ({ ...source });
    return source;
  }

  async function openModal(dimensions) {
    const wrapper = mount(CalibrationModal, {
      props: { active: false, image: image(dimensions) },
      global: {
        plugins: [Buefy],
        stubs: { 'cytomine-modal': cytomineModalStub },
        mocks: {
          $t: message => message,
          $notify: vi.fn(),
          $store: { state: { currentProject: { project: { blindMode: false } } } }
        }
      }
    });
    await wrapper.setProps({ active: true });
    return wrapper;
  }

  it('should only show the z and t fields for images that have those dimensions', async () => {
    expect((await openModal()).findAll('.field input').length).toBe(1);
    expect((await openModal({ depth: 4 })).findAll('.field input').length).toBe(2);
    expect((await openModal({ depth: 4, duration: 10 })).findAll('.field input').length).toBe(3);
  });

  it('should not hold back a 2D image on the z and t rules it never shows', async () => {
    const wrapper = await openModal();

    await wrapper.find('form').trigger('submit');
    await flushPromises();

    expect(save).toHaveBeenCalled();
    expect(wrapper.emitted().setResolution).toBeTruthy();
  });

  it('should validate the z field once the image has depth', async () => {
    const wrapper = await openModal({ depth: 4 });

    await wrapper.findAll('.field input')[1].setValue('');
    await wrapper.find('form').trigger('submit');
    await flushPromises();

    expect(wrapper.emitted().setResolution).toBeUndefined();
    expect(wrapper.text()).toContain('This field is required');
  });

  it.each([
    ['abc', 'Must be a number'],
    ['-1', 'Must be positive'],
    ['', 'This field is required']
  ])('should reject a resolution of %p', async (value, message) => {
    const wrapper = await openModal();

    await wrapper.find('.field input').setValue(value);
    await wrapper.find('form').trigger('submit');
    await flushPromises();

    expect(wrapper.emitted().setResolution).toBeUndefined();
    expect(wrapper.text()).toContain(message);
  });

  it('should scale the resolution by the selected unit', async () => {
    const wrapper = await openModal();

    await wrapper.find('.field input').setValue('4');
    wrapper.vm.calibrationFactorX = 1000;
    await wrapper.find('form').trigger('submit');
    await flushPromises();

    expect(wrapper.emitted().setResolution[0][0]).toMatchObject({ x: 4000, y: 4000 });
  });

  it('should re-seed every field each time the modal is reopened', async () => {
    const wrapper = await openModal({ depth: 4, duration: 10 });

    await wrapper.find('.field input').setValue('999');
    await wrapper.setProps({ active: false });
    await wrapper.setProps({ active: true });

    expect(wrapper.vm.form.state.values).toEqual({
      'resolution': 0.5,
      'resolution-z': 2,
      'resolution-t': 25
    });
  });
});

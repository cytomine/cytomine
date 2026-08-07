import { mount } from '@vue/test-utils';
import Buefy from 'buefy';

import UserModal from '@/components/admin/UserModal';
import { flushPromises } from '../../../utils';

const save = vi.fn().mockResolvedValue(undefined);
const defineRole = vi.fn().mockResolvedValue(undefined);

vi.mock('@/api', () => ({
  User: vi.fn().mockImplementation(function () {
    return { save, defineRole };
  }),
}));

describe('UserModal.vue', () => {

  const cytomineModalStub = {
    name: 'cytomine-modal',
    props: ['active', 'title'],
    template: '<div><slot /><slot name="footer" /></div>'
  };

  function existingUser() {
    const user = {
      id: 7,
      username: 'jdoe',
      firstname: 'Jane',
      lastname: 'Doe',
      email: 'jane@example.com',
      role: 'ROLE_USER',
      language: 'EN',
      save,
      defineRole
    };
    user.clone = () => ({ ...user });
    return user;
  }

  async function openModal(props = {}) {
    const wrapper = mount(UserModal, {
      props: { active: false, ...props },
      global: {
        plugins: [Buefy],
        stubs: { 'cytomine-modal': cytomineModalStub },
        mocks: { $t: message => message, $notify: vi.fn() }
      }
    });
    await wrapper.setProps({ active: true });
    return wrapper;
  }

  function fields(wrapper) {
    const inputs = wrapper.findAll('.field input');
    return {
      username: inputs[0],
      firstname: inputs[1],
      lastname: inputs[2],
      email: inputs[3],
      password: inputs[4]
    };
  }

  async function fillValidProfile(wrapper) {
    const input = fields(wrapper);
    await input.username.setValue('jdoe');
    await input.firstname.setValue('Jane');
    await input.lastname.setValue('Doe');
    await input.email.setValue('jane@example.com');
  }

  beforeEach(() => {
    vi.spyOn(crypto, 'randomUUID').mockReturnValue('a-reference');
  });

  it('should seed the fields from the user being edited', async () => {
    const wrapper = await openModal({ user: existingUser() });

    expect(wrapper.vm.form.state.values).toMatchObject({
      username: 'jdoe',
      firstname: 'Jane',
      lastname: 'Doe',
      email: 'jane@example.com',
      password: ''
    });
  });

  it('should require a password when creating a user', async () => {
    const wrapper = await openModal();

    await fillValidProfile(wrapper);
    await wrapper.find('form').trigger('submit');
    await flushPromises();

    expect(save).not.toHaveBeenCalled();
    expect(wrapper.text()).toContain('This field is required');
  });

  it('should accept an empty password when editing, and leave it untouched', async () => {
    const wrapper = await openModal({ user: existingUser() });

    await wrapper.find('form').trigger('submit');
    await flushPromises();

    expect(save).toHaveBeenCalled();
    expect(wrapper.vm.internalUser.password).toBeUndefined();
  });

  it('should reject a password shorter than 8 characters', async () => {
    const wrapper = await openModal({ user: existingUser() });

    await fields(wrapper).password.setValue('short');
    await wrapper.find('form').trigger('submit');
    await flushPromises();

    expect(save).not.toHaveBeenCalled();
    expect(wrapper.text()).toContain('Must have at least 8 characters');
  });

  it('should carry a typed password over to the user being saved', async () => {
    const wrapper = await openModal({ user: existingUser() });

    await fields(wrapper).password.setValue('long-enough');
    await wrapper.find('form').trigger('submit');
    await flushPromises();

    expect(save).toHaveBeenCalled();
    expect(wrapper.vm.internalUser.password).toBe('long-enough');
  });

  it('should reject a malformed email address', async () => {
    const wrapper = await openModal();

    await fillValidProfile(wrapper);
    await fields(wrapper).email.setValue('not-an-email');
    await fields(wrapper).password.setValue('long-enough');
    await wrapper.find('form').trigger('submit');
    await flushPromises();

    expect(save).not.toHaveBeenCalled();
    expect(wrapper.text()).toContain('Must be a valid email address');
  });

  it('should build the user name from the first and last name on save', async () => {
    const wrapper = await openModal();

    await fillValidProfile(wrapper);
    await fields(wrapper).password.setValue('long-enough');
    await wrapper.find('form').trigger('submit');
    await flushPromises();

    expect(save).toHaveBeenCalled();
    expect(wrapper.vm.internalUser.name).toBe('Jane Doe');
    expect(wrapper.emitted().addUser).toBeTruthy();
  });
});

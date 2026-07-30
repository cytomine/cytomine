import { shallowMount } from '@vue/test-utils';
import Buefy from 'buefy';

import AttachedFileModal from '@/components/attached-file/AttachedFileModal';
import CytomineModalCard from '@/components/utils/CytomineModalCard.vue';
import { veeValidateDirectives, veeValidateMocks } from '../../../vee-validate';

vi.mock('@/api', () => ({
  AttachedFile: vi.fn().mockImplementation(function () {
    return {
      save: vi.fn().mockResolvedValue({ id: 1, filename: 'mockFile.pdf' }),
    };
  }),
}));

describe('AttachedFileModal.vue', () => {
  let wrapper;

  beforeEach(() => {

    wrapper = shallowMount(AttachedFileModal, {
      props: { object: { id: 1 } },
      global: {
        plugins: [Buefy],
        directives: veeValidateDirectives,
        mocks: {
          $t: (message) => message,
          ...veeValidateMocks(),
        },
        stubs: {
          CytomineModalCard: true,
        }
      }
    });
  });

  afterEach(() => {
    wrapper.unmount();
  });

  it('should render the component correctly', () => {
    expect(wrapper.exists()).toBe(true);
    expect(wrapper.findComponent(CytomineModalCard).exists()).toBe(true);
    expect(wrapper.findAll('p').length).toBe(2);
  });

  it('should update the name when a file is selected', async () => {
    expect(wrapper.vm.name).toBe('');
    expect(wrapper.find('.filename').exists()).toBe(false);

    const mockFile = new File(['content'], 'mockFile.pdf', { type: 'application/pdf' });
    wrapper.setData({ selectedFile: mockFile });

    await wrapper.vm.$nextTick();

    expect(wrapper.vm.name).toBe('mockFile.pdf');
    expect(wrapper.find('.filename').exists()).toBe(true);
    expect(wrapper.find('.filename').text()).toContain('mockFile.pdf');
  });
});

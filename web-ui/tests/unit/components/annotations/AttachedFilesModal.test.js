import { shallowMount } from '@vue/test-utils';
import Buefy from 'buefy';

import AttachedFileModal from '@/components/attached-file/AttachedFileModal';
import CytomineModalCard from '@/components/utils/CytomineModalCard.vue';

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
        mocks: {
          $t: (message) => message,
        },
        stubs: {
          CytomineModalCard: true,
          Field: false,
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
    expect(wrapper.vm.form.state.values.name).toBe('');
    expect(wrapper.find('.filename').exists()).toBe(false);

    const mockFile = new File(['content'], 'mockFile.pdf', { type: 'application/pdf' });
    wrapper.setData({ selectedFile: mockFile });

    await wrapper.vm.$nextTick();

    expect(wrapper.vm.form.state.values.name).toBe('mockFile.pdf');
    expect(wrapper.find('.filename').exists()).toBe(true);
    expect(wrapper.find('.filename').text()).toContain('mockFile.pdf');
  });
});

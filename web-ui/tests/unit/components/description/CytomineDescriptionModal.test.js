import {shallowMount} from '@vue/test-utils';
import VueI18n from 'vue-i18n';

import CytomineDescriptionModal from '@/components/description/CytomineDescriptionModal';
import CytomineModalCard from '@/components/utils/CytomineModalCard';

vi.mock('@/components/utils/CytomineModalCard', () => ({
  default: {
    name: 'cytomine-modal-card',
    template: '<div><slot></slot></div>'
  }
}));

vi.mock('@/components/form/CytomineQuillEditor', () => ({
  default: {
    name: 'cytomine-quill-editor',
    template: '<div><slot></slot></div>',
    props: ['value', 'placeholder']
  }
}));

describe('CytomineDescriptionModal', () => {

  let wrapper;

  beforeEach(() => {
    wrapper = shallowMount(CytomineDescriptionModal, {
      props: {
        description: {data: 'Test description'},
        edit: false
      },
      global: {
        plugins: [VueI18n],
        mocks: {
          $t: (message) => message,
          $notify: vi.fn()
        }
      }
    });
  });

  it('should render the component correctly', () => {
    expect(wrapper.findComponent(CytomineModalCard).exists()).toBe(true);
    expect(wrapper.find('.ql-editor.preview').exists()).toBe(true);
    expect(wrapper.text()).toContain('description');
  });
});

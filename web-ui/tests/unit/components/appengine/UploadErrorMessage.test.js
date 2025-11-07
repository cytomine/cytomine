import {shallowMount} from '@vue/test-utils';

import UploadErrorMessage from '@/components/appengine/UploadErrorMessage.vue';

describe('UploadErrorMessage.vue', () => {
  const mockError = {
    message: 'Schema validation failed for the descriptor file',
    details: {
      errors: [
        {message: 'namespace is missing but it is required'},
        {message: 'version is missing but it is required'},
      ],
    },
  };

  const createWrapper = (options = {}) => shallowMount(
    UploadErrorMessage,
    {
      propsData: {
        error: mockError,
      },
      stubs: {
        'b-message': true,
      },
      ...options,
    },
  );

  it('should render the list of errors', () => {
    const wrapper = createWrapper();

    mockError.details.errors.forEach((error) => {
      expect(wrapper.text()).toContain(error.message);
    });
  });
});

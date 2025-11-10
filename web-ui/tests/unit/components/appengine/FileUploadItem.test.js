import {shallowMount} from '@vue/test-utils';

import FileUploadItem from '@/components/appengine/FileUploadItem.vue';
import {UploadStatus} from '@/utils/app';

describe('FileUploadItem.vue', () => {
  const mockFile = new File(['PK\x03\x04'], 'test.zip', {
    type: 'application/zip',
    lastModified: new Date(),
  });

  const createWrapper = (options = {}) => shallowMount(
    FileUploadItem,
    {
      propsData: {
        file: mockFile,
      },
      mocks: {
        $t: (key) => key,
      },
      stubs: {
        'b-button': true,
        'b-icon': true,
        'b-progress': true,
      },
      ...options,
    },
  );

  it('should render file information', () => {
    const wrapper = createWrapper();

    expect(wrapper.text()).toContain(mockFile.name);
  });

  describe('Upload Status', () => {
    it('should render upload button when file is pending', () => {
      const wrapper = createWrapper();

      expect(wrapper.text()).toContain('upload');
      expect(wrapper.text()).not.toContain('button-cancel');
      expect(wrapper.text()).not.toContain('upload-cancelled');
      expect(wrapper.text()).not.toContain('upload-completed');
    });

    it('should render cancel button when file is uploading', async () => {
      const wrapper = createWrapper();
      await wrapper.setData({
        uploadFile: {
          ...wrapper.vm.uploadFile,
          status: UploadStatus.UPLOADING,
        },
      });

      expect(wrapper.text()).not.toContain('upload');
      expect(wrapper.text()).toContain('button-cancel');
      expect(wrapper.text()).not.toContain('upload-cancelled');
      expect(wrapper.text()).not.toContain('upload-completed');
    });

    it('should render cancelled text when file is cancelled', async () => {
      const wrapper = createWrapper();
      await wrapper.setData({
        uploadFile: {
          ...wrapper.vm.uploadFile,
          status: UploadStatus.CANCELLED,
        },
      });

      expect(wrapper.text()).not.toContain('button-cancel');
      expect(wrapper.text()).toContain('upload-cancelled');
      expect(wrapper.text()).not.toContain('upload-completed');
    });

    it('should render success text when file is uploaded', async () => {
      const wrapper = createWrapper();
      await wrapper.setData({
        uploadFile: {
          ...wrapper.vm.uploadFile,
          status: UploadStatus.COMPLETED,
        },
      });

      expect(wrapper.text()).not.toContain('button-cancel');
      expect(wrapper.text()).not.toContain('upload-cancelled');
      expect(wrapper.text()).toContain('upload-completed');
    });
  });
});

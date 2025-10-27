import {shallowMount} from '@vue/test-utils';

import AppUploadPanel from '@/components/appengine/panels/AppUploadPanel.vue';

describe('AppUploadPanel.vue', () => {
  const mockedSelectedFiles = [
    new File(
      ['PK\x03\x04'],
      'test.zip',
      {
        type: 'application/zip',
        lastModified: new Date(),
      }
    ),
  ];

  const createWrapper = (options = {}) => shallowMount(
    AppUploadPanel,
    {
      mocks: {
        $t: (key) => key,
      },
      stubs: {
        FileUploadItem: true,
        'b-button': true,
        'b-field': true,
        'b-icon': true,
        'b-upload': true,
      },
      ...options,
    },
  );

  it('should render translated text', () => {
    const wrapper = createWrapper();

    expect(wrapper.text()).toContain('app-engine.task.upload');
    expect(wrapper.text()).toContain('upload-placeholder');
    expect(wrapper.text()).toContain('upload-support');
  });

  it('should render files to upload', async () => {
    const wrapper = createWrapper();
    await wrapper.setData({
      selectedFiles: mockedSelectedFiles,
    });

    expect(wrapper.text()).toContain(`files (${mockedSelectedFiles.length})`);
    expect(wrapper.text()).toContain('upload-all');
  });
});

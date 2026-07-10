import {shallowMount} from '@vue/test-utils';

import CytomineStorage from '@/components/storage/CytomineStorage.vue';
import {StorageCollection, UploadedFileStatus} from '@/api';

jest.mock('@/utils/image-utils', () => ({
  isWebPSupported: jest.fn(() => true),
}));

jest.mock('@/api', () => ({
  Cytomine: {instance: {fetchSignature: jest.fn(), api: {get: jest.fn()}}},
  StorageCollection: {fetchAll: jest.fn()},
  ProjectCollection: {fetchAll: jest.fn()},
  UploadedFile: jest.fn(),
  UploadedFileStatus: {
    UPLOADED: 0,
    DETECTING_FORMAT: 10,
    EXTRACTING_DATA: 20,
    CONVERTING: 30,
    DEPLOYING: 40,
    DEPLOYED: 100,
    CONVERTED: 104,
  },
  User: {fetchCurrentUserKeys: jest.fn()},
}));

describe('CytomineStorage.vue', () => {
  const createWrapper = ({data = {}} = {}) => shallowMount(CytomineStorage, {
    mocks: {
      $t: (key) => key,
    },
    stubs: {
      'list-uploaded-files': true,
      'cytomine-multiselect': true,
      'b-message': true,
      'b-upload': true,
    },
    computed: {
      currentUser: () => ({id: 42}),
      currentAccount: () => ({isDeveloper: false}),
    },
    data() {
      return {...data};
    },
  });

  beforeEach(() => {
    jest.clearAllMocks();
  });

  it('should select the storage belonging to the current user', async () => {
    StorageCollection.fetchAll.mockResolvedValue({
      array: [
        {id: 1, name: 'other', userId: 1},
        {id: 2, name: 'mine', userId: 42},
      ],
    });

    const wrapper = createWrapper();
    await wrapper.vm.fetchStorages();

    expect(wrapper.vm.selectedStorage).toMatchObject({id: 2, userId: 42});
  });

  it('should add unprocessed files only when files change', () => {
    const wrapper = createWrapper();
    const alreadyAdded = {name: 'a.txt', processed: true};
    const newFile = {name: 'b.txt'};

    wrapper.vm.filesChange([alreadyAdded, newFile]);

    expect(wrapper.vm.dropFiles).toHaveLength(1);
    expect(wrapper.vm.dropFiles[0].file).toBe(newFile);
  });

  it('should only cancel files that are not yet uploaded', () => {
    const uploaded = {file: {name: 'done.txt'}, uploading: false, uploadedFile: {status: UploadedFileStatus.UPLOADED}, cancelToken: null};
    const pending = {file: {name: 'pending.txt'}, uploading: false, uploadedFile: null, cancelToken: null};

    const wrapper = createWrapper({data: {dropFiles: [uploaded, pending]}});
    wrapper.vm.cancelAll();

    expect(wrapper.vm.dropFiles).toEqual([uploaded]);
  });

  it('should only hide files with a finished status', () => {
    const finished = {file: {name: 'done.txt'}, uploadedFile: {status: UploadedFileStatus.CONVERTED}};
    const inProgress = {file: {name: 'progress.txt'}, uploadedFile: {status: UploadedFileStatus.CONVERTING}};

    const wrapper = createWrapper({data: {dropFiles: [finished, inProgress]}});
    wrapper.vm.hideFinished();

    expect(wrapper.vm.dropFiles).toEqual([inProgress]);
  });
});

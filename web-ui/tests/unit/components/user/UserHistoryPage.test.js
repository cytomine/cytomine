import {createLocalVue, mount} from '@vue/test-utils';
import Buefy from 'buefy';

import UserHistoryPage from '@/components/user/UserHistoryPage';
import {Cytomine} from '@/api';
import {UploadedFileStatus} from '@/constants/UploadedFileStatus';
import {flushPromises} from '../../../utils';

jest.mock('@/api', () => ({
  Cytomine: {
    instance: {
      api: {
        get: jest.fn(),
        post: jest.fn(),
      },
    },
  },
}));

jest.mock('@/utils/date', () => ({
  formatDate: jest.fn((date) => date),
}));

const insertCommand = {
  id: '189bd77a-583e-4560-9e32-09f766fcbd67',
  created: '2026-07-14T08:36:32',
  commandRequest: {
    commandType: 'INSERT_UPLOADED_FILE_COMMAND',
    userId: 45,
    after: {id: 2, filename: '/data/pims/tmp/abc', originalFilename: 'image', status: UploadedFileStatus.UPLOADED},
  },
};

const updateCommand = {
  id: '10e41aea-86d8-49fe-84b9-d99f23fcaa11',
  created: '2026-07-14T08:36:33',
  commandRequest: {
    commandType: 'UPDATE_UPLOADED_FILE_COMMAND',
    userId: 45,
    before: {id: 2, filename: 'upload-abc/image', originalFilename: 'image', status: UploadedFileStatus.CHECKING_INTEGRITY},
    after: {id: 2, filename: 'upload-abc/image', originalFilename: 'image', status: UploadedFileStatus.DEPLOYED},
  },
};

const undoCommand = {
  id: '9c09888d-e40b-4740-adda-f4d9eda9d9f4',
  created: '2026-07-14T08:37:19',
  commandRequest: {
    commandType: 'UNDO_CREATE_COMMAND',
    commandId: '3a6e777f-470b-4bf0-abb5-996729e9299d',
    target: {
      commandType: 'INSERT_ONTOLOGY_COMMAND',
      userId: 45,
      after: {id: 1, name: 'TEST-PROJECT'},
    },
  },
};

const commands = [undoCommand, updateCommand, insertCommand];

const createWrapper = async (options = {}) => {
  const localVue = createLocalVue();
  localVue.use(Buefy);

  const wrapper = mount(UserHistoryPage, {
    localVue,
    mocks: {
      $t: (message) => message,
      $i18n: {locale: 'en'},
      $notify: jest.fn(),
    },
    ...options,
  });
  await flushPromises();
  return wrapper;
};

describe('UserHistoryPage.vue', () => {
  beforeEach(() => {
    Cytomine.instance.api.get.mockResolvedValue({data: {collection: commands, size: commands.length}});
    Cytomine.instance.api.post.mockResolvedValue({});
  });

  it('should fetch the commands sorted by creation date on creation', async () => {
    const wrapper = await createWrapper();

    expect(Cytomine.instance.api.get).toHaveBeenCalledWith(
      '/commands',
      {params: {page: 0, size: 20, sort: 'created,desc'}},
    );
    expect(wrapper.vm.commands).toEqual(commands);
    expect(wrapper.vm.total).toBe(3);
    expect(wrapper.vm.loading).toBe(false);
  });

  it('should render a row per command with operation, domain, and description', async () => {
    const wrapper = await createWrapper();

    const rows = wrapper.findAll('tbody tr');
    expect(rows.length).toBe(3);

    const cells = (index) => rows.at(index).findAll('td');
    expect(cells(0).at(1).text()).toBe('Undo');
    expect(cells(0).at(2).text()).toBe('Ontology');
    expect(cells(0).at(3).text()).toBe('TEST-PROJECT');

    expect(cells(1).at(1).text()).toBe('Update');
    expect(cells(1).at(2).text()).toBe('Uploaded file');
    expect(cells(1).at(3).text()).toBe('image');

    expect(cells(2).at(1).text()).toBe('Insert');
    expect(cells(2).at(2).text()).toBe('Uploaded file');
    expect(cells(2).at(3).text()).toBe('image');
  });

  it('should notify an error when the initial fetch fails', async () => {
    Cytomine.instance.api.get.mockRejectedValue(new Error('network error'));

    const wrapper = await createWrapper();

    expect(wrapper.vm.$notify).toHaveBeenCalledWith(
      {type: 'error', text: 'unexpected-error-info-message'},
    );
    expect(wrapper.vm.loading).toBe(false);
  });

  it('should refetch with the new sort when sorted from page 1', async () => {
    const wrapper = await createWrapper();

    wrapper.vm.onSort('created', 'asc');
    await flushPromises();

    expect(Cytomine.instance.api.get).toHaveBeenLastCalledWith(
      '/commands',
      {params: {page: 0, size: 20, sort: 'created,asc'}},
    );
  });

  it('should reset to the first page when sorted from another page', async () => {
    const wrapper = await createWrapper();

    await wrapper.setData({currentPage: 3});
    await flushPromises();

    wrapper.vm.onSort('created', 'asc');
    await flushPromises();

    expect(wrapper.vm.currentPage).toBe(1);
    expect(Cytomine.instance.api.get).toHaveBeenLastCalledWith(
      '/commands',
      {params: {page: 0, size: 20, sort: 'created,asc'}},
    );
  });

  it('should refetch the requested page when the current page changes', async () => {
    const wrapper = await createWrapper();

    await wrapper.setData({currentPage: 2});
    await flushPromises();

    expect(Cytomine.instance.api.get).toHaveBeenLastCalledWith(
      '/commands',
      {params: {page: 1, size: 20, sort: 'created,desc'}},
    );
  });

  it('should refetch with the new page size when it changes', async () => {
    const wrapper = await createWrapper();

    await wrapper.setData({perPage: 50});
    await flushPromises();

    expect(Cytomine.instance.api.get).toHaveBeenLastCalledWith(
      '/commands',
      {params: {page: 0, size: 50, sort: 'created,desc'}},
    );
  });

  it('should undo a command and refetch on success', async () => {
    const wrapper = await createWrapper();
    Cytomine.instance.api.get.mockClear();

    wrapper.findAll('tbody tr').at(0).find('button').trigger('click');
    await flushPromises();

    expect(Cytomine.instance.api.post).toHaveBeenCalledWith(`/commands/undo/${undoCommand.id}`);
    expect(wrapper.vm.$notify).toHaveBeenCalledWith({type: 'success', text: 'notify-success-undo'});
    expect(Cytomine.instance.api.get).toHaveBeenCalled();
    expect(wrapper.vm.undoing).toBeNull();
  });

  it('should notify an error when the undo fails', async () => {
    const wrapper = await createWrapper();
    Cytomine.instance.api.get.mockClear();
    Cytomine.instance.api.post.mockRejectedValue(new Error('forbidden'));

    wrapper.vm.undo(insertCommand);
    await flushPromises();

    expect(wrapper.vm.$notify).toHaveBeenCalledWith({type: 'error', text: 'notify-error-undo'});
    expect(Cytomine.instance.api.get).not.toHaveBeenCalled();
    expect(wrapper.vm.undoing).toBeNull();
  });

  it('should parse the command type into an operation and a domain', async () => {
    const wrapper = await createWrapper();

    expect(wrapper.vm.parseType('INSERT_UPLOADED_FILE_COMMAND')).toEqual(
      {operation: 'INSERT', domain: ['UPLOADED', 'FILE']}
    );
    expect(wrapper.vm.parseType('UNDO_CREATE_COMMAND')).toEqual({operation: 'UNDO', domain: ['CREATE']});
  });

  it('should map each operation to a tag type', async () => {
    const wrapper = await createWrapper();

    expect(wrapper.vm.operationTag(insertCommand)).toBe('is-success');
    expect(wrapper.vm.operationTag(updateCommand)).toBe('is-info');
    expect(wrapper.vm.operationTag(undoCommand)).toBe('is-warning');
    expect(wrapper.vm.operationTag({commandRequest: {commandType: 'MERGE_TERM_COMMAND'}})).toBe('is-light');
  });

  it('should resolve the domain of an undo command from its target', async () => {
    const wrapper = await createWrapper();

    expect(wrapper.vm.domainLabel(undoCommand)).toBe('Ontology');
    expect(wrapper.vm.domainLabel(insertCommand)).toBe('Uploaded file');
  });

  it('should build the description from the most relevant payload field', async () => {
    const wrapper = await createWrapper();

    expect(wrapper.vm.description(undoCommand)).toBe('TEST-PROJECT');
    expect(wrapper.vm.description(insertCommand)).toBe('image');

    const withFilenameOnly = {commandRequest: {commandType: 'DELETE_UPLOADED_FILE_COMMAND', before: {id: 7, filename: 'a/b'}}};
    expect(wrapper.vm.description(withFilenameOnly)).toBe('a/b');

    const withIdOnly = {commandRequest: {commandType: 'DELETE_USER_ROLE_COMMAND', before: {id: 7}}};
    expect(wrapper.vm.description(withIdOnly)).toBe('#7');

    const withoutPayload = {commandRequest: {commandType: 'DELETE_USER_ROLE_COMMAND'}};
    expect(wrapper.vm.description(withoutPayload)).toBe('');
  });
});

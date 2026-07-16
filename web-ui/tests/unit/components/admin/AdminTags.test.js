import {createLocalVue, mount} from '@vue/test-utils';
import Buefy from 'buefy';

import AdminTags from '@/components/admin/AdminTags';
import {Cytomine} from '@/api';
import {flushPromises} from '../../../utils';

jest.mock('@/api', () => ({
  Cytomine: {
    instance: {
      api: {
        get: jest.fn(),
      },
    },
  },
}));

jest.mock('@/utils/date', () => ({
  formatDate: jest.fn((date) => date),
}));

const createTag = (id, name, creatorName) => ({
  id,
  name,
  creatorName,
  created: '1752537600000',
  delete: jest.fn(),
  populate: jest.fn(),
});

let tags;

const createWrapper = async (options = {}) => {
  const localVue = createLocalVue();
  localVue.use(Buefy);

  const wrapper = mount(AdminTags, {
    localVue,
    stubs: {'tag-modal': true},
    mocks: {
      $t: (message) => message,
      $i18n: {locale: 'en'},
      $notify: jest.fn(),
      $buefy: {
        dialog: {
          confirm: jest.fn((params) => params.onConfirm()),
        },
      },
    },
    ...options,
  });
  await flushPromises();
  return wrapper;
};

describe('AdminTags.vue', () => {
  beforeEach(() => {
    tags = [
      createTag(1, 'brain', 'admin'),
      createTag(2, 'lung', 'john'),
      createTag(3, 'kidney', 'jane'),
    ];
    Cytomine.instance.api.get.mockResolvedValue({data: {collection: tags, size: tags.length}});
  });

  afterEach(() => {
    jest.clearAllMocks();
  });

  it('should fetch the tags on creation', async () => {
    const wrapper = await createWrapper();

    expect(Cytomine.instance.api.get).toHaveBeenCalledWith(
      '/tag.json',
      {params: {page: 0, size: 25, sort: 'created,desc'}},
    );
    expect(wrapper.vm.tags).toEqual(tags);
    expect(wrapper.vm.total).toBe(3);
    expect(wrapper.vm.loading).toBe(false);
  });

  it('should render a row per tag with name, creator, and creation date', async () => {
    const wrapper = await createWrapper();

    const rows = wrapper.findAll('tbody tr');
    expect(rows.length).toBe(3);

    const cells = rows.at(0).findAll('td');
    expect(cells.at(0).text()).toBe('brain');
    expect(cells.at(1).text()).toBe('admin');
    expect(cells.at(2).text()).toBe('1752537600000');
  });

  it('should display an error message when the fetch fails', async () => {
    Cytomine.instance.api.get.mockRejectedValue(new Error('network error'));

    const wrapper = await createWrapper();

    expect(wrapper.vm.error).toBe(true);
    expect(wrapper.vm.loading).toBe(false);
    expect(wrapper.text()).toContain('unexpected-error-info-message');
    expect(wrapper.vm.$notify).toHaveBeenCalledWith(
      {type: 'error', text: 'notify-error-fetch-tag'},
    );
  });

  it('should filter the tags by name from the search string', async () => {
    const wrapper = await createWrapper();

    await wrapper.setData({searchString: 'br*'});

    expect(wrapper.vm.filteredTags).toEqual([tags[0]]);
    const rows = wrapper.findAll('tbody tr');
    expect(rows.length).toBe(1);
    expect(rows.at(0).text()).toContain('brain');
  });

  it('should display an empty message when no tag matches the search string', async () => {
    const wrapper = await createWrapper();

    await wrapper.setData({searchString: 'unknown'});

    expect(wrapper.vm.filteredTags).toEqual([]);
    expect(wrapper.text()).toContain('no-tag-fitting-criteria');
  });

  it('should open the modal without tag when starting a tag creation', async () => {
    const wrapper = await createWrapper();

    await wrapper.find('button.is-link').trigger('click');

    expect(wrapper.vm.editedTag).toBeNull();
    expect(wrapper.vm.modal).toBe(true);
  });

  it('should open the modal with the tag when starting a tag edition', async () => {
    const wrapper = await createWrapper();

    await wrapper.findAll('tbody tr').at(0).find('button.is-link.is-small').trigger('click');

    expect(wrapper.vm.editedTag).toEqual(tags[0]);
    expect(wrapper.vm.modal).toBe(true);
  });

  it('should add the new tag to the list', async () => {
    const wrapper = await createWrapper();

    const newTag = createTag(4, 'liver', 'admin');
    wrapper.vm.addTag(newTag);

    expect(wrapper.vm.tags.length).toBe(4);
    expect(wrapper.vm.tags[3]).toEqual(newTag);
  });

  it('should populate the edited tag on update', async () => {
    const wrapper = await createWrapper();

    wrapper.vm.startTagEdition(wrapper.vm.tags[0]);
    wrapper.vm.updateTag({name: 'renamed'});

    expect(tags[0].populate).toHaveBeenCalledWith({name: 'renamed'});
  });

  it('should delete the tag and remove it from the list on confirmation', async () => {
    const wrapper = await createWrapper();
    const deletedTag = wrapper.vm.tags[0];

    await wrapper.findAll('tbody tr').at(0).find('button.is-danger').trigger('click');

    expect(wrapper.vm.$buefy.dialog.confirm).toHaveBeenCalled();
    expect(deletedTag.delete).toHaveBeenCalled();
    expect(wrapper.vm.tags.length).toBe(2);
    expect(wrapper.vm.tags).not.toContain(deletedTag);
    expect(wrapper.vm.$notify).toHaveBeenCalledWith(
      {type: 'success', text: 'notif-success-tag-delete'},
    );
  });
});

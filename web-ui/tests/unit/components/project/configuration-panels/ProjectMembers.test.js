import { createLocalVue, shallowMount } from '@vue/test-utils';
import Buefy from 'buefy';

import ProjectMembers from '@/components/project/configuration-panels/ProjectMembers.vue';
import { ProjectRepresentative } from '@/api';

vi.mock('@/api', () => ({
  Cytomine: {
    instance: { host: 'https://test.cytomine.org', basePath: '/api/' }
  },
  UserCollection: vi.fn(),
  ProjectRepresentative: {
    delete: vi.fn().mockResolvedValue()
  }
}));

describe('ProjectMembers.vue', () => {
  let localVue;
  let projectMock;
  let dispatch;
  let notify;

  const CONTRIBUTOR = 'contributor';
  const MANAGER = 'manager';
  const REPRESENTATIVE = 'representative';

  const createWrapper = (selectedMembers = [], options = {}) => shallowMount(ProjectMembers, {
    localVue,
    data: () => ({ selectedMembers, loading: false }),
    computed: {
      project: () => projectMock,
      currentUser: () => ({ id: 999 }),
      shortTermToken: () => 'token'
    },
    mocks: {
      $t: (message) => message,
      $tc: (message) => message,
      $notify: notify,
      $store: { dispatch }
    },
    ...options
  });

  beforeEach(() => {
    localVue = createLocalVue();
    localVue.use(Buefy);

    projectMock = {
      id: 10,
      deleteUsers: vi.fn().mockResolvedValue(),
      deleteAdmin: vi.fn().mockResolvedValue()
    };
    dispatch = vi.fn().mockResolvedValue();
    notify = vi.fn();
  });

  afterEach(() => {
    vi.clearAllMocks();
  });

  it('should remove a contributor without touching admin or representative status', async () => {
    const wrapper = createWrapper([{ id: 1, fullName: 'Contributor', role: CONTRIBUTOR }]);

    await wrapper.vm.removeSelectedMembers();

    expect(projectMock.deleteAdmin).not.toHaveBeenCalled();
    expect(ProjectRepresentative.delete).not.toHaveBeenCalled();
    expect(projectMock.deleteUsers).toHaveBeenCalledWith([1]);
    expect(dispatch).toHaveBeenCalledWith('currentProject/fetchProjectMembers');
    expect(notify).toHaveBeenCalledWith(
      expect.objectContaining({ type: 'success' })
    );
  });

  it('should revoke admin status before deleting a manager', async () => {
    const wrapper = createWrapper([{ id: 2, fullName: 'Manager', role: MANAGER }]);

    await wrapper.vm.removeSelectedMembers();

    expect(projectMock.deleteAdmin).toHaveBeenCalledWith(2);
    expect(ProjectRepresentative.delete).not.toHaveBeenCalled();
    expect(projectMock.deleteUsers).toHaveBeenCalledWith([2]);
    expect(notify).toHaveBeenCalledWith(
      expect.objectContaining({ type: 'success' })
    );
  });

  it('should revoke representative and admin status before deleting a representative', async () => {
    const wrapper = createWrapper([{ id: 3, fullName: 'Representative', role: REPRESENTATIVE }]);

    await wrapper.vm.removeSelectedMembers();

    expect(ProjectRepresentative.delete).toHaveBeenCalledWith(0, 10, 3);
    expect(projectMock.deleteAdmin).toHaveBeenCalledWith(3);
    expect(projectMock.deleteUsers).toHaveBeenCalledWith([3]);
  });

  it('should handle a mixed selection, demoting only non-contributors', async () => {
    const wrapper = createWrapper([
      { id: 1, fullName: 'Contributor', role: CONTRIBUTOR },
      { id: 2, fullName: 'Manager', role: MANAGER },
      { id: 3, fullName: 'Representative', role: REPRESENTATIVE }
    ]);

    await wrapper.vm.removeSelectedMembers();

    // only the representative triggers a representative deletion
    expect(ProjectRepresentative.delete).toHaveBeenCalledTimes(1);
    expect(ProjectRepresentative.delete).toHaveBeenCalledWith(0, 10, 3);

    // both manager and representative get their admin rights revoked
    expect(projectMock.deleteAdmin).toHaveBeenCalledTimes(2);
    expect(projectMock.deleteAdmin).toHaveBeenCalledWith(2);
    expect(projectMock.deleteAdmin).toHaveBeenCalledWith(3);
    expect(projectMock.deleteAdmin).not.toHaveBeenCalledWith(1);

    // everyone is removed from the user list
    expect(projectMock.deleteUsers).toHaveBeenCalledWith([1, 2, 3]);
  });

  it('should notify an error and skip refresh when deletion fails', async () => {
    projectMock.deleteUsers.mockRejectedValueOnce(new Error('boom'));
    const wrapper = createWrapper([{ id: 1, fullName: 'Contributor', role: CONTRIBUTOR }]);

    await wrapper.vm.removeSelectedMembers();

    expect(dispatch).not.toHaveBeenCalled();
    expect(notify).toHaveBeenCalledWith(
      expect.objectContaining({ type: 'error' })
    );
  });
});

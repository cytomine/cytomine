import {shallowMount} from '@vue/test-utils';

import OntologyDetails from '@/components/ontology/OntologyDetails.vue';
import {Ontology, ProjectCollection, User, Cytomine} from '@/api';
import {flushPromises} from '../../../utils';

jest.mock('@/api', () => ({
  Ontology: {
    fetch: jest.fn()
  },
  ProjectCollection: {
    fetchAll: jest.fn()
  },
  User: {
    fetch: jest.fn()
  },
  Cytomine: {
    instance: {
      api: {
        get: jest.fn()
      }
    }
  }
}));

describe('OntologyDetails.vue', () => {
  const propsOntology = {id: 1};
  const fullOntology = {id: 1, name: 'Test Ontology', user: 10, projects: [1, 2], save: jest.fn()};
  const defaultProjectCollection = [{id: 1}, {id: 2}];
  const defaultUser = {id: 10, fullName: 'John Doe'};

  const createWrapper = ({ontology = propsOntology, currentUser} = {}) => {
    const mockStore = {
      state: {
        currentUser: {
          account: {isDeveloper: true},
          user: {
            id: null,
            adminByNow: false,
            guestByNow: false,
            ...currentUser,
          },
        },
      },
    };

    return shallowMount(OntologyDetails, {
      propsData: {
        ontology,
      },
      mocks: {
        $notify: jest.fn(),
        $store: mockStore,
        $t: (key) => key,
      },
      stubs: {
        OntologyTree: true,
        RenameModal: true,
        'router-link': true,
        'b-loading': true,
        'b-message': true
      }
    });
  };

  beforeEach(() => {
    jest.clearAllMocks();
  });

  it('should fetch ontology, projects and creator on created', async () => {
    Ontology.fetch.mockResolvedValue(fullOntology);
    ProjectCollection.fetchAll.mockResolvedValue({
      array: defaultProjectCollection
    });
    User.fetch.mockResolvedValue(defaultUser);

    const wrapper = createWrapper();
    await flushPromises();

    expect(Ontology.fetch).toHaveBeenCalledWith(propsOntology.id);
    expect(ProjectCollection.fetchAll).toHaveBeenCalled();
    expect(User.fetch).toHaveBeenCalledWith(defaultUser.id);

    expect(wrapper.vm.creator).toEqual(defaultUser);
    expect(wrapper.vm.fullOntology).toEqual(fullOntology);
    expect(wrapper.vm.loading).toEqual(false);
    expect(wrapper.vm.managedProjects).toEqual(defaultProjectCollection);
    expect(wrapper.vm.projects).toEqual(defaultProjectCollection);
  });

  it('should allow edit when user is admin', () => {
    const wrapper = createWrapper({
      currentUser: {...defaultUser, adminByNow: true},
    });

    expect(wrapper.vm.canEdit).toBe(true);
  });

  it('should not allow edit for guest users', () => {
    const wrapper = createWrapper({
      currentUser: {...defaultUser, adminByNow: false, guestByNow: true}
    });

    expect(wrapper.vm.canEdit).toBe(false);
  });

  it('should rename ontology successfully', async () => {
    const wrapper = createWrapper();
    await flushPromises();

    await wrapper.vm.rename('New Name');

    expect(wrapper.vm.fullOntology.name).toBe('New Name');
    expect(wrapper.vm.fullOntology.save).toHaveBeenCalled();
    expect(wrapper.vm.$notify).toHaveBeenCalledWith(
      expect.objectContaining({type: 'success'})
    );
  });

  it('should handle rename error', async () => {
    jest.spyOn(console, 'error').mockImplementation(() => {});
    Ontology.fetch.mockResolvedValue({...fullOntology, save: jest.fn().mockRejectedValue(new Error('fail'))});
    const wrapper = createWrapper();
    await flushPromises();

    await wrapper.vm.rename('New Name');

    expect(wrapper.vm.$notify).toHaveBeenCalledWith(
      expect.objectContaining({type: 'error'})
    );
  });
});

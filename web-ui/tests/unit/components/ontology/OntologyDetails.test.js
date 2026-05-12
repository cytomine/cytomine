import {shallowMount} from '@vue/test-utils';

import OntologyDetails from '@/components/ontology/OntologyDetails.vue';
import {Ontology, ProjectCollection, User} from '@/api';
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
  const defaultOntology = {id: 1, name: 'Test Ontology', user: 10, projects: [1, 2]};
  const defaultProjectCollection = [{id: 1}, {id: 2}];
  const defaultUser = {id: 10, fullName: 'John Doe'};

  const createWrapper = ({ontology = defaultOntology} = {}) => {
    const mockStore = {
      state: {
        currentUser: {
          account: {isDeveloper: true},
          user: {
            id: null,
            adminByNow: false,
            guestByNow: false,
          },
        },
      },
    };

    return shallowMount(OntologyDetails, {
      propsData: {
        ontology,
      },
      mocks: {
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
    Ontology.fetch.mockResolvedValue(defaultOntology);
    ProjectCollection.fetchAll.mockResolvedValue({
      array: defaultProjectCollection
    });
    User.fetch.mockResolvedValue(defaultUser);

    const wrapper = createWrapper();
    await flushPromises();

    expect(Ontology.fetch).toHaveBeenCalledWith(defaultOntology.id);
    expect(ProjectCollection.fetchAll).toHaveBeenCalled();
    expect(User.fetch).toHaveBeenCalledWith(defaultUser.id);

    expect(wrapper.vm.creator).toEqual(defaultUser);
    expect(wrapper.vm.fullOntology).toEqual(defaultOntology);
    expect(wrapper.vm.loading).toEqual(false);
    expect(wrapper.vm.managedProjects).toEqual(defaultProjectCollection);
    expect(wrapper.vm.projects).toEqual(defaultProjectCollection);
  });
});

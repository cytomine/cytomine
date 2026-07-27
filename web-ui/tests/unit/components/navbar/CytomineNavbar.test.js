import {createLocalVue, shallowMount} from '@vue/test-utils';
import Vuex from 'vuex';

import CytomineNavbar from '@/components/navbar/CytomineNavbar';

vi.mock('@/lang.js', () => ({
  changeLanguageMixin: {
    methods: {
      changeLanguage: vi.fn()
    }
  }
}));

describe('CytomineNavbar.vue', () => {
  const localVue = createLocalVue();
  localVue.use(Vuex);
  localVue.directive('shortkey', {});

  const actions = {
    logout: vi.fn()
  };

  const keycloak = {
    hasResourceRole: vi.fn(() => false),
    logout: vi.fn().mockResolvedValue()
  };

  const createWrapper = ({user = {}, projects = {}} = {}) => {
    const store = new Vuex.Store({
      modules: {
        currentUser: {
          namespaced: true,
          state: {
            user: {fullName: 'John Doe', guestByNow: false, ...user}
          }
        },
        projects: {
          namespaced: true,
          state: projects
        }
      },
      actions
    });

    return shallowMount(CytomineNavbar, {
      localVue,
      store,
      mocks: {
        $t: (message) => message,
        $keycloak: keycloak,
        $notify: vi.fn()
      },
      stubs: {
        'router-link': true
      }
    });
  };

  it('should render the component correctly', () => {
    const wrapper = createWrapper();

    expect(wrapper.find('nav.navbar').exists()).toBe(true);
    expect(wrapper.find('[to="/projects"]').exists()).toBe(true);
    expect(wrapper.find('[to="/ontology"]').exists()).toBe(true);
    expect(wrapper.find('navbar-dropdown-stub[icon="fa-user"]').attributes('title')).toBe('John Doe');
  });

  it('should show the storage link only for non-guest users', () => {
    let wrapper = createWrapper();
    expect(wrapper.find('[to="/storage"]').exists()).toBe(true);

    wrapper = createWrapper({user: {guestByNow: true}});
    expect(wrapper.find('[to="/storage"]').exists()).toBe(false);
  });

  it('should show the admin link only for admin users', () => {
    let wrapper = createWrapper();
    expect(wrapper.find('[to="/admin"]').exists()).toBe(false);

    keycloak.hasResourceRole.mockImplementationOnce((role) => role === 'ADMIN');
    wrapper = createWrapper();
    expect(wrapper.find('[to="/admin"]').exists()).toBe(true);
  });

  it('should show the workspace dropdown only when there are active projects', () => {
    let wrapper = createWrapper();
    expect(wrapper.find('navbar-dropdown-stub[icon="fa-folder-open"]').exists()).toBe(false);

    wrapper = createWrapper({projects: {1: {}}});
    expect(wrapper.find('navbar-dropdown-stub[icon="fa-folder-open"]').exists()).toBe(true);
  });

  it('should dispatch the store logout and keycloak logout on logout', async () => {
    const wrapper = createWrapper();

    await wrapper.vm.logout();

    expect(actions.logout).toHaveBeenCalled();
    expect(keycloak.logout).toHaveBeenCalled();
  });
});

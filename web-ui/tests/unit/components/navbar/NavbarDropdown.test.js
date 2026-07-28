import {shallowMount} from '@vue/test-utils';

import NavbarDropdown from '@/components/navbar/NavbarDropdown';

describe('NavbarDropdown.vue', () => {
  const createWrapper = ({props = {}, path = '/'} = {}) => shallowMount(NavbarDropdown, {
    props: {
      title: 'dropdown title',
      ...props
    },
    slots: {
      default: '<a class="navbar-item">Item</a>'
    },
    global: {
      mocks: {
        $route: {path}
      }
    }
  });

  it('should render the component correctly', () => {
    const wrapper = createWrapper({props: {icon: 'fa-user'}});

    expect(wrapper.find('.navbar-link').text()).toBe('dropdown title');
    expect(wrapper.find('i').classes()).toEqual(['fas', 'fa-user']);
    expect(wrapper.find('.navbar-dropdown .navbar-item').text()).toBe('Item');
  });

  it('should not render an icon when none is provided', () => {
    const wrapper = createWrapper();

    expect(wrapper.find('i').exists()).toBe(false);
  });

  it('should apply custom classes to the dropdown', () => {
    const wrapper = createWrapper({props: {classes: ['is-right']}});

    expect(wrapper.find('.navbar-dropdown').classes()).toContain('is-right');
  });

  it('should mark the link as active only when the route matches one of the paths', () => {
    let wrapper = createWrapper({props: {listPathes: ['/project/']}, path: '/project/42'});
    expect(wrapper.find('.navbar-link').classes()).toContain('is-active');

    wrapper = createWrapper({props: {listPathes: ['/project/']}, path: '/ontology'});
    expect(wrapper.find('.navbar-link').classes()).not.toContain('is-active');
  });
});

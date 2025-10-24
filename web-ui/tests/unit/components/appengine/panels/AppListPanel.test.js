import {shallowMount} from '@vue/test-utils';

import AppListPanel from '@/components/appengine/panels/AppListPanel.vue';
import AppCard from '@/components/appengine/AppCard.vue';

describe('AppListPanel.vue', () => {
  const mockApplications = [
    {id: 1, name: 'App 1', version: '1.0.0'},
    {id: 2, name: 'App 2', version: '2.0.0'},
    {id: 3, name: 'App 3', version: '1.5.0'},
  ];

  const createWrapper = (options = {}) => shallowMount(
    AppListPanel,
    {
      propsData: {
        applications: mockApplications,
      },
      mocks: {
        $t: (key) => key,
      },
      stubs: {
        AppCard: true,
      },
      ...options,
    },
  );

  it('should render translated text', () => {
    const wrapper = createWrapper();

    expect(wrapper.text()).toContain('app-engine.tasks.installed');
  });

  it('should have props data', () => {
    const wrapper = createWrapper();

    expect(wrapper.vm.applications).toEqual(mockApplications);
  });

  it('should render the correct number of AppCard components', async () => {
    const wrapper = createWrapper();

    const appCards = wrapper.findAllComponents(AppCard);

    expect(appCards).toHaveLength(mockApplications.length);
  });

  it('should pass correct props to AppCard components', async () => {
    const wrapper = createWrapper();

    const appCards = wrapper.findAllComponents(AppCard);

    appCards.wrappers.forEach((cardWrapper, index) => {
      expect(cardWrapper.props('app')).toEqual(mockApplications[index]);
    });
  });

  it('should handle empty applications array', async () => {
    const wrapper = createWrapper({
      propsData: {
        applications: [],
      }
    });

    const appCards = wrapper.findAllComponents(AppCard);

    expect(appCards).toHaveLength(0);
    expect(wrapper.vm.applications).toEqual([]);
  });
});

import {createLocalVue, shallowMount} from '@vue/test-utils';
import moment from 'moment';

import ActivityLogsItem from '@/components/utils/ActivityLogsItem';
import VueRouter from 'vue-router';

vi.mock('@/utils/token-utils', () => ({
  appendShortTermToken: vi.fn((url, token) => `${url}?token=${token}`)
}));

vi.mock('@/utils/store-helpers', () => ({
  get: vi.fn(() => 'mock-token')
}));

vi.mock('@/utils/date', () => ({
  formatMomentDate: vi.fn((value, format) => moment.utc(Number(value)).format(format))
}));

describe('ActivityLogsItem.vue', () => {
  let wrapper;
  const action = {
    id: 1,
    created: '1626874800000',
    serviceName: 'userAnnotationService',
    className: 'be.cytomine.command.AddCommand',
    data: JSON.stringify({
      id: 1,
      url: 'https://example.com/image',
      project: 123,
      image: 456
    }),
    message: 'Added a new annotation'
  };

  const localVue = createLocalVue();
  localVue.use(VueRouter);
  localVue.component('router-link', {template: '<a><slot></slot></a>'});
  localVue.component('router-view', {template: '<div><slot></slot></div>'});

  beforeEach(() => {
    wrapper = shallowMount(ActivityLogsItem, {
      localVue,
      propsData: {
        action
      },
      computed: {
        shortTermToken: () => 'mock-token'
      },
    });
  });

  afterEach(() => {
    vi.clearAllMocks();
  });

  it('should render the correct timestamp and message', () => {
    const strongTag = wrapper.find('strong');
    const messageSpan = wrapper.find('.content');

    expect(strongTag.text()).toBe('7/21/2021 1:40:00 PM:');
    expect(messageSpan.text()).toContain('Added a new annotation');
  });

  it('should create the correct annotation route and preview', async () => {
    wrapper.vm.$options.created[0].call(wrapper.vm);
    await wrapper.vm.$nextTick();

    expect(wrapper.vm.route).toBe('/project/123/image/456/annotation/1');
    expect(wrapper.vm.previewUrl).toBe('https://example.com/image?maxSize=125&complete=true&thickness=2&increaseArea=1.25&draw=true');
  });

  it('should toggle preview when hovering over', async () => {
    expect(wrapper.vm.showPreview).toBe(false);

    wrapper.vm.enter();
    await wrapper.vm.$nextTick();
    expect(wrapper.vm.showPreview).toBe(true);

    wrapper.vm.leave();
    await wrapper.vm.$nextTick();
    expect(wrapper.vm.showPreview).toBe(false);
  });
});

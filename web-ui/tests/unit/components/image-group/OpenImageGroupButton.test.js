import {shallowMount, createLocalVue} from '@vue/test-utils';
import {createRouter, createMemoryHistory} from 'vue-router';

import OpenImageGroupButton from '@/components/image-group/OpenImageGroupButton';

const localVue = createLocalVue();
const router = createRouter({history: createMemoryHistory(), routes: [{path: '/:pathMatch(.*)*', component: {template: '<div/>'}}]});
localVue.use(router);

const mockImages = (count) => Array.from({length: count}, (_, i) => ({id: i + 1, name: `image-${i + 1}.tif`}));

const createWrapper = (imageGroup = {}) =>
  shallowMount(OpenImageGroupButton, {
    localVue,
    router,
    propsData: {
      imageGroup: {
        project: 'project-1',
        imageInstances: mockImages(4),
        ...imageGroup,
      },
    },
    mocks: {$t: (key) => key},
    stubs: {
      'b-dropdown-item': true,
      'b-dropdown': true,
      ImageName: true,
    },
  });

describe('OpenImageGroupButton', () => {
  describe('disabled state', () => {
    it('should render disabled button when imageInstances is empty', () => {
      const wrapper = createWrapper({imageInstances: []});
      expect(wrapper.find('button[disabled]').exists()).toBe(true);
    });

    it('should render router-link when imageInstances is not empty', () => {
      const wrapper = createWrapper();
      expect(wrapper.findComponent({name: 'router-link'}).exists()).toBe(true);
    });
  });

  describe('dropdown visibility', () => {
    it('should not render dropdown when there is only 1 image', () => {
      const wrapper = createWrapper({imageInstances: mockImages(1)});
      expect(wrapper.findComponent({name: 'b-dropdown'}).exists()).toBe(false);
    });

    it('should render dropdown when there are multiple images', () => {
      const wrapper = createWrapper();
      expect(wrapper.findComponent({name: 'b-dropdown'}).exists()).toBe(true);
    });
  });

  describe('batches', () => {
    it('should split images into correct number of batches', () => {
      const wrapper = createWrapper({imageInstances: mockImages(7)});
      expect(wrapper.vm.batches.length).toBe(2);
    });

    it('should compute correct batch start and end', () => {
      const wrapper = createWrapper({imageInstances: mockImages(4)});
      expect(wrapper.vm.batches[0]).toMatchObject({start: 0, end: 4});
    });
  });

  describe('batchSize', () => {
    it('should clamp batchSize to maxBatchSize when images are fewer', async () => {
      const wrapper = createWrapper({imageInstances: mockImages(2)});
      expect(wrapper.vm.batchSize).toBe(2);
    });

    it('should clamp batchSize when maxBatchSize decreases', async () => {
      const wrapper = createWrapper();
      wrapper.vm.batchSize = 4;
      await wrapper.setProps({imageGroup: {project: 'proj-1', imageInstances: mockImages(2)}});
      expect(wrapper.vm.batchSize).toBe(2);
    });
  });

  describe('viewerURL', () => {
    it('should generate correct URL from images', () => {
      const wrapper = createWrapper();
      const url = wrapper.vm.viewerURL(mockImages(2));
      expect(url).toBe('/project/project-1/image/1-2');
    });
  });
});

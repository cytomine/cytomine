import { flushPromises, mount } from '@vue/test-utils';
import Buefy from 'buefy';

import PasteAnnotationWithLinkModal from '@/components/viewer/interactions/PasteAnnotationWithLinkModal.vue';

vi.mock('ol/format/WKT', () => ({ __esModule: true, default: vi.fn() }));

vi.mock('@/api', () => ({
  ImageGroup: {
    fetch: vi.fn(async () => ({
      name: 'group',
      imageInstances: [{ id: 1 }, { id: 2 }, { id: 3 }],
    })),
  },
  ImageInstanceCollection: vi.fn(),
  AnnotationGroup: vi.fn(),
  AnnotationCollection: vi.fn(),
  Annotation: vi.fn(),
  AnnotationLink: vi.fn(),
}));

// Issue 10 replaced `@change.native` on the two "check all" boxes with
// `@update:model-value`, the event `b-checkbox` actually declares. Neither the
// build nor lint can tell whether the handler still runs.
describe('PasteAnnotationWithLinkModal.vue', () => {
  const createWrapper = async () => {
    const wrapper = mount(PasteAnnotationWithLinkModal, {
      props: { index: '0' },
      global: {
        plugins: [Buefy],
        stubs: { 'annotation-links-preview': true, 'image-name': true },
        mocks: {
          $t: key => key,
          $store: {
            commit: vi.fn(),
            getters: {
              'currentUser/user': { id: 1 },
              'currentProject/currentViewerModule': 'viewer/',
              'currentProject/imageModule': () => 'image/',
              'image/imageGroupId': 7,
              'currentProject/currentViewer': {
                copiedAnnot: { id: 100, image: 1, group: null, annotationLink: [] },
                copiedAnnotImageInstance: { id: 1 },
                images: {
                  0: { imageInstance: { id: 1 }, imageGroupLink: { group: 7 }, view: {} },
                  1: { imageInstance: { id: 2 }, imageGroupLink: { group: 7 }, view: {} },
                },
              },
            },
          },
        },
      },
    });
    await flushPromises();
    return wrapper;
  };

  // Image 2 is in the group and open in the viewer; image 3 is in the group
  // only. Image 1 holds the copied annotation, so it is not eligible.
  it('renders one "check all" box per section', async () => {
    const wrapper = await createWrapper();

    expect(wrapper.findAll('.field.header')).toHaveLength(2);
  });

  it('ticks every in-viewer image when its "check all" box is toggled', async () => {
    const wrapper = await createWrapper();

    await wrapper.findAll('.field.header input[type="checkbox"]')[0].setValue(true);

    expect(wrapper.vm.checkedInViewer).toEqual([2]);
  });

  it('ticks every not-in-viewer image when its "check all" box is toggled', async () => {
    const wrapper = await createWrapper();

    await wrapper.findAll('.field.header input[type="checkbox"]')[1].setValue(true);

    expect(wrapper.vm.checkedNotInViewer).toEqual([3]);
  });

  it('unticks them again when the box is toggled back', async () => {
    const wrapper = await createWrapper();

    const checkAll = wrapper.findAll('.field.header input[type="checkbox"]')[0];
    await checkAll.setValue(true);
    await checkAll.setValue(false);

    expect(wrapper.vm.checkedInViewer).toEqual([]);
  });

  // Issue 12 replaced the `this.$set(imageInstance, 'inViewerPosition', …)` pair
  // in `created()` with plain assignment, which carries reactivity only because
  // the images are read back out of `this.imageGroup` and so arrive as proxies.
  // Checked by breaking it both ways: dropping the two assignments fails the
  // seeding test, and marking the images raw fails the other two.
  describe('the position fields added after the images are fetched', () => {
    it('seeds each select with the default for its section', async () => {
      const wrapper = await createWrapper();

      const selects = wrapper.findAll('.field:not(.header) select');
      expect(selects).toHaveLength(2);
      expect(selects[0].element.value).toBe('viewer');
      expect(selects[1].element.value).toBe('image');
    });

    it('re-renders the select when the position changes after mount', async () => {
      const wrapper = await createWrapper();

      wrapper.vm.imagesInGroupInViewer[0].inViewerPosition = 'annotation';
      await wrapper.vm.$nextTick();

      expect(wrapper.find('.field:not(.header) select').element.value).toBe('annotation');
    });

    it('carries the chosen position into the selection', async () => {
      const wrapper = await createWrapper();

      await wrapper.findAll('.field.header input[type="checkbox"]')[0].setValue(true);
      await wrapper.findAll('.field:not(.header) select')[0].setValue('annotation');

      expect(wrapper.vm.selectedImagesAndOptions)
        .toEqual([{ image: 2, position: 'annotation' }]);
    });
  });
});
